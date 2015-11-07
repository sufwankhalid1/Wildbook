wildbook.app.directive("wbMediaSubmissionAdmin",
    ["$http", "$q", "$exceptionHandler", "wbDateUtils", function ($http, $q, $exceptionHandler, wbDateUtils) {
        return {
            restrict: 'E',
            scope: {},
            templateUrl: 'util/render?j=partials/media_submission_admin',
            replace: true,
            controller: function($scope) {
                $scope.encounters = [];
                $scope.surveyEncs = [];
                $scope.activeEncounter = null;
            
                function attachEncounter(encounter) {
                    if (! encounter) {
                        return;
                    }
                    
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
            
                $scope.dateStringFromRest = function(date) {
                    return wbDateUtils.dateStringFromRest(date);
                }
            
                $scope.editEncounterDone = function(encounter) {
                    $scope.encounterToEdit = null;
                    if (encounter) {
                        attachEncounter(encounter);
                    }
                }
                
                $scope.searchEncounter = function() {
                    $scope.encounterToEdit = null;
                    // This one
                    $scope.encounterSearch = true;
                    $scope.surveyToEdit = null;
                    $scope.surveySearch = false;
              }
                
                $scope.searchEncounterDone = function(encounter) {
                    $scope.encounterSearch = false;
                    attachEncounter(encounter);
                }
                
                $scope.addEncounter = function(surveypart) {
                    $scope.surveypart = surveypart;
                    // This one
                    $scope.encounterToEdit = "new";
                    $scope.encounterSearch = false;
                    $scope.surveyToEdit = null;
                    $scope.surveySearch = false;
                }
                
                function editEncounter(encounter) {
                    // This one
                    $scope.encounterToEdit = encounter;
                    $scope.encounterSearch = false;
                    $scope.surveyToEdit = null;
                    $scope.surveySearch = false;
                }
                
                $scope.selectEncounter = function(encounter) {
                    $scope.activeEncounter = encounter;
                    editEncounter(encounter);
                }
                
                $scope.searchSurvey = function() {
                    $scope.encounterToEdit = null;
                    $scope.encounterSearch = false;
                    $scope.surveyToEdit = null;
                    // This one
                    $scope.surveySearch = true;
                }

                $scope.addSurvey = function() {
                    $scope.encounterToEdit = null;
                    $scope.encounterSearch = false;
                    // This one
                    $scope.surveyToEdit = "new";
                    $scope.surveySearch = false;
                }
                
                $scope.editSurveyDone = function(surveypart) {
                    $scope.surveyToEdit = null;
                    addSurveyEncounters(surveypart);
                }
                
                $scope.searchSurveyDone = function(surveypart) {
                    $scope.surveySearch = false;
                    if (! surveypart) {
                        return;
                    }
                    //
                    // Look for any encounters attached to this survey already
                    //
                    $http.get("obj/survey/encounters/" + surveypart.track.surveyPartId)
                    .then(function(result) {
                        addSurveyEncounters(surveypart, result.data);
                    }, $exceptionHandler)
                }

                $scope.deletePhoto = function(id) {
                    return $http.post("obj/mediasubmission/deletemedia", {submissionid: $scope.submission.id, mediaid: id})
                                .catch($exceptionHandler);
                }
                
                $scope.addPhotos = function(photos) {
                    if (!$scope.activeEncounter) {
                        alertplus.alert("No active encounter selected.");
                        return;
                    }
                    
                    $http.post("obj/encounter/addmedia/" + $scope.activeEncounter.id, photoids)
                    .then(function() {
                        if (! $scope.activeEncounter.photos) {
                            $http.get("obj/encounter/getmedia/" + $scope.activeEncounter.id)
                            .then(function(data) {
                                $scope.activeEncounter.photos = data;
                                $scope.activeEncounter.photos.push(photos);
                            });
                        } else {
                            $scope.activeEncounter.photos.push(photos);
                        }
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
            }
        }
    }]
);
