/* global angular */
'use strict';

require('../encounters/individual_search.js');
require('../encounters/individual_edit.js');
require('../encounters/individual_id_search.js');

angular.module('wildbook.admin').directive(
    'wbIndividualSearchPage',
    ['$http', function($http) {
        return {
            restrict: 'E',
            templateUrl: 'pages/individualSearchPage.html',
            replace: true,
            controller: function($scope) {
               $scope.mode_edit = false;

               $scope.searchIndividualDone = function(individual) {
                    $scope.indData = individual;
               };

               $scope.editIndividualDone = function(individual, deleted) {
                    $scope.mode_edit = false;
                    if (deleted) {
                        $scope.removed = individual.id;
                    }
               };

               $scope.edit = function() {
                   $scope.mode_edit = true;
               };

                $scope.reset = function() {
                    $scope.indData = null;
                    $scope.mode_edit = false;
                };

               $scope.cancel = function() {
                   if ($scope.mode_edit) {
                       $scope.mode_edit = false;
                   } else {
                       $scope.indData = null;
                   }
               };
            }
        };
    }]
);
