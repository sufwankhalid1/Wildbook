if (typeof wildbook == 'undefined') wildbook = {};

wildbook.auth = (function() {
    function loginAttempt(baseUrl, username, title, message) {
        var dialog = '<div><input id="login-username" value="'
            + (username || '')
            + '" placeholder="username/email" /></div><div><input id="login-password" type="password" placeholder="password" /></div>'
            + '<div style="border-bottom: 2px solid black; margin-bottom: 10px; padding-bottom: 10px;"><span style="margin-left: 10px; color: #900;">'
            + (message || '')
            + '</span><input class="login-button btn" type="button" value="forgot password" style="float: right;" onClick="window.location.href=\'/spPasswordReset?email=\' + $(\'#login-username\').val();" /></div>';
        return alertplus.confirm(dialog, title || "Login")
        .then(function() {
            var username = $('#login-username').val();
            var password = $('#login-password').val();

            app.wait.show();
            return $.ajax({url: baseUrl + '/obj/user/login',
                           type: "POST",
                           data: JSON.stringify({username: username, password: password}),
                           contentType: "application/json",
                           xhrFields: {withCredentials: true}})
            .then(function(response) {
                app.wait.hide();
                return response;
            },
            function(ex) {
                app.wait.hide();
                return loginAttempt(baseUrl, username, title, ex.responseJSON.message);
            });
        });
    }

    return {login: loginAttempt};
})();

