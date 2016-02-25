/* global angular, alertplus, document */
'use strict';

angular.module('wildbook.admin').directive(
    'wbUserView',
    function() {
        return {
            restrict: 'E',
            templateUrl: 'user/user_view.html',
            scope: {
                user: '=',
            },
            replace: true,
            link: function($scope, elem, attr) {
            }
        };
    }
);

angular.module('wildbook.admin').directive(
    "wbUserEditFields",
    ["$http", "wbConfig", "$mdDialog",
     function ($http, wbConfig, $mdDialog) {
        return {
            restrict: 'E',
            templateUrl: 'user/user_edit_fields.html',
            scope: {
                user: '=',
                save: '&',
                roles: '=',
                showdelete: '@'
            },
            replace: true,
            controller: function($scope) {
                $scope.deleteUser = function() {
                    return alertplus.confirm('Are you sure you want to delete this user?', "Delete User", true)
                    .then(function() {
                        $http.post("admin/api/user/userdelete", $scope.user)
                        .then(function() {
                            //
                            // TODO: Reperform search to clear out this now deleted value.
                            //
                            delete $scope.user;
                        });
                    });
                };

                //modify user Roles
                $scope.modifyUserRoles = function($event) {
                    var parentEl = angular.element(document.body);

                    function DialogController($scope, $mdDialog, user, userroles) {

                        //
                        //TODO: Need to get a list of availableroles rather than hardcode them
                        //

                        $scope.availableroles = ["admin", "rest"];
                        $scope.userroles = userroles;
                        $scope.selectedAvailableRole = [];
                        $scope.selectedUserRoles = [];

                        $scope.closeDialog = function() {
                            $mdDialog.hide();
                        };

                        $scope.update = function() {
                            $http.post('admin/api/user/roles/update/'+user.id, $scope.userroles)
                            .then(function(res) {
                                $scope.selectedAvailableRole = [];
                                $scope.selectedUserRoles = [];

                                $mdDialog.hide()
                                .then(function(){
                                    return updateRoles($scope.userroles);
                                });
                            });
                        };

                        $scope.selectVal = function(arr, val) {
                            if (arr.length) {
                                var spliced = false;
                                for (var ii = 0; ii < arr.length; ii++) {
                                    if(arr[ii] === val) {
                                        arr.splice(ii, 1);
                                        spliced = true;

                                    }
                                }

                                if (!spliced) {
                                    arr.push(val);
                                }
                            } else {
                                arr.push(val);
                            }
                        };

                        $scope.clearSelectedUsers = function() {
                            $scope.selectedUserRoles = [];
                        };

                        $scope.clearAvailableSelected = function() {
                            $scope.selectedAvailableRole = [];
                        };

                        $scope.compareArrs = function(arr1, arr2) {
                            if (arr1.length > 0) {
                                for (var ii = 0; ii < arr1.length; ii++) {
                                    if (arr2.indexOf(arr1[ii]) > -1) {
                                        arr1.splice(ii, 1);
                                        ii--;
                                    }
                                }
                            }
                        };

                        $scope.updateArr = function(selected, target, from) {
                            if (selected.length === 0) {
                                return;
                            }

                            for (var ii = 0; ii < selected.length; ii++) {
                                target.push(selected[ii]);
                            }

                            $scope.compareArrs(from, selected);

                            $scope.selectedAvailableRole = [];
                            $scope.selectedUserRoles = [];
                            selected = [];
                        };

                        //init
                        $scope.compareArrs($scope.availableroles, $scope.userroles);
                    }

                    $mdDialog.show({
                        parent: parentEl,
                        clickOutsideToClose: true,
                        targetEvent: $event,
                        templateUrl: "user/user_roles_edit.html",
                        bindToController: true,
                        locals: {
                            user: $scope.user,
                            userroles: angular.copy($scope.roles)
                        },
                        controller: DialogController
                    });
                };

                function updateRoles(dialogRes) {
                    $scope.roles = dialogRes;
                }

                 //change password dialog
                $scope.changeUserPassword = function($event) {
                    var parentEl = angular.element(document.body);

                    function DialogController($scope, $mdDialog, user) {
                        $scope.password = null;
                        $scope.verifyPassword = "";
                        $scope.disableSave = true;

                        $scope.$watch('verifyPassword', function(newVal, oldVal) {
                            if (newVal && newVal.length > 5 && newVal === $scope.password) {
                                $scope.disableSave = false;
                            } else {
                                $scope.disableSave = true;
                            }
                        });

                        $scope.savePassword = function() {
                            if ($scope.password === $scope.verifyPassword && $scope.password.length >= 6) {
                                if ($scope.password.length < 6) {
                                    return;
                                }
                                $http.post('admin/api/user/editpw/' + user.id, $scope.password);
                            }

                            $mdDialog.hide();
                        };

                        $scope.closeDialog = function() {
                            $mdDialog.hide();
                        };
                    }

                    $mdDialog.show({
                        parent: parentEl,
                        clickOutsideToClose: true,
                        preserveScope: true,
                        targetEvent: $event,
                        template:
                            '<md-dialog class="admin-changepassword-dialog md-raised" aria-label="List dialog">'
                            +'   <md-toolbar>'
                            +'       <div class="md-toolbar-tools">'
                            +'           <h2>Change User Password</h2>'
                            +'           <span flex></span>'
                            +'           <md-button class="md-icon-button" ng-click="closeDialog()">'
                            +'               <md-icon md-svg-icon="close" aria-label="Close dialog"></md-icon>'
                            +'           </md-button>'
                            +'       </div>'
                            +'   </md-toolbar>'
                            +'   <md-dialog-content class="md-dialog-content">'
                            +'       <md-input-container>'
                            +'         <label> Password </label>'
                            +'         <input required aria-label="password" minlength="8" ng-model="password" type="password">'
                            +'         <div ng-messages="password.$error" ng-show="password.length < 8">'
                            +'           <div ng-message="minlength">Password must be at least 8 characters.</div>'
                            +'         </div>'
                            +'       </md-input-container>'
                            +'       <md-input-container>'
                            +'         <label> Verify Password </label>'
                            +'         <input required aria-label="password" minlength="8" ng-model="verifyPassword" type="password">'
                            +'         <div ng-messages="verifyPassword.$error" ng-show="verifyPassword.length < 8">'
                            +'           <div ng-message="minlength">Password must be at least 8 characters.</div>'
                            +'         </div>'
                            +'       </md-input-container>'
                            +'       <div class="mt-5" ng-show="password != verifyPassword">'
                            +'         Passwords do not match'
                            +'       </div>'
                            +'       <md-dialog-actions layout="row" layout-align="end center">'
                            +'           <md-button class="md-icon-button" ng-click="closeDialog()">'
                            +'               Cancel'
                            +'           </md-button>'
                            +'           <md-button ng-disabled="disableSave" class="md-icon-button" ng-click="savePassword()">'
                            +'               Save'
                            +'           </md-button>'
                            +'       </md-dialog-actions>'
                            +'   </md-dialog-content>'
                            +'</md-dialog>',
                        locals: {
                            user: $scope.user
                        },
                        controller: DialogController
                    });
                };

                $scope.editOrg = function($e) {
                    if(!$scope.orgs) {
                        $scope.orgs = wbConfig.config()
                        .then(function(config) {
                            return config.orgs;
                        });
                    }
                    pickOrganization($e);
                };

                //organization dialog
                function pickOrganization($event) {
                    var parentEl = angular.element(document.body);

                    function DialogController($scope, $mdDialog, user, organizations) {
                        $scope.orgSearch = "";
                        $scope.user = user;
                        $scope.organizations = organizations;

                        $scope.clearSearch = function() {
                            if ($scope.user.organization) {
                                $scope.orgSearch = $scope.user.organization.displayName;
                            } else {
                                $scope.orgSearch = null;
                            }
                        };

                        $scope.closeDialog = function() {
                            $mdDialog.hide();
                        };
                    }

                    $mdDialog.show({
                        parent: parentEl,
                        clickOutsideToClose: true,
                        preserveScope: true,
                        targetEvent: $event,
                        template: '<md-dialog class="user-organization-dialog md-raised" aria-label="List dialog">'
                            +'   <md-toolbar>'
                            +'       <div class="md-toolbar-tools">'
                            +'           <h2>Edit Organization</h2>'
                            +'           <span flex></span>'
                            +'           <md-button class="md-icon-button" ng-click="closeDialog()">'
                            +'               <md-icon md-svg-icon="close" aria-label="Close dialog"></md-icon>'
                            +'           </md-button>'
                            +'       </div>'
                            +'   </md-toolbar>'
                            +'   <md-dialog-content class="md-dialog-content">'
                            +'       <md-autocomplete md-floating-label="Search Organizations" md-selected-item="user.organization" md-search-text="orgSearch"'
                            +'        md-items="org in organizations|filter: {displayName: orgSearch}" md-item-text="org.displayName">'
                            +'           <span md-highlight-text="searchText">{{org.displayName}}</span>'
                            +'       </md-autocomplete>'
                            +'       <md-select ng-model="user.organization" aria-label="select organization"'
                            +'        ng-change="clearSearch()" placeholder="Select Organization">'
                            +'           <md-option ng-value="null"> None </md-option>'
                            +'           <md-option ng-value="org" ng-repeat="org in organizations"> {{org.displayName}}'
                            +'       </md-select>'
                            +'       <md-dialog-actions layout="row" layout-align="end center">'
                            +'           <md-button class="md-icon-button" ng-click="closeDialog()">'
                            +'               Done'
                            +'           </md-button>'
                            +'       </md-dialog-actions>'
                            +'   </md-dialog-content>'
                            +'</md-dialog>',
                        locals: {
                            user: $scope.user,
                            organizations: $scope.orgs
                        },
                        controller: DialogController
                    });
                }
            }
        };
    }]
);
