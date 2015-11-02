wildbook.app.controller("EncounterSearchController", function($scope, $http, $exceptionHandler) {
    var panelName = "encounter_search";
    $scope.searchdata = {};

    $scope.search = function() {
        $http.post("search/encounter", $scope.searchdata)
        .then(function(result) {
            $scope.gotresults = true;
            $scope.gridOptions.api.setRowData(result.data);
        },
        $exceptionHandler);
    };

    function rowSelectedFunc(event) {
        $scope.$emit(panelName + "_select", event.node.data);
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
    
    //
    // wb-key-handler-form
    //
    $scope.cancel = function() {
        $scope.panels[panelName] = false;
    }
    
    $scope.cmdEnter = function() {
        $scope.search();
    }
});
