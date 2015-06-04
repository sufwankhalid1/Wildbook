var express = require('express');
var session = require('express-session');
var http = require('http');
var app = express();
var bodyParser = require('body-parser');
var cookieParser = require('cookie-parser');

//
// Read in any configuration parameters
//
var fs = require('fs');
var config = JSON.parse(fs.readFileSync('server/cust/config.json', 'utf8'));

var i18n = require('i18next');

i18n.init({
    ns: {
        namespaces: [config.custcode, 'app'],
        defaultNs: config.custcode
    }
//    saveMissing: true,
//    ignoreRoutes: ['images/', 'public/', 'css/', 'js/']
//    debug: true
}, function() {
//    console.log(i18n.t('admin.login.title'));
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

require('./server/router')(app, config);

http.createServer(app).listen(app.get('port'), function(){
    console.log("Express server listening on port " + app.get('port'));
});