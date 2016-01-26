/* global angular */
'use strict';

//generic cancel button
angular.module('wildbook.util')
.directive('cancelButton', [function() {
    return {
        restrict: 'E',
        scope: {
                hideme: '=?',
                cancel: '&'
                },
        template: '<md-icon md-svg-icon="close-circle" ng-show="!hideme" ng-click="cancel()"><md-tooltip>Cancel</md-tooltip></md-icon>'
    };
}]);
