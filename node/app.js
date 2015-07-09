var express = require('express');
var session = require('express-session');
var http = require('http');
var app = express();
var bodyParser = require('body-parser');
var cookieParser = require('cookie-parser');

var args = require('minimist')(process.argv.slice(2));

if (! args.c) {
    //
    // Default to this for ease of early development.
    //
    args.c="happywhale";
}

//
// Read in any configuration parameters
//
var fs = require('fs');
var config = JSON.parse(fs.readFileSync("cust/" + args.c + "/server/config.json", "utf8"));

if (args.config) {
    try {
        var override = JSON.parse(fs.readFileSync(args.config, "utf8"));

        if (override) {
            var extend = require('extend');
            config = extend(true, config, override);
        }
    } catch (ex) {
        console.log(ex);
    }
}

if (config.client.wildbook.proxy) {
    config.client.wildbook.proxyUrl = "/wildbook";
} else {
    config.client.wildbook.proxyUrl = config.client.wildbook.url;
}

var secrets = JSON.parse(fs.readFileSync("secrets.json", "utf8"));

if (args.debug) {
    console.log(config);
    console.log(secrets);
}

var i18n = require('i18next');

//
// This does work if you put a cust.json file in your locales
// directories and then address it as cust:blah.blah. But I'm trying to
// get this done quick and so I'm not going to use that functionality at the
// moment as trying to figure out what is Happy Whale vs. generic is something
// we will have to do later.
//
var options = {
    ns: {
        namespaces: ['cust', 'app'],
        defaultNs: 'app'
    },
    fallbackToDefaultNS: true,
//        saveMissing: true,
//        ignoreRoutes: ['images/', 'public/', 'css/', 'js/']
//    debug: true
};

i18n.init(options, function() {
    console.log("App Title: " + i18n.t("app.title"));
});

app.use(session({
   secret: 'wildbook is for birds (and whales)',
   resave: false,
   saveUninitialized: true
}));


app.set('port', 3000);
app.set('views', __dirname + '/server/views');
app.set('view engine', 'jade');
app.locals.pretty = true;
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: false}));
app.use(cookieParser());
app.use(i18n.handle);
i18n.registerAppHelper(app);

if (app.get('env') === 'development') {
    app.use(function(err, req, res, next) {
        res.status(err.status || 500);
        res.render('error', {
            message: err.message,
            error: err
        });
    });
}

//production error handler
//no stacktraces leaked to user
app.use(function(err, req, res, next) {
    res.status(err.status || 500);
    res.render('error', {
        message: err.message,
        error: {}
    });
});

//app.use(require('stylus').middleware({ src: __dirname + '/app/public' }));
app.use(express.static(__dirname + '/public'));

require('./server/router')(app, config, secrets, args.debug);

http.createServer(app).listen(app.get('port'), function(){
    console.log("Express server listening on port " + app.get('port'));
});
