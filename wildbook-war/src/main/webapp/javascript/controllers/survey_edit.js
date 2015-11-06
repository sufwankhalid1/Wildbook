wildbook.app.directive(
    'wbSurveyEdit',
    ["$http", "$exceptionHandler", "wbConfig", function($http, $exceptionHandler, wbConfig) {
        return {
            restrict: 'E',
            scope: {
                data: "=surveyToEdit",
                editSurveyDone: "&"
            },
            templateUrl: 'util/render?j=partials/survey_edit',
            replace: true,
            controller: function($scope) {
                $scope.info = {};
                
                if ($scope.data === "new") {
                    $scope.data = {};
                }
            
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
            
                    wbConfig.getVessels(org)
                    .then(function(vessels) {
                        $scope.info.vessels = vessels;
                    });
                };
            
                $scope.save = function() {
                    $http.post('obj/survey/savetrack', $scope.data)
                    .then(function() {
                        $scope.editSurveyDone({surveypart: $scope.data});
                    }, $exceptionHandler);
                };
                
                //
                // wb-key-handler-form
                //
                $scope.cancel = function() {
                    $scope.editSurveyDone({surveypart: null});
                }
                
                $scope.cmdEnter = function() {
                    $scope.save();
                }
            }
        };
    }]
);
