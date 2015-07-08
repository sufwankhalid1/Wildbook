//
// TODO: Uncomment when you are ready to use the database.
//
//var mongodb = require('mongodb');
//var dbPort = 27017;
//var dbHost = 'localhost';
//var dbName = 'wildbook';
//
//// establish the database connection
//var db = new mongodb.Db(dbName, new mongodb.Server(dbHost, dbPort, {auto_reconnect: true}), {w: 1});
//    db.open(function(ex, db) {
//    if (ex) {
//        console.log(ex);
//    } else {
//        console.log('connected to database [' + dbName + ']');
//    }
//});

var request = require('request');
//var https = require('https');
var extend = require('extend');

//
// Set up social data grabbing
//
var home = {
    spotlight: {
        name: "Frosty",
        species: "Humpback Whale",
        id: "Cascadia #12492",
        place: "Monterey Bay, CA"
    },
    social: {"instagram": {"feed": []},
             "twitter": {"feed": []}}
};

// Instagram photos
function instagramFeed(config, secrets) {
    var url = "https://api.instagram.com/v1/users/"
        + config.client.social.instagram.user_id
        + "/media/recent/?count="
        + (config.client.social.instagram.feed_count ? config.client.social.instagram.feed_count : 4)
        + "&access_token="
        + secrets.social.instagram.access_token;
//    console.log(url);
    request(url, function(error, response, body) {
        if (error) {
            console.log(error);
            home.social.instagram.feed = [];
            return;
        }

        if (response.statusCode !== 200) {
            console.log("url [" + url + "] returned status [" + response.statusCode + "]");
            home.social.instagram.feed = [];
            return
        }

        try {
            home.social.instagram.feed = JSON.parse(body).data;
        } catch (err) {
            console.log(err);
        }
    });
}

var Codebird = require("codebird");
function twitterFeed(config) {
    //
    // This will call the following, but with the required authentication
    //    https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=happyhumpback&count=4
    //
    var cb = new Codebird;
//    cb.setConsumerKey(config.client.social.twitter.consumer_key, secrets.social.twitter.consumer_secret);
    cb.setBearerToken(home.social.twitter.token);
    cb.__call(
        "statuses_userTimeline",
        {
            "screen_name": config.client.social.twitter.id,
            "count": config.client.social.twitter.feed_count || 4
        },
        function (reply) {
            if( Object.prototype.toString.call( reply ) !== '[object Array]' ) {
                console.log("Twitter feed issue:");
                console.log(reply);
                return;
            }
            //
            // Since we are only using the text, just return that. We need the map function anyway
            // to convert the string to html so that we can convert any links.
            //
            home.social.twitter.feed = reply.map(function(value) {
                var tweet = value.text || "";
//                console.log(tweet);
                var value;
                var startIndex = 0;
                var index = tweet.indexOf("http");
                if (index < 0) {
                    value = tweet;
                } else {
                    value = "";
                    while (index >= 0) {
                        value += tweet.slice(startIndex, index);
                        var spaceIndex = tweet.indexOf(" ", index);
                        var link;
                        if (spaceIndex <= -1) {
                            link = tweet.slice(index);
                            index = -1;
                        } else {
                            link = tweet.slice(index, spaceIndex);
                            index = tweet.indexOf("http", spaceIndex);
                        }
                        value += '<a href="' + link + '" target="_blank">' + link + '</a>';

                        //
                        // If this is the last then we need to make sure we get the rest of it.
                        //
                        if (index < 0 && spaceIndex >= 0) {
                            value += tweet.slice(spaceIndex);
                        }
                    };
                }

                return {text: value};
            });

//            console.log(home.social.twitter.feed);
//            console.log(reply);
        },
        true // this parameter required for AppAuthentication, which is done without the consumer key and secret.
    );
}
//
// End social data grabbing.
//


module.exports = function(app, config, secrets, debug) {
    var vars = {config: config.client};

    var cb = new Codebird;
    cb.setConsumerKey(config.client.social.twitter.consumer_key, secrets.social.twitter.consumer_secret);
    cb.__call(
        "oauth2_token",
        {},
        function (reply) {
            home.social.twitter.token = reply.access_token;
            twitterFeed(config);
        }
    );

    setInterval(function() {
        twitterFeed(config);
    }, 60*1000);

    instagramFeed(config, secrets);
    setInterval(function() {
        instagramFeed(config, secrets);
    }, 15*60*1000);

    //
    // Main site
    //
    app.get('/', function(req, res) {
        //
        // NOTE: i18n available as req.i18n.t or just req.t
        // Also res.locals.t
        //
        res.render('home', extend({}, vars, {home: home}));
    });

    app.get('/config', function(req,res) {
        res.send(config.client);
    });

    app.get('/submitMedia', function(req, res) {
        res.render('mediasubmission', vars);
    });

    app.get("/about", function(req, res) {
        res.render('about', vars);
    });

    app.get("/individual/*", function(req, res) {
        var id = req.url.slice(12);
//        http://tomcat:tomcat123@wildbook.happywhale.com/rest/org.ecocean.MarkedIndividual?individualID==%27<search_string>%27

        var url = config.wildbook.authUrl
            + "/rest/org.ecocean.MarkedIndividual?individualID==%27"
            + id
            + "%27";
        if (debug) {
            console.log(url);
        }
        request(url, function(error, response, body) {
            var data;
            if (error) {
                console.log(error);
                data = {error: error};
            } else if (response.statusCode !== 200) {
                console.log("url [" + url + "] returned status [" + response.statusCode + "]");
                data = {error: {status: response.statusCode}};
            } else {
                data = {"ind": JSON.parse(body)};
            }
            res.render("individual", extend({}, vars, {page: data}));
        });
    });

    //
    // Proxy over to wildbook
    //
    app.get('/wildbook/*', function(req, res) {
        //
        // Extract out the * part of the address and forward it through
        // a pipe and request like so. Have to handle error on each section of the pipe.
        //
        var url = config.wildbook.url + req.url.slice(9);

        if (debug) {
            console.log("wildbook GET: " + url);
        }

        req.pipe(request(url))
        .on('error', function(ex) {
            console.log("Trouble connecting to [" + url + "]");
            console.log(ex);
            res.status(500).send(ex);
        })
        .pipe(res)
        .on('error', function(ex) {
            console.log(ex);
        });
    });

    //
    // Proxy over to wildbook
    //
    app.post('/wildbook/*', function(req, res) {
        //
        // Extract out the * part of the address and forward it through
        // a pipe and request like so. Have to handle error on each section of the pipe.
        //
        var url = config.wildbook.url + req.url.slice(9);

        console.log("wildbook POST: " + url);

        req.pipe(request.post({uri: url, json: req.body}))
        .on('error', function(ex) {
            console.log("Trouble connecting to [" + url + "]");
            console.log(ex);
            res.status(500).send(ex);
        })
        .pipe(res)
        .on('error', function(ex) {
            console.log(ex);
        });
    });

    //=================
    // Catch-all
    //=================

    app.get('*', function(req, res) {
        res.render('404', vars);
    });
};
