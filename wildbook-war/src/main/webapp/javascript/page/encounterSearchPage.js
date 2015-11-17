wildbook.app.directive("wbEncounterSearchPage",
    ["wbEncounterUtils", function (wbEncounterUtils) {
        return {
            restrict: 'E',
            scope: {},
            templateUrl: 'util/render?j=directive/encounterSearchPage',
            replace: true,
            controller: function($scope) {
                $scope.searchEncounterDone = function(encounter) {
                    wbEncounterUtils.getEncData(encounter)
                    .then(function(encdata) {
                        $scope.encdata = encdata;
                    });
                }
                
                $scope.editEncounterDone = function(encdata) {
                    $scope.encdata = null;
                }
            }
        }
    }]
);
