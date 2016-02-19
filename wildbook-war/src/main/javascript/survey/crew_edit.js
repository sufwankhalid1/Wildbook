/* global angular, alertplus */
'use strict';

angular.module('wildbook.admin').directive(
    'wbCrewEdit',
    ["$http", function($http) {
        return {
            restrict: 'E',
            templateUrl: 'survey/crew_edit.html',
            scope: {
                data: '='
            },
            replace: true,
            link: function($scope, element, attr) {
                if (!$scope.data.crew) {
                    $scope.data.crew = [];
                }

                $scope.originalCrew = angular.copy($scope.data.crew);

                if ($scope.data.part && $scope.data.part.surveyPartId) {
                    var newmember = {
                                     user: {'fullName': 'Choose User'},
                                     crewrole: {},
                                     surveypartid: $scope.data.part.surveyPartId
                                    };
                }

                $scope.addMember = function() {
                    $scope.data.crew.push(newmember);
                };

                $scope.addCrewMember = function(member, index) {
                    $scope.data.crew[index].user = member;
                };

                $scope.save = function() {
                    var hasError;
                    $scope.data.crew.forEach(function(member) {
                        if (!member.user.id) {
                            alertplus.error("One of your crew members is missing a user!");
                            hasError = true;
                        }

                        if (!member.crewrole.role || !member.crewrole.crewRoleId) {
                            alertplus.error("One of your crew members is missing a role or a new role has been created and not saved before adding this crew.");
                            hasError = true;
                        }
                    });

                    if (hasError) {
                        return;
                    }

                    $http.post('obj/survey/updatecrewmember', $scope.data.crew)
                    .then(function() {
                        alertplus.alert("Crew has been successfully saved");
                        $scope.editcrew = false;
                    });
                };

                $scope.getCrew = function() {
                    $http.get('obj/survey/part/getcrew/' + $scope.data.part.surveyPartId)
                    .then(function(res) {
                        $scope.data.crew = res.data;
                    });
                };

                $scope.selectedRole = function(crewrole, index) {
                    $scope.data.crew[index].crewrole = crewrole;
                };

                $scope.cancel = function() {
                    $scope.data.crew = $scope.originalCrew;
                };

                $scope.delete = function(index) {
                    $scope.data.crew.splice(index, 1);
                };
            }
        };
    }]
);
