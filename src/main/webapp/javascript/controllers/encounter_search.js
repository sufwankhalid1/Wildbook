wildbook.app.controller("EncounterSearchController", function($scope, $http, $exceptionHandler) {
    $scope.searchdata = {};

    $scope.search = function() {
        $http({url: "search/encounter", params: $scope.searchdata})
        .then(function(result) {
            $scope.gotresults = true;
            $scope.gridOptions.api.setRowData(result.data);
        },
        $exceptionHandler);
    };

    function rowSelectedFunc(event) {
        $scope.$emit('encounter_search_done', event.node.data);
    }

    $scope.gridOptions = {
        columnDefs:
            [{headerName: "Individual",
                field: "individual",
                cellRenderer: function(params) {
                    if (params.value) {
                        return params.value.displayName;
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
        enableSorting: true,
        rowSelection: 'single',
        onRowSelected: rowSelectedFunc
    };
});
