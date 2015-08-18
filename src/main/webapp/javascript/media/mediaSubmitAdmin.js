angular.module('appWildbook', [])
.controller("MediaSubmissionController", function ($scope, $http, $compile) {
    function handleError(ex) {
        alertplus.error(ex);
    }

    $scope.deleteImage = function(row, id) {
        return alertplus.confirm('Are you sure you want to delete this image?', "Delete Image", true)
        .then(function() {
            $.ajax({
                url: "obj/mediasubmission/deletemedia",
                type: "POST",
                data: JSON.stringify({submissionid: $scope.submission.id, mediaid: id}),
                contentType: "application/json"
            })
            .then(function() {
                imageTable.row(row).remove().draw();
            }, handleError);
        });
    };

    $scope.deleteSubmission = function() {
        return alertplus.confirm('Are you sure you want to delete this <b>entire</b> submission?', "Delete Submission", true)
        .then(function() {
            $.ajax({
                url: "obj/mediasubmission/delete",
                type: "POST",
                data: JSON.stringify({submissionid: $scope.submission.id}),
                contentType: "application/json"
            })
            .then(function() {
                $scope.$apply(function() {
                    $scope.submission = null;
                })
                $scope.currentRow.remove().draw();
                $scope.currentRow = null;
            }, handleError);
        });
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
                      data: 'id',
                      title: 'Delete',
                      render: function(id, type, data, meta) {
                          //
                          // Can't get this to work. So resorting to click event below.
                          //
//                          var content = '<a href="javascript:;" ng-click="deleteImage('
//                              + meta.row
//                              + ", '"
//                              + id
//                              + '\')"><i class="glyphicon glyphicon-trash"></i></a>';
//                          content = "{{submission.id}}";
//                          //
//                          // Now compile this with angular so that ng-click is understood.
//                          //
//                          return $compile(content)($scope)[0].outerHTML;
                          return '<a href="javascript:;"><i class="glyphicon glyphicon-trash"></i></a>';
                      },
                      width: "32px"
                  },
                  {
                      data: 'thumbUrl',
                      title: 'Image',
                      render: function(thumb) {
                          // the ../ is for now until we get actual address?
                          return '<img width="50" src="../' + thumb + '">';
                      }
                  }
                  ]
    });

    $("#imagetable").on("click", "td", function(evt) {
        var cell = imageTable.cell(this);
        if (cell.index() && cell.index().column == 0) {
            $scope.deleteImage(cell.index().row,cell.data());
        }
    });

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
        $scope.currentRow = msTable.row(this);

        return $http({url:"obj/mediasubmission/photos/" + $scope.submission.id})
        .then(function(images) {
            imageTable.clear();
            imageTable.rows.add(images.data).draw();
        }, handleError);
    });

    $scope.doneEditing = function() {
        //
        // TODO: Finishing saving any unsaved changes to the media submission?
        //
        $scope.submission = null;
        $scope.currentRow = null;
    };

    //
    // Finally, kick us off.
    //
    return $http({url:"obj/mediasubmission/get/status"})
    .then(function(msResult) {
        msTable.rows.add(msResult.data).draw();
    }, handleError);
});
