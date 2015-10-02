wildbook.app.controller("SurveyEditController", function($scope, $http, $exceptionHandler) {
    $scope.orgChange = function() {
        $scope.data = {};

        //
        // This is apparently a copy of the object in the collection so
        // setting anything on this is not preserved from one selection
        // to the next. So we have to adjust the original collection.
        //
        var org = $scope.survey.organization;

        if (org == null) {
            $scope.data.vessels = null;
            delete $scope.survey.organization;
            return;
        }

        $scope.getVessels(org)
        .then(function(vessels) {
            $scope.data.vessels = vessels;
        });

        $scope.save = function() {
            //
            // TODO: Save survey
        }
    }
});
