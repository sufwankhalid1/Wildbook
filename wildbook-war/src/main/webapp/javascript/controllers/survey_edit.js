wildbook.app.controller("SurveyEditController", function($scope, $http, $exceptionHandler) {
    var panelName = "survey_edit";
    $scope.panelList.push(panelName);

    $scope.info = {};

    $scope.$on(panelName, function(event, data) {
        if (typeof data === "boolean") {
            $scope.panels[panelName] = false;
            return;
        }

        $scope.panels[panelName] = true;
        if (data) {
            $scope.data = data;
        } else {
//            // create empty new survey with one track.
//            $scope.data = {tracks: [{}]};
            $scope.data = {};
        }
    });

    $scope.orgChange = function() {
        //
        // This is apparently a copy of the object in the collection so
        // setting anything on this is not preserved from one selection
        // to the next. So we have to adjust the original collection.
        //
        var org = $scope.data.survey.organization;

        if (org == null) {
            $scope.info.vessels = null;
            delete $scope.data.survey.organization;
            return;
        }

        $scope.main.getVessels(org)
        .then(function(vessels) {
            $scope.info.vessels = vessels;
        });
    };

    $scope.save = function() {
        $http.post('obj/survey/savetrack', $scope.data)
        .then(function() {
            $scope.panels[panelName] = false;
            $scope.$emit(panelName + "_done", $scope.data);
        }, $exceptionHandler);
    };
});
//
//wildbook.app.directive('surveyTrackEdit', function() {
//    return {restrict: 'E',
//        scope: {
//            location: '='
//        },
//        templateUrl: 'util/render?j=partials/survey-track-edit.jade'
//    };
//});
