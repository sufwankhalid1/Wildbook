wildbook.app.controller("MediaSubmissionController", function ($scope, $http, $q, $compile, $exceptionHandler) {
    $scope.panelList = [];

    $scope.encounters = [];
    $scope.surveyEncs = [];

    $scope.panels = {};

    function attachEncounter(survey) {
        if (! $scope.survey) {
            $scope.encounters.push(encounter);
            return;
        }

        // Call to add encounter to survey.
        $scope.survey;
        $http.post();
    }

    function getSurveyEncounter(surveypart, encounters) {
        return {surveypart: surveypart, encounters: encounters};
    }

    $scope.$on('survey_edit_done', function(event, surveypart) {
        $scope.surveyEncs.push(getSurveyEncounter(surveypart));
    });

    $scope.$on('survey_search_select', function(event, surveypart) {
        $scope.panels["survey_search"] = false;
        //
        // Look for any encounters attached to this survey already
        //
        $http("obj/survey/encounters/" + surveypart.track.surveyPartId)
        .then(function(result) {
            $scope.surveyEncs.push(getSurveyEncounter(surveypart, result.data));
        }, $exceptionHandler)
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
        .then(function(results) {
            $scope.submission = submission;
            $scope.photos = results[0].data;

            $scope.encounters = results[1].data.encounters || [];
            $scope.surveyEncs = results[1].data.surveyEncounters || [];
        }, $exceptionHandler);
    }

    $scope.deleteSubmission = function(submission) {
        return alertplus.confirm('Are you sure you want to delete the <b>entire</b> submission '+ submission.id +'?', "Delete Submission", true)
        .then(function() {
            $.ajax({
                url: "obj/mediasubmission/delete",
                type: "POST",
                data: JSON.stringify({submissionid: submission.id}),
                contentType: "application/json"
            })
            .then(function() {
                $scope.$apply(function() {
                    updateSubmissionData();
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
              width: 48,
              template: '<a href="javascript:;" ng-click="editSubmission(data)"><i class="glyphicon glyphicon-edit"></i></a>&nbsp;<a href="javascript:;" ng-click="deleteSubmission(data)"><i class="glyphicon glyphicon-trash"></i></a>'
             },
             {headerName: "Submitted",
              field: "timeSubmitted",
              cellRenderer: function(params) {
                  return $scope.timeToDate(params.value);
              },
              sort: 'desc'
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
        enableServerSideSorting: true,
        enableSorting: false,
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
        updateSubmissionData();
        $scope.submission = null;
    };

    var dataSource = {
        pageSize: 25,
        getRows: function(args) {
            if(args.sortModel) {
                $scope.rowData = sortSubmissionData(args.sortModel, $scope.rowData);
            }
            setTimeout(function() {
                args.successCallback($scope.rowData.slice(args.startRow, args.endRow), $scope.rowData.length);
            }, 100);
        }
    }

    function setDataSource() {
        $scope.msGridOptions.api.setDatasource(dataSource);
    }

    function updateSubmissionData() {
        $http({url:"obj/mediasubmission/get/status"})
        .then(function(result) {
            $scope.rowData = result.data;
            $scope.msGridOptions.api.setDatasource(dataSource);
        }, $exceptionHandler);
    }

    // Source: http://www.ag-grid.com/angular-grid-pagination/index.php
    function sortSubmissionData(sortModel, data) {
        // do an in memory sort of the data, across all the fields
        var resultOfSort = data.slice();
        resultOfSort.sort(function(a,b) {
            for (var k = 0; k<sortModel.length; k++) {
                var sortColModel = sortModel[k];
                var valueA = a[sortColModel.colId];
                var valueB = b[sortColModel.colId];

                // this filter didn't find a difference, move onto the next one
                if (valueA==valueB) {
                    continue;
                }

                var sortDirection = sortColModel.sort === 'asc' ? 1 : -1;
                if (valueA > valueB) {
                    return sortDirection;
                }
                else {
                    return sortDirection * -1;
                }
            }
            // no filters found a difference
            return 0;
        });
        return resultOfSort;
    }

    //
    // Finally, kick us off.
    //
    updateSubmissionData();
});
