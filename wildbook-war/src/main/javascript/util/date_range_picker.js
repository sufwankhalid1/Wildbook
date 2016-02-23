/* global angular */
'use strict';

//
//=data : scope to attach the daterange obj to
//

angular.module('wildbook.util')
.directive('wbDateRangePicker', [function() {
    return {
        restrict: 'E',
        scope: {
                searchdata: '='
                },
        templateUrl: 'util/date_range_picker.html',
        link: function($scope, ele, attr) {

            $scope.standalone = false;

            if (!$scope.searchdata) {
                $scope.searchdata = {range:0};
                $scope.standalone = true;
            }
        }
    };
}]);
