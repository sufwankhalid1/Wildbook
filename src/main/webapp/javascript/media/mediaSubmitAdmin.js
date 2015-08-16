$(document).ready(function() {
    $.get("obj/mediasubmission/get/status")
    .then(function(data) {
        $("#mstable").DataTable({
            data: data,
            columns: [
                      {data: 'id',
                       title: 'ID'},
                      {data: 'user',
                       title: 'Submitted By',
                       render: function(user) {
                          if (user) {
                              return user.displayName;
                          }
                          return null;
                      }},
                      {data: 'timeSubmitted',
                       title: 'Date',
                       render: function(timeSubmitted) {
                           return moment(timeSubmitted).format('lll');
                       }},
                       {data: 'status',
                        title: 'Status'}
                  ]
        });
    }, function(error) {
        alertplus.error(error);
    });
});
