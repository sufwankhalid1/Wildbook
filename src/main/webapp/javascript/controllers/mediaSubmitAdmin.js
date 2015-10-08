wildbook.app.controller("MediaSubmissionController", function ($scope, $http, $q, $compile, $exceptionHandler) {
    $scope.panelList = [];

    $scope.encounters = [];
    $scope.surveys = [];

    $scope.panels = {};

    function deleteRows(gridOptions, filter) {
        gridOptions.api.setRowData(gridOptions.rowData.filter(filter));
    }

    function attachEncounter(survey) {
        if (! $scope.survey) {
            $scope.encounters.push(encounter);
            return;
        }

        // Call to add encounter to survey.
        $scope.survey;
        $http.post();
    }

    $scope.$on('survey_edit_done', function(event, survey) {
        $scope.surveys.push(survey);
    });

    $scope.$on('survey_search_select', function(event, survey) {
        $scope.panels["survey_search"] = false;
        $scope.surveys.push(survey);
    });

    $scope.$on('encounter_edit_done', function(event, encounter) {
        attachEncounter();
    });

    $scope.$on('encounter_search_select', function(event, encounter) {
        $scope.panels["encounter_search"] = false;
        attachEncounter();
    });

    $scope.showPanel = function(panel, data) {
        this.panelList.forEach(function(value) {
            if (panel === value) {
                $scope.$broadcast(value, data);
            } else {
                $scope.$broadcast(value, false);
            }
        });
    };

    $scope.searchEncounter = function(survey) {
        $scope.survey = survey;
        this.showPanel('encounter_search');
    }

    $scope.addEncounter = function(survey) {
        $scope.survey = survey;
        this.showPanel('encounter_edit');
    }

    $scope.addSurvey = function() {
        this.showPanel('survey_edit');
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

            $scope.encounters = results[1].encounters || [];
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
        // When everything is ready, initialize tooltips on the page
        $('[data-toggle="tooltip"]').tooltip();
    }, $exceptionHandler);
});
