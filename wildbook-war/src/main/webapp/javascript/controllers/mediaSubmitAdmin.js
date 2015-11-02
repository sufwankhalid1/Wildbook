wildbook.app.controller("MediaSubmissionController", function ($scope, $http, $q, $compile, $exceptionHandler) {
    $scope.panelList = [];
    $scope.panelList.push("encounter_edit");
    $scope.panelList.push("encounter_search");
    $scope.panelList.push("survey_edit");
    $scope.panelList.push("survey_search");

    $scope.encounters = [];
    $scope.surveyEncs = [];

    $scope.panels = {};

    function attachEncounter(encounter) {
        if (! $scope.surveypart) {
            $scope.encounters.push(encounter);
            return;
        }

        // Call to add encounter to survey.
        $http.post("obj/survey/addencounter",
                   {surveypartid: $scope.surveypart.track.surveyPartId, encounterid: encounter.id})
        .then(function() {
            addSurveyEncounters($scope.surveypart, [encounter]);
        });
    }

    function addSurveyEncounters(surveypart, encounters) {
        $scope.surveyEncs.push({surveypart: surveypart, encounters: encounters});
    }

    $scope.$on('survey_edit_done', function(event, surveypart) {
        addSurveyEncounters(surveypart);
    });

    $scope.$on('survey_search_select', function(event, surveypart) {
        $scope.panels["survey_search"] = false;
        //
        // Look for any encounters attached to this survey already
        //
        $http.get("obj/survey/encounters/" + surveypart.track.surveyPartId)
        .then(function(result) {
            addSurveyEncounters(surveypart, result.data);
        }, $exceptionHandler)
    });

    $scope.$on('encounter_edit_done', function(event, encounter) {
        if (encounter) {
            attachEncounter(encounter);
        }
    });

    $scope.$on('encounter_search_select', function(event, encounter) {
        $scope.panels["encounter_search"] = false;
        attachEncounter(encounter);
    });

    $scope.showPanel = function(panel, data) {
        this.panelList.forEach(function(value) {
            if (panel === value) {
                $scope.panels[value] = true;
                $scope.$broadcast(value, data);
            } else {
                $scope.panels[value] = false;
            }
        });
    };

    $scope.searchEncounter = function() {
        this.showPanel('encounter_search');
    }

    $scope.addEncounter = function(surveypart) {
        $scope.surveypart = surveypart;
        this.showPanel('encounter_edit');
    }

    $scope.addSurvey = function() {
        this.showPanel('survey_edit');
    }

    $scope.deleteImage = function(id) {
        return alertplus.confirm('Are you sure you want to delete this image?', "Delete Image", true)
        .then(function() {
            $http.post("obj/mediasubmission/deletemedia", {submissionid: $scope.submission.id, mediaid: id})
            .then(function() {
                $scope.zoomimage = null;
                $scope.photos = $scope.photos.filter(function(photo) {
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
            $scope.zoomimage = null;

            $scope.encounters = results[1].data.encounters || [];
            $scope.surveyEncs = results[1].data.surveyEncounters || [];
        }, $exceptionHandler);
    }

    $scope.deleteSubmission = function(submission) {
        return alertplus.confirm('Are you sure you want to delete the <b>entire</b> submission '+ submission.id +'?', "Delete Submission", true)
        .then(function() {
            $http.post("obj/mediasubmission/delete", {submissionid: submission.id})
            .then(function() {
                updateSubmissionData();
                $scope.submission = null;
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
        //enableSorting: false,
        // this pins it such that you can't scroll the page in that spot too
        // which when you have no scroll bar in the page is annoying because then no scrolling either way
        //pinnedColumnCount: 3,
        sortingOrder: ['desc', 'asc'],
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

    $scope.viewImage = function(photo) {
        $scope.zoomimage = photo;
    }

    $scope.doneEditing = function() {
        // Why do we need to update here?
        //updateSubmissionData();
        $scope.submission = null;
    };

    var dataSource = {
        pageSize: 20,
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
    // wb-key-handler-form
    //
    $scope.cancel = function() {
        if ($scope.zoomimage) {
            $scope.zoomimage = null;
            return;
        }
        $scope.doneEditing();
    }
    
    $scope.cmdEnter = function() {
        // do nothing
        // want this here to override any parent scope cmdEnter event though.
    }

    //
    // Finally, kick us off.
    //
    updateSubmissionData();
});
