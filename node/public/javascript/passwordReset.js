
function sendResetEmail(email) {
    if (!email || (email == '')) {
        alertplus.error("empty email");
        return false;
    }
    $('#password-button').hide();
    doResetEmail(email);
    return false;
}


function resetPassword(token, p1, p2) {
    if (!p1 || (p1 == "")) {
        alertplus.error("password empty");
        return false;
    }
    if (p1 != p2) {
        alertplus.error("passwords do not match");
        return false;
    }

    $('#password-button').hide();
    doResetPassword(token, p1);
    return false;
}


//actual work (after verifcation passed
function doResetEmail(email) {
    $.ajax({
        url: wildbookUrl + "/obj/user/sendpassreset",
        type: "POST",
        data: email,
        contentType: "text/plain"})
    .then(function() {
        $('#password-form').hide();
        $('#password-success').show();
    },
    function(ex) {
        alertplus.error(ex);
        $('#password-button').show();
    });
}

function doResetPassword(token, password) {
    $.ajax({
        url: wildbookUrl + "/obj/user/resetpass",
        type: "POST",
        data: JSON.stringify({token: token, password: password}),
        contentType: 'application/json'})
    .then(function() {
        $('#password-form').hide();
        $('#password-success').show();
    },
    function(ex) {
        alertplus.error(ex);
        $('#password-button').show();
    });
}
