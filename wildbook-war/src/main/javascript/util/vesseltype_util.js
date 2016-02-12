/* global angular */
'use strict';

angular.module('wildbook.util')
.directive("wbVesselTypeUtils", ["wbConfig", function(wbConfig) {
    return {
        restrict: 'E',
        scope: {
            selectedVesselTypeId:'=',
            vesselType: '=',
            standalone: '@'
        },
        templateUrl: 'util/vesseltype_util.html',
        link: function($scope, ele, attr){
            $scope.showAddNew = false;

            wbConfig.config()
            .then(function(config) {
                $scope.vesseltypes = config.vesseltypes;
                if ($scope.vesseltypes.length === 0) {
                    $scope.selectedVesselTypeId = "new";
                    $scope.showAddNew = true;
                }
            });

            $scope.checkNew = function(selectedVesselTypeId) {
                if (selectedVesselTypeId === "new") {
                    $scope.showAddNew = true;
                } else {
                    $scope.showAddNew = false;
                }
            };
        }
    };
}]);
