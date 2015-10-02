wildbook.app.controller("SurveySearchController", function($scope, $http, $exceptionHandler) {
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

        $scope.getVessels(org)
        .then(function(vessels) {
            $scope.data.vessels = vessels;
        });
    }

    $scope.search = function() {
        $http({url: "search/survey", params: $scope.surveysearch})
        .then(function(result) {
            $scope.gotresults = true;
            $scope.surveyGridOptions.rowData = result.data;
            $scope.surveyGridOptions.api.onNewRows();
        },
        $exceptionHandler);
    };

    $scope.surveyGridOptions = {
        columnDefs:
            [{headerName: "Organization", field: "survey.organization.name"},
             {headerName: "Number", field: "survey.surveyNumber"},
             {headerName: "Date", field: "part.formattedDate"},
             {headerName: "Code", field: "code"},
             {headerName: "location", field: "part.location.locationid"}],
        rowData: null,
        enableSorting: true,
        angularCompileRows: true
    };
});
