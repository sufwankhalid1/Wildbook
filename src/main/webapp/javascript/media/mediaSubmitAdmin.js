$(document).ready(function() {
    $.get("obj/mediasubmission/get/status")
    .then(function(data) {
        $("#mstable").DataTable({
            data: data,
            columns: [
                      {data: 'id'},
                      {data: 'user',
                       render: function(user) {
                          if (user) {
                              return user.displayName;
                          }
                          return null;
                      }},
                      {data: 'timeSubmitted',
                       render: function(timeSubmitted) {
                           return moment(timeSubmitted).format('lll');
                       }}
                  ]
        });
    }, function(error) {
        alertplus.error(error);
    });
});
