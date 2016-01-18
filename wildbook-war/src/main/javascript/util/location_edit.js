/* global angular */
'use strict';

angular.module('wildbook.admin').directive(
    'locationEdit',
    function() {
        return {
            restrict: 'E',
            scope: {
                location: '='
            },
            templateUrl: 'util/location_edit.html',
            link: function($scope) {
                $scope.accuracies = [{value: null, label: '--Unknown--'},
                                     {value: 'PRECISE', label: 'Precise'},
                                     {value: 'APPROX', label: 'Approximate'},
                                     {value: 'GENERAL', label: 'General'}];
                $scope.precisionSources = [{value: null, label: '--Unknown--'},
                                           {value: 'CAMERA', label: 'Camera'},
                                           {value: 'GPS', label: 'GPS'},
                                           {value: 'MANUAL', label: 'Manual'}];
            }
        };
    }
);
