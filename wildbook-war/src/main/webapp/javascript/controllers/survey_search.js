wildbook.app.directive(
    'wbSurveySearch',
    ["$http", "$exceptionHandler", "wbConfig", function($http, $exceptionHandler, wbConfig) {
        return {
            restrict: 'E',
            scope: {
                searchSurveyDone: "&"
            },
            templateUrl: 'util/render?j=partials/survey_search',
            replace: true,
            controller: function($scope) {
                $scope.data = {};
                $scope.surveysearch = {};
            
                $scope.orgChange = function() {
                    //
                    // This is apparently a copy of the object in the collection so
                    // setting anything on this is not preserved from one selection
                    // to the next. So we have to adjust the original collection.
                    //
                    var org = $scope.search.organization;
            
                    if (org == null) {
                        $scope.data.vessels = null;
                        delete $scope.surveysearch.orgid;
                        return;
                    }
            
                    $scope.surveysearch.orgid = org.orgId;
            
                    wbConfig.getVessels(org)
                    .then(function(vessels) {
                        $scope.data.vessels = vessels;
                    });
                }
            
                $scope.search = function() {
                    $http.post("search/survey", $scope.surveysearch)
                    .then(function(result) {
                        $scope.gotresults = true;
                        $scope.surveyGridOptions.api.setRowData(result.data);
                    },
                    $exceptionHandler);
                };
            
                function rowSelectedFunc(event) {
            //        var survey = event.node.data.survey;
            //        survey.tracks = [event.node.data.track];
                    $scope.searchSurveyDone({surveypart: event.node.data});
                }
            
                $scope.surveyGridOptions = {
                    columnDefs:
                        [{headerName: "Organization",
                            field: "survey",
                            cellRenderer: function(params) {
                                if (params.value.organization) {
                                    return params.value.organization.displayName;
                                }
                                return null;
                            }
                         },
                         {headerName: "Number",
                             field: "survey",
                             cellRenderer: function(params) {
                                 return params.value.surveyNumber;
                             }
                         },
                         {headerName: "Vessel",
                             field: "track",
                             cellRenderer: function(params) {
                                 if (params.value.vessel) {
                                     return params.value.vessel.displayName;
                                 }
                                 return null;
                             }
                         },
                         {headerName: "Date",
                             field: "track",
                             cellRenderer: function(params) {
                                 return params.value.formattedTime;
                             }
                         },
                         {headerName: "Code",
                             field: "track",
                             cellRenderer: function(params) {
                                 return params.value.code;
                             }
                         },
                         {headerName: "Location",
                            field: "track",
                             cellRenderer: function(params) {
                                 if (params.value.location) {
                                     return params.value.location.locationid;
                                 }
                             }
                         }],
                    rowData: null,
                    enableSorting: true,
                    rowSelection: 'single',
                    onRowSelected: rowSelectedFunc
                };
                
                //
                // wb-key-handler-form
                //
                $scope.cancel = function() {
                    $scope.searchSurveyDone({surveypart: null});
                }
                
                $scope.cmdEnter = function() {
                    $scope.search();
                }
            }
        };
    }]
);
