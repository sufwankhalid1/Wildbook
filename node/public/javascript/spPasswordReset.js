
function sendResetEmail(email) {
    console.log(email);
    if (!email || (email == '')) {
        alertplus.error("empty email");
        return false;
    }
    $('#password-button').hide();
    doResetEmail(email);
    return false;
}


function resetPassword(token, p1, p2) {
    console.log('token %s passwords(%s,%s)', token, p1, p2);
    if (!p1 || (p1 == "")) {
        alertplus.error("password empty");
        return false;
    }
    if (p1 != p2) {
        alertplus.error("passwords do not match");
        return false;
    }
////////TODO other password checks! (to fulfill Stormpath)
    $('#password-button').hide();
    doResetPassword(token, p1);
    return false;
}


//actual work (after verifcation passed
function doResetEmail(email) {
    $.ajax({
        url: wildbookUrl + "/PasswordReset?email=" + encodeURIComponent(email),
        type: 'GET',
        dataType: 'json',
        success: function(d) {
console.log(d);
            if (d.success) {
                $('#password-form').hide();
                $('#password-success').show();
            } else {
                alertplus.error(d.error);
                $('#password-button').show();
            }
        },
        error: function(x) {
console.log('error %o', x);
            alertplus.error("error: " + x);
            $('#password-button').show();
        }
    });
}

function doResetPassword(token, password) {
    $.ajax({
        url: wildbookUrl + "/PasswordReset",
        type: 'POST',  //using POST here so it will not log password in access_log
        data: {token: token, password: password},
        dataType: 'json',
        success: function(d) {
console.log(d);
            if (d.success) {
                $('#password-form').hide();
                $('#password-success').show();
            } else {
                alertplus.error(d.error);
                $('#password-button').show();
            }
        },
        error: function(x) {
console.log('error %o', x);
            alertplus.error("error: " + x);
            $('#password-button').show();
        }
    });
}
