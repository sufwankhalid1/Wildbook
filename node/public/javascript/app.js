/* global angular */

//'use strict';

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

function handleError(jqXHR) {
    alertplus.error(jqXHR);
}

var app = {};
var configPromise = $.get("/config")
.then(function(config) {
    app.config = config;
    if (typeof maptool !== 'undefined') {
        maptool.init(config.maptool);
    }
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

app.beingDiv = function(being) {
    if (!being) {
        return null;
    }

    var div = $('<div>').addClass("being-info");
    var avatar = $('<div>').attr("data-toggle", "tooltip")
            .attr("title", "")
            .addClass("being-avatar")
            .attr("data-original-title", being.displayName).tooltip();
    div.append(avatar);

    var defaultImage = "/images/species/" + being.species + ".svg";
    var image;
    if (being.avatar) {
        image = $('<img>').attr("src", being.avatar)
            .attr("onerror", 'this.onerror=null;this.src="' + defaultImage + '"');
    } else {
        image = $('<img>').attr("src", defaultImage);
    }

    var href;
    if (being.species == "human") {
        href = "/user/" + being.username;
    } else {
        href = "/individual/" + being.id;
    }

    var link = $('<a>').attr("href", href);
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
    configPromise.then( function() {
        $scope.user = app.user;
//        $scope.$digest();
    });

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
//                                label = "Whale: ";
                                label = item.species + ": ";
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

