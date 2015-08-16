$(document).ready(function() {
    $.get("obj/mediasubmission/get/status")
    .then(function(data) {
        $("#mstable").DataTable({
            data: data,
//            columnDefs: [
//                {
//                    render: function ( data, type, row ) {
//                        return data.displayName;
//                    },
//                    targets: 1
//                }
//            ]
//        ,
            columns: [
                      {data: 'id'},
//                      {data: 'user.displayName'},
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
