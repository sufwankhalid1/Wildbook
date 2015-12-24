angular.module('wildbook.admin').directive(
    'wbEncounterSearch',
    ["$http", "$exceptionHandler", function($http, $exceptionHandler) {
        return {
            restrict: 'E',
            scope: {
                searchEncounterDone: "&"
            },
            templateUrl: 'encounters/encounter_search.html',
            replace: true,
            controller: function($scope) {
                $scope.searchdata =  { 
                    encounter: {},
                    individual: {},
                    contributor: {}
                }
                $scope.selectedTabIndex = 0;

                $scope.search = function() {
                    $http.post("admin/search/encounter", $scope.searchdata)
                    .then(function(result) {
                        $scope.gridOptions.api.setRowData(result.data);
                        $scope.selectedTabIndex = 1;
                    },
                    $exceptionHandler);
                };
                
                function rowSelectedFunc(event) {
                    $scope.searchEncounterDone({encounter: event.node.data});
                }
            
                $scope.gridOptions = {
                    columnDefs:
                        [{headerName: "",
                            field: "individual",
                            cellRenderer: function(params) {
                                if (params.value && params.value.avatar) {
                                    return '<img width="*" height="32px" src="' + params.value.avatar + '"/>';
                                }
                                return null;
                            },
                            width: 32
                         },
                         {headerName: "Individual",
                             field: "individual",
                             cellRenderer: function(params) {
                                 if (params.value) {
                                     return params.value.displayName;
                                 }
                                 return null;
                             }
                         },
                         {headerName: "Species",
                             field: "individual",
                             cellRenderer: function(params) {
                                 if (params.value && params.value.species) {
                                     return params.value.species.name;
                                 }
                                 return null;
                             }
                         },
                         {headerName: "Date",
                             field: "formattedTime"
                         },
                         {headerName: "Location",
                             field: "location",
                             cellRenderer: function(params) {
                                 return params.value ?  params.value.locationid : null;
                             }
                         }],
                    rowData: null,
                    rowHeight: 32,
                    enableSorting: true,
                    rowSelection: 'single',
                    onRowSelected: rowSelectedFunc
                };
                
                //
                // wb-key-handler-form
                //
                $scope.cancel = function() {
                    $scope.searchEncounterDone(null);
                }
                
                $scope.cmdEnter = function() {
                    $scope.search();
                }
            }
        }
    }]
);
