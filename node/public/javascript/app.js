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
var ngApp = angular.module('nodeApp', ['nodeApp.config', 'nodeApp.controllers']);

function handleError(jqXHR) {
    alertplus.error(jqXHR);
}

var app = {};

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

    var defaultImage = "/images/species/" + being.species.code + ".svg";
    var image;
    if (being.avatar) {
        image = $('<img>').attr("src", being.avatar)
            .attr("onerror", 'this.onerror=null;this.src="' + defaultImage + '"');
    } else {
        image = $('<img>').attr("src", defaultImage);
    }

    var href;
    if (being.species.code == "homo_sapien") {
        href = "/user/" + being.id;
    } else {
        href = "/individual/" + being.id;
    }

    var link = $('<a>').attr("href", href);
    link.append(image);
    avatar.append(link);

    return div;
}

app.toMoment = function(date) {
//    var dateString = date.year + '-' + date.monthValue + '-' + date.dayOfMonth;
//    return moment(dateString, 'YYYY-M-D');
    return moment({year: date[0], month: date[1], day: date[2]});
}

function configSearchBox() {
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
                url: app.config.wildbook.proxyUrl + "/search/site",
                dataType: "json",
                data: {
                    term: request.term
                },
                success: function( data ) {
                    var res = $.map(data, function(item) {
                        var label;
                        if (item.type == "individual") {
                            label = item.species.name + ": ";
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

    //this hides the no results message when the user leaves search field
    $('#search-site').on('blur', function() { $('#search-help').hide(); });
}

angular.module('nodeApp.controllers', ['nodeApp.config'])
.controller('AppController', ['$scope', '$http', 'configFactory', function ($scope, $http, config) {
    $scope.user = null;
    $scope.showlogin = false;
    config.getConfig().then(function(configData) {
        app.config = configData;
        $http.get(app.config.wildbook.url + '/obj/user/isloggedin', {withCredentials: true})
        .then(function(res) {
            $scope.user = res.data;
        });

        //setup map tool
        if(typeof maptool !== 'undefined') {
            maptool.init(configData.maptool);
        }
        //setup locale, tooltips, searchbox:
        moment.locale(window.navigator.userLanguage || window.navigator.language || 'en');

        $('[data-toggle="tooltip"]').tooltip();

        configSearchBox();

        $scope.logout = function() {
            $http({url: app.config.wildbook.url + "/LogoutUser", withCredentials: true})
            .then(function() {
                $scope.user = null;
                setTimeout(function(){$scope.$apply();});
            })
        };

        $scope.terms = function() {
            $http.get("/termsModal")
            .then(function(terms) {
                alertplus.alert(terms.data, null, "Usage Agreement");
            });
        };

        $scope.showloginmodal = function(username) {
            $scope.showlogin = true;
            $scope.$broadcast('loginEvent', { username: username });
        };
    });
}])
.controller("LoginController", ['$scope', '$http', 'configFactory', function($scope, $http, config) {
    $scope.loginForm = {
        username: null,
        password: null,
        error: null
    };
    $scope.resetForm = {
        email: null
    };
    $scope.reset = {
        on: false,
        sent: false
    };
    $scope.login = function(event) {
        if((event && event.keyCode != 13)
            || !$scope.loginForm.username) {
            return;
        }
        $http.post(app.config.wildbook.url + '/obj/user/login',
        {
            username: $scope.loginForm.username,
            password: $scope.loginForm.password
        },
        {
            withCredentials: true,
            contentType: 'application/json'
        })
        .then(function(res) {
            $scope.$parent.user = res.data;
            $scope.$parent.showlogin = false;
        }, function(err) {
            var message = "Invalid Username or Password.";
            if(err.data.message) {
                if(err.data.message.indexOf('username') > 0) {
                    message = 'We do not have an account for email ' + $scope.loginForm.username + '. To create an account, please submit media.';
                }
                if(err.data.message.indexOf('credentials') > 0) {
                    message = "Please retype your password.";
                }
            }
            $scope.loginForm.error = message;
        });
    };
    $scope.sendreset = function(event) {
        if((event && event.keyCode != 13)
            || !$scope.resetForm.email
            || !$scope.reset.on) {
            return;
        }
        $http.post(app.config.wildbook.url + '/obj/user/sendpassreset', $scope.resetForm.email, { contentType: 'text/plain' })
        .then(function() {
            $scope.reset.on = false;
            $scope.reset.sent = true;
        }, function(err){
            if(err.data.message) {
                $scope.reset.error = "The email address doesn't exist. To create an account please upload some images.";
            }
        });
    }
    $scope.loginValidClass = function() {
        return $scope.loginForm.username && $scope.loginForm.password ? '' : 'disabled';
    }
    $scope.resetValidClass = function() {
        return $scope.resetForm.email ? '' : 'disabled';
    }
    $scope.closemodal = function() {
        $scope.$parent.showlogin = false;
        $scope.reset.on = false;
        $scope.reset.sent = false;
    }
    $scope.closemodalDone = function() {
        if($scope.reset.sent) {
            $scope.closemodal();
        }
    }

    //AppController may broadcast some information to LoginController from other forms
    $scope.$on('loginEvent', function(event, data) {
        $scope.loginForm.username = data.username;
        $scope.reset.on = false;
        $scope.reset.sent = false;
        $scope.loginForm.error = '';
        $scope.reset.error = '';
    });
}])
.controller("IndividualController", ['$scope', '$http', 'configFactory', function(scope, http, config) {
    config.getConfig().then(function(configData) {
        individualPage.init(config, photos, encounters);
    });
}])
.controller("ResetController", ['$scope', '$http', '$attrs', function($scope, http, attrs) {
    $scope.form = {
        password: null,
        password2: null,
        token: attrs.token
    }
    $scope.passwordError = null;
    $scope.reset = function(event) {
        if(event && event.keyCode != 13) {
            return;
        }
        if($scope.form.password && $scope.form.password2 && $scope.form.token) {
            http.post(app.config.wildbook.url + '/obj/user/resetpass',
                { token: $scope.form.token, password: $scope.form.password },
                { contentType: 'application/json'})
            .then(function() {
                $scope.done = true;
            }, function(res) {
                $scope.passwordError = 'Something went wrong. Please try again.';
            });
        }
    };
    $scope.verifyPasswordForm = function() {
        return !!$scope.form.password && $scope.form.password === $scope.form.password2 ? '' : 'disabled';
    }
}]);

angular.module("nodeApp.config", [])
.factory('configFactory', ['$http', '$q', function($http, $q) {
    var defConfig = $q.defer();
    $http.get('/config')
    .success(function(res) {
        return defConfig.resolve(res);
    })
    .error(function(err){
        console.log('ng error getting config');
    });
    return {
        getConfig: function() {
            return defConfig.promise;
        }
    };
}])
.directive('focusMe', function($timeout, $parse) {
    return {
        scope: true,
        link: function(scope, element, attrs) {
          var model = $parse(attrs.focusMe);
          scope.$watch(model, function(value) {
            if(value === true) {
              $timeout(function() {
                element[0].focus();
              });
            }
          });
        }
      };
})