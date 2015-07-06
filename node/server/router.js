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
    news: [{headline: "News Section Headline",
            contents: "Lorem ipsum..."},
           {headline: "News Section Headline 2",
            contents: "Ut enim..."}],
    social: {"instagram": {},
             "twitter": {}}
};

// Instagram photos
function instagramFeed(config, secrets) {
    var url = "https://api.instagram.com/v1/users/"
        + config.client.social.instagram.user_id
        + "/media/recent/?count=4&access_token="
        + secrets.social.instagram.access_token;
//    request.get(url)
//    .on('response', function(response) {
//        console.log("******onResponse:");
//        if (typeof response == "string") {
//            console.log(response.slice(0, 100));
//        } else {
//            console.log("RESPONSE NOT STRING");
//        }
//    })
//    .on('data', function(data) {
//        console.log("******onData:");
//        console.log(data);
//        home.social.instagram.feed = data.data;
//    })
//    .on('error', function(err) {
//        console.log("******onError:");
//        console.log(err);
//    });
    console.log(url);
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
//
// End social data grabbing.
//


module.exports = function(app, config, secrets, debug) {
    var vars = {config: config.client};

    instagramFeed(config, secrets);

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
