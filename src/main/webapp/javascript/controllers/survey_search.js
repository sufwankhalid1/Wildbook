wildbook.app.controller("SurveySearchController", function($scope, $http, $exceptionHandler) {
    var panelName = "survey_search";
    $scope.panelList.push(panelName);

    $scope.$on(panelName, function(event, data) {
        if (typeof data === "boolean") {
            $scope.panels[panelName] = false;
            return;
        }

        $scope.panels[panelName] = true;
    });

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

        $scope.main.getVessels(org)
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
        var survey = event.node.data.survey;
        survey.tracks = [event.node.data.part];
        $scope.$emit(panelName + "_select", survey);
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
                 field: "part",
                 cellRenderer: function(params) {
                     if (params.value.vessel) {
                         return params.value.vessel.displayName;
                     }
                     return null;
                 }
             },
             {headerName: "Date",
                 field: "part",
                 cellRenderer: function(params) {
                     return params.value.formattedTime;
                 }
             },
             {headerName: "Code",
                 field: "part",
                 cellRenderer: function(params) {
                     return params.value.code;
                 }
             },
             {headerName: "Location",
                field: "part",
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
});
