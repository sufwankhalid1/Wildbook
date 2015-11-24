angular.module('wildbook.admin').directive(
    "wbEncounterSearchPage",
    ["wbEncounterUtils", function (wbEncounterUtils) {
        return {
            restrict: 'E',
            scope: {},
            templateUrl: 'directive/encounterSearchPage.html',
            replace: true,
            controller: function($scope) {
                $scope.mode_edit = false;
                
                $scope.searchEncounterDone = function(encounter) {
                    wbEncounterUtils.getEncData(encounter)
                    .then(function(encdata) {
                        $scope.encdata = encdata;
                    });
                }
                
                $scope.editEncounterDone = function(encdata) {
                    $scope.mode_edit = false;
                }
                
                $scope.edit = function() {
                    $scope.mode_edit = true;
                }
                
                $scope.cancel = function() {
                    if ($scope.mode_edit) {
                        $scope.mode_edit = false;
                    } else {
                        $scope.encdata = null;
                    }
                }
            }
        }
    }]
);
