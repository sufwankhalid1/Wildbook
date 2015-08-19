angular.module('appWildbook', ["angularGrid"])
angular.module('appWildbook').controller("MediaSubmissionController", function ($scope, $http, $compile) {
    $scope.viewImage = function(url) {
        $scope.zoomimage = url;
    }

    function handleError(ex) {
        alertplus.error(ex);
    }

    function deleteRows(gridOptions, filter) {
        gridOptions.rowData = gridOptions.rowData.filter(filter);
        gridOptions.api.onNewRows();
    }

    $scope.sendForID = function(id) {
        //
        // TODO: Send photo to service and update row to show this somehow.
        //
        alertplus.alert("TODO: Send photo [" + id + "] to ID service.");
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
                 headerName: "",
                 field: "id",
                 width: 24,
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
                 headerName: '',
                 template: '<a href="javascript:;" ng-click="deleteImage(data.id)"><i class="glyphicon glyphicon-trash"></i></a>',
                 width: 24
             },
             {
                 field: 'id',
                 headerName: '',
                 template: '<a href="javascript:;" ng-click="sendForID(data.id)"><i class="glyphicon glyphicon-send"></i></a>',
                 width: 24
             },
             {
                 field: 'thumbUrl',
                 headerName: 'Image',
                 template: '<a href="javascript:;" ng-click="viewImage(data.url)"><img width="50px" src="..{{data.thumbUrl}}"></a>',
                 width: 60
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
