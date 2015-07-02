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

angular.module('nodeApp', ['nodeApp.controllers'])
.factory('dataService', function() {
    var _data = {};
    return {
        data: _data
    };
});

angular.module("nodeApp.controllers", [])
.controller("AppController", function ($scope, $http, dataService) {
    $scope.data = dataService.data;

    function parseError(status, data) {
        var message;
        if (typeof data === "string") {
            message = data;
        } else {
            message = JSON.stringify(data);
        }

        return {status: status, message: message};
    }

    var promise = $http({
        method: "GET",
        url: "/config"
    }).success(function (config, status, headers, obj) {
        $scope.data.config = config;

        $http({
            method: "GET",
//            url: config.wildbook.url + "/obj/user"
            url: "/wildbook/obj/user"
        }).success(function (user, status, headers, obj) {
            if (user.username) {
                $scope.data.user = user;
            } else {
                $scope.data.user = null;
            }
        }).error(function (data, status, headers, config) {
            alertplus.error(parseError(status, data));
        });
    }).error(function (data, status, headers, config) {
        alertplus.error(parseError(status, data));
    });

    $scope.login = function() {
				wildbook.auth.loginPopup($scope);
        //$scope.data.user = {fullname: "Bobby Fisher"};
    };

    return promise;
});

