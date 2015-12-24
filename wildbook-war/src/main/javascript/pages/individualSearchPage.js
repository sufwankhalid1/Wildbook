require('../encounters/individual_search.js');
require('../encounters/individual_edit.js');

angular.module('wildbook.admin').directive(
    'wbIndividualSearchPage',
    ["$http", "$exceptionHandler", "wbConfig", function($http, $exceptionHandler, wbConfig) {
        return {
            restrict: 'E',
            templateUrl: 'pages/individualSearchPage.html',
            replace: true,
            controller: function($scope) {
               $scope.mode_edit = false;
               
               $scope.searchIndividualDone = function(individual) {
                    $scope.indData = individual;
               }

               $scope.editIndividualDone = function() {
                    $scope.mode_edit = false;
               }

               $scope.edit = function() {
                   $scope.mode_edit = true;
               }
                
               $scope.cancel = function() {
                   if ($scope.mode_edit) {
                       $scope.mode_edit = false;
                   } else {
                       $scope.indData = null;
                   }
                }
            }
        }
    }]
);
