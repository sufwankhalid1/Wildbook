angular.module('appWildbook', [])
.controller("MediaSubmissionController", function ($scope, $http) {
    function handleError(ex) {
        alertplus.error(ex);
    }

    var msTable = $("#mstable").DataTable({
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

    var imageTable = $("#imagetable").DataTable({
        columns: [
                  {
                      data: 'thumbUrl',
                      title: 'image',
                      render: function(thumb) {
                          // the ../ is for now until we get actual address?
                          return '<img width="50" src="../' + thumb + '">';
                      }
                  }
                  ]
    });

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
    .then(function(msResult) {
//        msTable.clear();
        msTable.rows.add(msResult.data).draw();

        //
        // TODO: I think we have to do something like this to get the rows to highlight as
        // we mouse over.
        //
    //        var lastRow = null;
    //        $('#mstable tbody')
    //        .on( 'mouseover', 'tr', function () {
    //            if (lastRow = )
    //            lastRow =
    //            $(msTable.row(this)).addClass('highlight');
    //            var colIdx = msTable.cell(this).index().column;
    //
    //            if ( colIdx !== lastIdx ) {
    //                $( msTable.cells().nodes() ).removeClass( 'pageableTable-visible' );
    //                $( msTable.column( colIdx ).nodes() ).addClass( 'pageableTable-visible' );
    //            }
    //        } )
    //        .on( 'mouseleave', function () {
    //            $( msTable.cells().nodes() ).removeClass( 'pageableTable-visible' );
    //        } );

        $("#mstable").on("click", "tr", function() {
            $scope.submission = msTable.row(this).data();
//          $scope.$digest();

            return $http({url:"obj/mediasubmission/photos/" + $scope.submission.id})
            .then(function(images) {
                imageTable.clear();
                imageTable.rows.add(images.data).draw();
            }, handleError);
        });
    }, handleError);
});
