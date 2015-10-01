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

        //
        // Let's find our master organization so that we can add the vessels to
        // it and thus cache the results for future occerences of the user picking
        // this organization again in the list.
        //
        var orgfilter = $scope.maindata.organizations.filter(function(value) {
            return (value.orgId == org.orgId);
        });

        var orgmaster;
        if (orgfilter.length > 0) {
            orgmaster = orgfilter[0];
        } else {
            //
            // Just set it to org so we don't have to check for null below
            // but it should *never* not be an array of size 1
            //
            orgmaster = org;
        }

        if (orgmaster.vessels) {
            $scope.data.vessels = orgmaster.vessels;
        } else {
            $http({url: "obj/survey/vessels/get", params: {orgid: org.orgId}})
            .then(function(results) {
                orgmaster.vessels = results.data;
                $scope.data.vessels = results.data;
            });
        }
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
