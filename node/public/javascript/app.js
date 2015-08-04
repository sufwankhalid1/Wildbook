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

app.userDiv = function(user) {
    if (!user) {
        return null;
    }

    var div = $('<div>').addClass("user-info");
    var avatar = $('<div>').attr("data-toggle", "tooltip")
            .attr("title", "")
            .addClass("user-avatar")
            .attr("data-original-title", user.displayName).tooltip();
    div.append(avatar);

    var image;
    if (user.avatar) {
        image = $('<img>').attr("src", user.avatar)
            .attr("onerror", "this.onerror=null;this.src=\'/cust/images/img_user_on.svg\'");
    } else {
        image = $('<img>').attr("src", "/cust/images/img_user_on.svg");
    }

    var link = $('<a>').attr("href", "/user/" + user.username);
    link.append(image);
    avatar.append(link);

    return div;
}


//ngApp.factory('dataService', function() {
//    var _data = {};
//    return {
//        data: _data
//    };
//});

angular.module("nodeApp.controllers", [])
//.controller("AppController", function ($scope, $http, dataService) {
//    $scope.data = dataService.data;
.controller("AppController", function ($scope, $http) {
    $scope.login = function() {
        wildbook.auth.loginPopup($scope);
    };

    $scope.terms = function() {
        $http.get("/terms")
        .then(function(terms) {
            alertplus.alert(terms.data, null, "Usage Agreement");
        });
    }
});

//app.ngApp = ngApp;

$(document).ready(function() {
    moment.locale(window.navigator.userLanguage || window.navigator.language || 'en');

    //
    // Trigger bootstrap tooltips
    //
    $('[data-toggle="tooltip"]').tooltip();

    configPromise.done( function() {
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
                if (ui.item.type == "individual") {
                    window.location.replace("/individual/" + ui.item.value);
                } else if (ui.item.type == "user") {
                    window.location.replace("/user/" + ui.item.value);
                } else {
                    alertplus.alert("Unknown result [" + ui.item.value + "] of type [" + ui.item.type + "]");
                }
                return false;
            },
            //source: app.config.wildbook.proxyUrl + "/search"
            source: function( request, response ) {
                $.ajax({
                    url: app.config.wildbook.proxyUrl + "/search",
                    dataType: "json",
                    data: {
                        term: request.term
                    },
                    success: function( data ) {
                        var res = $.map(data, function(item) {
                            var label;
                            if (item.type == "individual") {
                                label = "Whale: ";
                            } else if (item.type == "user") {
                                label = "User: ";
                            } else {
                                label = "";
                            }
                            return {label: label + item.label,
                                    value: item.value,
                                    type: item.type};
                            });

                        response(res);
                    }
                });
            }
        });
    });

    //this hides the no results message when the user leaves search field
    $('#search-site').on('blur', function() { $('#search-help').hide(); });
});

