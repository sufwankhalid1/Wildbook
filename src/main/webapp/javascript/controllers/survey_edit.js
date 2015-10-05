wildbook.app.controller("SurveyEditController", function($scope, $http, $exceptionHandler) {
    $scope.data = {};
    //
    // New empty survey with one empty track. This is for new surveys.
    //
    $scope.survey = {tracks: [{}]};

    $scope.orgChange = function() {
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
    };

    $scope.save = function() {
        $http.post('obj/survey/save', $scope.survey)
        .then(function() {
            $scope.$emit('survey_edit_done', $scope.survey);
        }, $exceptionHandler);
    };
});
