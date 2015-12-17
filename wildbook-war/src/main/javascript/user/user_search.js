angular.module('wildbook.admin').directive(
    'wbUserSearch',
    ["$http", "$exceptionHandler", function($http, $exceptionHandler) {
        return {
            restrict: 'E',
            scope: {
                searchUserDone: "&"
            },
            templateUrl: 'user/user_search.html',
            replace: true,
            link: function($scope, element, attr) {
                $scope.searchdata = {};
                $scope.selectedTabIndex = 0;
            
                $scope.search = function() {
                    $http.post("search/user", $scope.searchdata)
                    .then(function(result) {
                        $scope.gridOptions.api.setRowData(result.data);
                        $scope.selectedTabIndex = 1;
                    },
                    $exceptionHandler);
                };
            
                function rowSelectedFunc(event) {
                    $scope.searchUserDone({user: event.node.data});
                }

                $scope.gridOptions = {
                    columnDefs:
                        [{headerName: "Avatar",
                            field: "avatar",
                            cellRenderer: function(params) {
                                if (params.value) {
                                    return '<img width="*" height="32px" src="' + params.value + '"/>';
                                }
                                return null;
                            },
                            width: 32
                        },
                        {headerName: "Name",
                            field: "displayName"
                        }],
                    rowData: null,
                    enableSorting: true,
                    rowSelection: 'single',
                    onRowSelected: rowSelectedFunc,
                    rowHeight: 32
                };
                
                
                //
                // wb-key-handler-form
                //
                $scope.cancel = function() {
                    $scope.searchUserDone({user: null});
                }
                
                $scope.cmdEnter = function() {
                    $scope.search();
                }
            }
        }
    }]
);
