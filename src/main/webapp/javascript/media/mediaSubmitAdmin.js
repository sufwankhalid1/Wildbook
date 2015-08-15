$(document).ready(function() {
    $.get("obj/mediasubmission/get/status")
    .then(function(data) {
        $("#mstable").DataTable({
            data: data,
            columns: [
                      { data: 'id' },
                      { data: 'username' },
                      { data: 'timeSubmitted' }
                  ]
        });
    }, function(error) {
        alertplus.error(error);
    });
});
