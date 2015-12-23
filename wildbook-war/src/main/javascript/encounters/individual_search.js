angular.module('wildbook.admin').directive(
    'wbIndividualSearch',
    ["$http", "$exceptionHandler", "wbConfig", function($http, $exceptionHandler, wbConfig) {
        return {
            restrict: 'E',
            scope: {
                searchIndividualDone: "&",
                individualSearchCancelButtonHide: "@"
            },
            templateUrl: 'encounters/individual_search.html',
            replace: true,
            controller: function($scope) {
                $scope.searchdata = {};
                $scope.selectedTabIndex = 0;
            
                $scope.search = function() {
                    $http.post("obj/individual/search", $scope.searchdata)
                    .then(function(result) {
                        $scope.gridOptions.api.setRowData(result.data);
                        $scope.selectedTabIndex = 1;
                    },
                    $exceptionHandler);
                };

                function rowSelectedFunc(event) {
                    $scope.searchIndividualDone({individual: event.node.data});
                }
                
                wbConfig.config()
                .then(function(config) {
                    $scope.allSpecies = config.species;
                });
                
                $scope.clearSpecies = function() {
                    $scope.searchdata.species = undefined;
                }

                $scope.gridOptions = {
                    columnDefs:
                        [{
                            headerName: "Avatar",
                            field: "avatar",
                            cellRenderer: function(params) {
                                if (params.value) {
                                    return '<img width="*" height="32px" src="' + params.value + '"/>';
                                }
                                return null;
                            },
                            width: 32
                         },
                         {headerName: "Name/ID",
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
                    rowHeight: 32,
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
