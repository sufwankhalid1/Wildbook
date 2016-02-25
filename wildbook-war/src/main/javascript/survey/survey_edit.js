/* global angular */
'use strict';

angular.module('wildbook.admin').directive(
    'wbSurveyEdit',
    ["$http", "$exceptionHandler", "wbConfig", "wbDateUtils", "wbSurveyUtils", function($http, $exceptionHandler, wbConfig, wbDateUtils, wbSurveyUtils) {
        return {
            restrict: 'E',
            scope: {
                data: "=surveyToEdit",
                editSurveyDone: "&"
            },
            templateUrl: 'survey/survey_edit.html',
            replace: true,
            controller: function($scope) {
                $scope.info = {};

                $scope.getOrgId = function(orgId) {
                    wbConfig.config()
                    .then(function(config) {
                        config.orgs.forEach(function(org) {
                            if (org.orgId === orgId) {
                                $scope.data.survey.organization = org;
                            }
                        });
                    });
                };

                $scope.getVesselId = function(vessel) {
                    $scope.data.part.vessel = vessel;
                };

                // $scope.orgChange = function() {
                //     //
                //     // This is apparently a copy of the object in the collection so
                //     // setting anything on this is not preserved from one selection
                //     // to the next. So we have to adjust the original collection.
                //     //
                //
                //     var org = $scope.data.survey.organization;
                //
                //
                //     if (org === null) {
                //         $scope.info.vessels = null;
                //         delete $scope.data.survey.organization;
                //         return;
                //     }
                //
                //     wbConfig.getVessels(org)
                //     .then(function(vessels) {
                //         $scope.info.vessels = vessels;
                //     });
                // };

                if ($scope.data.part && $scope.data.part.partDate) {
                    if ($scope.data.part.partDate.length === 3) {
                        $scope.dateObj = new Date($scope.data.part.partDate[0], $scope.data.part.partDate[1] - 1, $scope.data.part.partDate[2]);
                    } else if ($scope.data.part.partDate.length === 2) {
                        $scope.dateObj = new Date($scope.data.part.partDate[0], $scope.data.part.partDate[1] - 1);
                    }
                }

                $scope.save = function() {
                    //md-datetime needs a date obj, so convert to date obj for use, convert back for save
                    $scope.data.part.partDate = wbDateUtils.dateToRest($scope.dateObj);

                    $scope.data.part = wbDateUtils.verifyTimeInput($scope.data.part);

                    $http.post('admin/api/survey/savetrack', $scope.data)
                    .then(function(result) {
                        $scope.editSurveyDone({surveypart: result.data});
                    }, $exceptionHandler);
                };

                //
                // wb-key-handler-form
                //
                $scope.cancel = function() {
                    $scope.editSurveyDone({surveypart: null});
                };

                $scope.cmdEnter = function() {
                    $scope.save();
                };
            }
        };
    }]
);
