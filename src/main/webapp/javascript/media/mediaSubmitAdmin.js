angular.module('appWildbook', ["ui.bootstrap"])

angular.module('appWildbook').controller('ModalZoomImageCtrl', function ($scope, $modalInstance, zoomimage) {
    $scope.zoomimage = zoomimage;
    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
  });

angular.module('appWildbook').controller("MediaSubmissionController", function ($scope, $modal, $http, $compile) {
    //
    // See Modal section of http://angular-ui.github.io/bootstrap/ if you want to expand on this modal
    // code. Usage here is *extremely* basic.
    //
    $scope.thumbZoom = function(zoomimage) {
        var modalInstance = $modal.open({
            animation: false,
            templateUrl: 'zoomimage.html',
            controller: 'ModalZoomImageCtrl',
            size: "lg",
            resolve: {
                zoomimage: function () {
                    return zoomimage;
                }
            }
        });
    };

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
                row.remove().draw();
            }, handleError);
        });
    };

    $scope.deleteSubmission = function() {
        return alertplus.confirm('Are you sure you want to delete the <b>entire</b> submission?', "Delete Submission", true)
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
                    $scope.currentRow.remove().draw();
                    $scope.currentRow = null;
                })
            }, handleError);
        });
    }

    var msTable = $("#mstable").DataTable({
        select: {style: "os"},
        columns: [
                  {
                      data: 'id',
                      title: 'Delete',
                      render: function(id, type, data, meta) {
                          return '<a href="javascript:;"><i class="glyphicon glyphicon-edit"></i></a>';
                      },
                      width: "32px"
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
                          return '<a href="javascript:;"><img width="50" src="..' + thumb + '"></a>';
                      }
                  }
                  ]
    });

    $("#imagetable").on("click", "td", function(evt) {
        //
        // To get row info from cell
        //    imageTable.row(imageTable.cell(this).index().row).data();
        //
        // To get the number of rows in the table you can do.
        //    imageTable.data().length
        //
        var cell = imageTable.cell(this);
        if (!cell.index()) {
            return;
        }

        var rowindex = cell.index().row;
        var row = imageTable.row(rowindex);
        switch (cell.index().column) {
        case 0:
            $scope.deleteImage(row, cell.data());
            break;
        case 1:
            $scope.$apply(function() {
                $scope.thumbZoom(row.data().url);
            });
            break;
        }
    });

    $("#mstable").on("click", "td", function() {
        var cell = msTable.cell(this);
        if (cell.index() && cell.index().column == 0) {
            $scope.currentRow = msTable.row(cell.index().row);
            $scope.submission = $scope.currentRow.data();
            //
            // This is the code if we add the click event to the <tr> element.
            //
            //$scope.currentRow = msTable.row(this);

            return $http({url:"obj/mediasubmission/photos/" + $scope.submission.id})
            .then(function(images) {
                imageTable.clear();
                imageTable.rows.add(images.data).draw();
            }, handleError);
        }
    });

    $scope.doneEditing = function() {
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
