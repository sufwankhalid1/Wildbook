angular.module('wildbook.admin').directive(
    'wbIndividualSearch',
    ["$http", "$exceptionHandler", "wbConfig", function($http, $exceptionHandler, wbConfig) {
        return {
            restrict: 'E',
            scope: {
                searchIndividualDone: "&"
            },
            templateUrl: 'partials/individual_search.html',
            replace: true,
            controller: function($scope) {
                $scope.searchdata = {};
                $scope.selectedTabIndex = 0;
            
                $scope.search = function() {
                    $http.post("search/individual", $scope.searchdata)
                    .then(function(result) {
                        $scope.gridOptions.api.setRowData(result.data);
                        $scope.selectedTabIndex = 1;
                    },
                    $exceptionHandler);
                };
            
                function rowSelectedFunc(event) {
                    $scope.searchIndividualDone({individual: event.node.data});
                }

                $scope.getSpecies = function() {
                    return wbConfig.config().species;
                }
                
                $scope.clearSpecies = function() {
                    $scope.searchdata.species = undefined;
                }

                $scope.gridOptions = {
                    columnDefs:
                        [{headerName: "Name/ID",
                            field: "displayName"
                         },
                         {headerName: "Species",
                             field: "species",
                             cellRenderer: function(params) {
                                 if (params.value) {
                                     return params.value.name;
                                 }
                                 return null;
                             }
                         },
                         {headerName: "Sex",
                             field: "sex"
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
                    $scope.searchIndividualDone({individual: null});
                }
                
                $scope.cmdEnter = function() {
                    $scope.search();
                }
            }
        }
    }]
);
