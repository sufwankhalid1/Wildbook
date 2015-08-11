if (typeof wildbook == 'undefined') wildbook = {};

wildbook.auth = {
    isLoggedIn: function() {
        return false;
    },

    user: function() {
        return null;
    },

    login: function() {
        if (this.isLoggedIn()) return true;
console.log('do login thing');
    },

    loginPopup: function(ngScope, username, title, details, redirectUrl) {
/*
        var d = $('#login-popup');
        if (!d.length) {
            d = $('<div id="login-popup">').css("display", "none").addClass("alertplus");
        }
*/
        if (!username) username = '';
        var h = '<div><input id="login-username" value="' + username + '" placeholder="username" /></div><div><input id="login-password" type="password" placeholder="password" /></div>';
        if (redirectUrl) h += '<input id="login-redirectUrl" type="xhidden" value="' + redirectUrl + '" />';
        h += '<div style="border-bottom: 2px solid black; margin-bottom: 10px; padding-bottom: 10px;"><input class="login-button btn" type="button" value="login" onClick="wildbook.auth.loginPopupTry()" /><span style="margin-left: 10px; color: #900;" id="login-message"></span><input class="login-button btn" type="button" value="forgot password" style="float: right;" onClick="window.location.href=\'/spPasswordReset?email=\' + $(\'#login-username\').val();" /></div>';
        if (!title) title = 'Login';
        alertplus.alert(h, details, title);
        $('.alertplus .btn-primary').html('Cancel');
    },

    loginPopupTry: function() {
        $('#login-message').html('');
        var username = $('#login-username').val();
        var password = $('#login-password').val();
        if (!username || !password) return;
        var redirectUrl = $('#login-redirectUrl').val();
        $('.alertplus .login-button').prop('disabled', 'disabled').css('opacity', 0.5);
        var args = {
            complete: function(x, status) {
                var resp = {};
                if (status != 'success') {
                    resp.success = false;
                    resp.error = 'loginAjax() ajax call returned status != success';
                } else {
                    resp = x.responseJSON;
                }
                console.info(resp);
                if (resp.success) {
                    //$('#login-popup').dialog('close');
                    $('.alertplus .btn-primary').click();  //hacktacular way to close popup dialog???
                    if (redirectUrl) {
                        window.location.href = redirectUrl;
                    } else {
                        wildbook.auth.updateAngularUserDiv(resp);
                    }
                } else {
                    console.error('login failure: ' + resp.error);
                    $('#login-message').html('failed');
                    $('.alertplus .login-button').prop('disabled', false).css('opacity', 1);
                }
            }
        };
        this.loginAjax(username, password, args);
    },

    loginAjax: function(username, password, args) {
        if (!args) args = {};
        args.url = wildbookGlobals.baseUrl + '/LoginUser?json=true&username=' + username + '&password=' + password;  //TODO https!
        args.type = 'GET';
        args.dataType = 'json';
        if (!args.complete) args.complete = function(a,b,c) { console.info('loginAjax return: %o %o %o',a,b,c); };
        return $.ajax(args);
    },


    //apparently updating angular from outside of angular is pretty hactacular.  :/
    //  https://stackoverflow.com/questions/22942509/angularjs-update-input-manually-does-not-trigger-change-in-the-model
    updateAngularUserDiv: function(u) {
        if (!angular) return;
        var el = document.getElementById('div-user-info');
        if (el) el = angular.element(el);
        if (!el) return;
        var scope = el.scope();
        if (!scope) return;
        scope.user = u;
        scope.$digest();
    }
};

