wildbook.app.controller("SurveyEditController", function($scope, $http, $exceptionHandler) {
    var panelName = "survey_edit";
    $scope.panelList.push(panelName);

    $scope.data = {};

    $scope.$on(panelName, function(event, data) {
        if (typeof data === "boolean") {
            $scope.panels[panelName] = false;
            return;
        }

        $scope.panels[panelName] = true;
        if (data) {
            $scope.survey = data;
        } else {
            // create empty new survey
            $scope.survey = {tracks: [{}]};
        }
    });

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

        $scope.main.getVessels(org)
        .then(function(vessels) {
            $scope.data.vessels = vessels;
        });
    };

    $scope.save = function() {
        $http.post('obj/survey/save', $scope.survey)
        .then(function() {
            $scope.panels[panelName] = false;
            $scope.$emit(panelName + "_done", $scope.survey);
        }, $exceptionHandler);
    };
});
