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

angular.module('nodeApp', ['nodeApp.controllers']);

angular.module("nodeApp.controllers", [])
.controller("AppCtrl", function ($scope, $http) {
    var promise = $http({
        method: "GET",
        url: "/config"
    }).success(function (config, status, headers, obj) {
        $scope.config = config;

        $http({
            method: "GET",
//            url: config.wildbook.url + "/obj/user"
            url: "/wildbook/obj/user"
        }).success(function (user, status, headers, obj) {
            if (user.username) {
                $scope.user = user;
            } else {
                $scope.user = null;
            }
        }).error(function (data, status, headers, config) {
            alertplus.alert(status + ": " + data);
        });
    }).error(function (data, status, headers, config) {
        alertplus.alert(status + ": " + data);
    });

    return promise;
});

