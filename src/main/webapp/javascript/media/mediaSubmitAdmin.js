$(document).ready(function() {
    $.get("obj/mediasubmission/get/status")
    .then(function(data) {
        var table = $("#mstable").DataTable({
            data: data,
            columns: [
                {
                    data: 'id',
                    title: 'Edit',
                    render: ''
                },
                {
                    data: 'timeSubmitted',
                    title: 'Submitted',
                    render: function(timeSubmitted) {
                        return moment(timeSubmitted).format('lll');
                    }
                },
                {
                    data: 'user',
                    title: 'Submitted By',
                    render: function(user) {
                        if (user) {
                            return user.displayName;
                        }
                        return null;
                    }
                },
                {
                    data: 'submissionid',
                    title: "Survey ID"
                },
                {
                    data: 'description',
                    title: "Description"
                },
                {
                    data: 'verbatimLocation',
                    title: "Location"
                },
                {
                    data: 'status',
                    title: 'Status'
                }
                ]
        });

//        var lastRow = null;
//        $('#mstable tbody')
//        .on( 'mouseover', 'tr', function () {
//            if (lastRow = )
//            lastRow =
//            $(table.row(this)).addClass('highlight');
//            var colIdx = table.cell(this).index().column;
//
//            if ( colIdx !== lastIdx ) {
//                $( table.cells().nodes() ).removeClass( 'pageableTable-visible' );
//                $( table.column( colIdx ).nodes() ).addClass( 'pageableTable-visible' );
//            }
//        } )
//        .on( 'mouseleave', function () {
//            $( table.cells().nodes() ).removeClass( 'pageableTable-visible' );
//        } );

        $("#mstable").on("click", "tr", function() {
            var data = table.row(this).data();

            //
            // TODO: Trigger John's code for editing a mediasubmission
            //
            console.log(data);
        });
    }, function(error) {
        alertplus.error(error);
    });
});
