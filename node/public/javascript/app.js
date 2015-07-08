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
});

//app.ngApp = ngApp;

$(document).ready(function() {
    //
    // Trigger bootstrap tooltips
    //
    $('[data-toggle="tooltip"]').tooltip();

	$('#search-site').autocomplete({
		appendTo: $('#navbar-top'),
		response: function(ev, ui) {
			if (ui.content.length < 1) {
				$('#search-help').show();
			} else {
				$('#search-help').hide();
			}
		},
		select: function(ev, ui) {
			alert('selected "' + ui.item.label + '" with fake url: ' + ui.item.value);
			return false;
		},
		source: wildbookGlobals.baseUrl + "/search"
	});

	//this hides the no results message when the user leaves search field
	$('#search-site').on('blur', function() { $('#search-help').hide(); });
});

