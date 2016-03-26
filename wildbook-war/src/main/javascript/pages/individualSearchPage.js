/* global angular, alertplus */
'use strict';

require('../encounters/individual_search.js');
require('../encounters/individual_edit.js');
require('../encounters/orphaned_search.js');
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

               $scope.searchIndividualDone = function(individual, cbOrphaned) {
                    $scope.indData = individual;
                    $scope.delOrphaned = cbOrphaned;
               };

               //
               // Want to delete only orphaned individuals so there is a callback in orphaned_search which calls to individual_search
               // When an individual is clicked, that passes a callback of isOrphaned here. May be able to skip that middle man?
               //

               $scope.editIndividualDone = function() {
                    $scope.mode_edit = false;
               };

               $scope.edit = function() {
                   $scope.mode_edit = true;
               };

               $scope.deleteInd = function() {
                   return alertplus.confirm("You're about to delete this individual", "Delete Individual")
                   .then(function() {
                       $http.post("admin/api/individual/delete", $scope.indData);
                       $scope.removed = $scope.indData.id;
                   });
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
