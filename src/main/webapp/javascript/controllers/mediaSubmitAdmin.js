wildbook.app.controller("MediaSubmissionController", function ($scope, $http, $q, $compile, $exceptionHandler) {
    var panels = ['survey_search', 'survey_edit', 'encounter_search', 'encounter_edit', 'encounter_view'];

    $scope.encounters = [];
    $scope.surveys = [];
    $scope.panel = {};

//    $scope.viewImage = function(url) {
//        $scope.zoomimage = url;
//        $scope.encounter = null;
//    }

    function deleteRows(gridOptions, filter) {
        gridOptions.api.setRowData(gridOptions.rowData.filter(filter));
    }

    $scope.$on('survey_edit_done', function(event, survey) {
        $scope.showPanel(null);
        $scope.surveys.push(survey);
    });

    $scope.$on('survey_search_done', function(event, survey) {
        $scope.showPanel(null);
        $scope.surveys.push(survey);
    });

    $scope.showPanel = function(panel) {
        panels.forEach(function(value) {
            $scope.panel[value] = (panel == value);
        });
    };

//    $scope.editEncounter = function(encounter) {
//        $scope.zoomimage = null;
//        $scope.encounter = encounter;
//    }
//
//    $scope.createEncounter = function() {
//        //
//        // TODO: Before willy-nilly creating an encounter from this data we should first
//        // see if there are any other encounters for this individual, date, and location
//        // and use this one instead.
//        //
//        var encounter = {
//                verbatimLocation: $scope.submission.verbatimLocation,
//                encdate: $scope.submission.startTime,
//                latitude: $scope.submission.latitude,
//                longitude: $scope.submission.longitude
//                };
//        $scope.encounters.push(encounter);
//        editEncounter(encounter);
//    }

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
                $scope.photos = photos.filter(function(photo) {
                    return (photo.id !== id);
                });
            }, $exceptionHandler);
        });
    }

    $scope.editSubmission = function(submission) {
        return $q.all([$http({url:"obj/mediasubmission/photos/" + submission.id}),
                       $http({url:"obj/mediasubmission/encounters/" + submission.id})])
//                       $q.resolve({encounters: [{individual: {displayName: 'test'}, encdate: '2009-12-13'}]})])
        .then(function(results) {
            $scope.submission = submission;
            $scope.photos = results[0].data;

            $scope.encounters = results[1].encounters;
        }, $exceptionHandler);
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
            }, $exceptionHandler);
        });
    };

    $scope.timeToDate = function(time) {
        return moment(time).format('lll');
    };

    $scope.msGridOptions = {
        columnDefs:
            [{headerName: "",
              field: "id",
              width: 24,
              template: '<a href="javascript:;" ng-click="editSubmission(data)"><i class="glyphicon glyphicon-edit"></i></a>'
             },
             {headerName: "Submitted",
              field: "timeSubmitted",
              cellRenderer: function(params) {
                  return $scope.timeToDate(params.value);
              }
             },
             {headerName: "Submitted By",
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
             {headerName: "Status", field: "status"}],
        rowData: null,
        enableSorting: true,
        pinnedColumnCount: 3,
        angularCompileRows: true
    };

//    $scope.imageGridOptions = {
//        columnDefs:
//            [
//             {
//                 field: 'id',
//                 headerName: '',
//                 template: '<a href="javascript:;" ng-click="deleteImage(data.id)"><i class="glyphicon glyphicon-trash"></i></a>',
//                 width: 24
//             },
//             {
//                 field: 'id',
//                 headerName: '',
//                 template: '<a href="javascript:;" ng-click="sendForID(data.id)"><i class="glyphicon glyphicon-send"></i></a>',
//                 width: 24
//             },
//             {
//                 field: 'thumbUrl',
//                 headerName: 'Image',
//                 template: '<a href="javascript:;" ng-click="viewImage(data.url)"><img width="50px" src="{{data.thumbUrl}}"></a>',
//                 width: 60
//             }
//             ],
//             rowData: null,
//             rowHeight: 50,
//             enableSorting: true,
//             angularCompileRows: true
//    };


    $scope.doneEditing = function() {
//        $scope.zoomimage = null;
        $scope.submission = null;
    };

    //
    // Finally, kick us off.
    //
    return $http({url:"obj/mediasubmission/get/status"})
    .then(function(result) {
        $scope.msGridOptions.api.setRowData(result.data);
    }, $exceptionHandler);
});

wildbook.app.controller("EncounterFormController", function($scope) {
});

wildbook.app.directive('locationEdit', function() {
    return {restrict: 'E',
        scope: {
            location: '='
        },
        templateUrl: 'util/render?j=partials/location_edit.jade'
    };
});
