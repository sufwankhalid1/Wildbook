/* global angular */
'use strict';

angular.module('wildbook.admin').directive(
    'wbIndividualSearch',
    ["$http", "$exceptionHandler", function($http, $exceptionHandler) {
        return {
            restrict: 'E',
            scope: {
                searchIndividualDone: "&",
                individualSearchCancelButtonHide: "@",
                resetSelectedResult:"&",
                isOrphaned: "&",
                removed: "="
            },
            templateUrl: 'encounters/individual_search.html',
            replace: true,
            controller: function($scope) {
                var orphaned;

                $scope.searchdata =  {
                    encounter: {},
                    individual: {},
                    contributor: {}
                };

                $scope.$watch('removed', function(newVal, oldVal) {
                    if (newVal !== oldVal) {
                        $scope.gridOptions.rowData.forEach(function(ind, index) {
                            if (ind.id === newVal) {
                                $scope.gridOptions.rowData.splice(index, 1);
                                $scope.gridOptions.api.setRowData($scope.gridOptions.rowData);
                            }
                        });
                    }
                });

                $scope.reset = function() {
                    $scope.searchdata =  {
                        encounter: {},
                        individual: {},
                        contributor: {}
                    };

                    $scope.indid = null;
                    $scope.searchIndividualDone({individual: null});
                };

                $scope.selectedTabIndex = 0;

                $scope.search = function() {
                    $scope.resetSelectedResult({val: null});
                    $http.post("admin/api/search/individual", $scope.searchdata)
                    .then(function(result) {
                        $scope.gridOptions.api.setRowData(result.data);
                        $scope.selectedTabIndex = 1;
                        orphaned = null;
                    },
                    $exceptionHandler);
                };

                $scope.orphaned = function(data, cbOrphaned) {
                    $scope.resetSelectedResult({val: null});
                    $scope.gridOptions.api.setRowData(data);
                    $scope.selectedTabIndex = 1;
                    orphaned = cbOrphaned;
                };

                $scope.idSearch = function(data) {
                    $scope.resetSelectedResult({val: null});
                    $scope.searchIndividualDone({individual: data});
                    orphaned = null;
                };

                function rowSelectedFunc(event) {
                    $scope.searchIndividualDone({individual: event.node.data, isOrphaned: orphaned});
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
                };

                $scope.cmdEnter = function() {
                    $scope.search();
                };
            }
        };
    }]
);
