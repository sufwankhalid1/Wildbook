angular.module('appWildbook', ["ui.bootstrap", "angularGrid"])

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
    $scope.viewImage = function(url) {
        var modalInstance = $modal.open({
            animation: false,
            templateUrl: 'zoomimage.html',
            controller: 'ModalZoomImageCtrl',
            size: "lg",
            resolve: {
                zoomimage: function () {
                    return url;
                }
            }
        });
    }

    function handleError(ex) {
        alertplus.error(ex);
    }

    function deleteRows(gridOptions, filter) {
        gridOptions.rowData = gridOptions.rowData.filter(filter);
        gridOptions.api.onNewRows();
    }

    $scope.deleteImage = function(id) {
        return alertplus.confirm('Are you sure you want to delete this image?', "Delete Image", true)
        .then(function() {
            $.ajax({
                url: "obj/mediasubmission/deletemedia",
                type: "POST",
                data: JSON.stringify({submissionid: $scope.submission.id, mediaid: id}),
                contentType: "application/json"
            })
            .then(function() {
                deleteRows($scope.imageGridOptions, function(value) {
                    return (value.id !== id);
                })
            }, handleError);
        });
    }

    $scope.editSubmission = function(submission) {
        return $http({url:"obj/mediasubmission/photos/" + submission.id})
        .then(function(result) {
            $scope.submission = submission;
            $scope.imageGridOptions.rowData = result.data;
            $scope.imageGridOptions.api.onNewRows();
        }, handleError);
    }


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
                    deleteRows($scope.msGridOptions, function(value) {
                        return (value.id !== $scope.submission.id);
                    });

                    $scope.submission = null;
                })
            }, handleError);
        });
    }


    $scope.msGridOptions = {
        columnDefs:
            [
             {
                 headerName: "Edit",
                 field: "id",
                 width: 32,
                 template: '<a href="javascript:;" ng-click="editSubmission(data)"><i class="glyphicon glyphicon-edit"></i></a>'
             },
             {
                 headerName: "Submitted",
                 field: "timeSubmitted",
                 cellRenderer: function(params) {
                     return moment(params.value).format('lll');
                 }
             },
             {
                 headerName: "Submitted By",
                 field: "user",
                 cellRenderer: function(params) {
                     if (params.value) {
                         return params.value.displayName;
                     }
                     return null;
                 }
             },
             {headerName: "Survey ID", field: "submissionid"},
             {headerName: "Description", field: "description"},
             {headerName: "Location", field: "verbatimLocation"},
             {headerName: "Status", field: "status"}
             ],
        rowData: null,
        enableSorting: true,
        pinnedColumnCount: 3,
        angularCompileRows: true
    };

    $scope.imageGridOptions = {
        columnDefs:
            [
             {
                 field: 'id',
                 headerName: 'Delete',
                 template: '<a href="javascript:;" ng-click="deleteImage(data.id)"><i class="glyphicon glyphicon-trash"></i></a>',
                 width: 32
             },
             {
                 field: 'thumbUrl',
                 headerName: 'Image',
                 template: '<a href="javascript:;" ng-click="viewImage(data.url)"><img width="50px" src="..{{data.thumbUrl}}"></a>'
             }
             ],
             rowData: null,
             rowHeight: 50,
             enableSorting: true,
             angularCompileRows: true
    };


    $scope.doneEditing = function() {
        $scope.submission = null;
    };

    //
    // Finally, kick us off.
    //
    return $http({url:"obj/mediasubmission/get/status"})
    .then(function(result) {
        $scope.msGridOptions.rowData = result.data;
        $scope.msGridOptions.api.onNewRows();
    }, handleError);
});
