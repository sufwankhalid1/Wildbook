angular.module('appWildbook', [])
.controller("MediaSubmissionController", function ($scope, $http) {
    $scope.doneEditing = function() {
        //
        // TODO: Finishing saving any unsaved changes to the media submission?
        //
        $scope.submission = null;
    }


    //
    // Finally, kick us off.
    //
    return $http({url:"obj/mediasubmission/get/status"})
    .then(function(result) {
        var table = $("#mstable").DataTable({
            data: result.data,
            columns: [
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
            $scope.submission = table.row(this).data();
            $scope.$digest();
        });
    }, function(error) {
        alertplus.error(error);
    });
});
