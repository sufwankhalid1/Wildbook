/* global angular */

'use strict';

// Declare app level module which depends on filters, and services
//angular.module('ppadminApp', ['ppadminApp.filters', 'ppadminApp.services', 'ppadminApp.directives'])
//.config(function($routeProvider, $locationProvider) {
//    $routeProvider.when('/edituser', {
//        templateUrl: 'partials/account',
//        controller: 'UserCtrl'
//    })
//    .otherwise({
//        redirectTo: '/'
//    });
//
//    $locationProvider.html5Mode(true);
//});

//function parseError(status, data) {
//    var message;
//    if (typeof data === "string") {
//        message = data;
//    } else {
//        message = JSON.stringify(data);
//    }
//
//    return {status: status, message: message};
//}
var ngApp = angular.module('nodeApp', ['nodeApp.controllers']);

function handleError(jqXHR, status) {
    alertplus.error(jqXHR);
}

var app = {};
var configPromise = $.get("/config")
.then(function(config) {
    app.config = config;
    return $.get(config.wildbook.url + "/obj/user");
}, handleError)
.then(function(user) {
    if (user.username) {
        app.user = user;
    } else {
        app.user = null;
    }
}, handleError);

app.configPromise = configPromise;

//ngApp.factory('dataService', function() {
//    var _data = {};
//    return {
//        data: _data
//    };
//});

angular.module("nodeApp.controllers", [])
//.controller("AppController", function ($scope, $http, dataService) {
//    $scope.data = dataService.data;
.controller("AppController", function ($scope) {
    $scope.login = function() {
        wildbook.auth.loginPopup($scope);
    };

    //
    // I think this properly delays it such that the rest of the controllers, e.g. on
    // the submitMedia page will not properly initialize until our promise is done.
    // It *seems* to work that way anyway.
    // UPDATE: NO, it doesn't can comment out I think.
    //
    return configPromise;
});

//app.ngApp = ngApp;

