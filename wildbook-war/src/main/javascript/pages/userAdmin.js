require('../user/user_search.js');

angular.module('wildbook.admin').directive(
    "wbUserAdmin",
    ["$http", "$q", "$exceptionHandler", "wbConfig", "$mdDialog",
     function ($http, $q, $exceptionHandler, wbConfig, $mdDialog) {
        return {
            restrict: 'E',
            templateUrl: 'pages/userAdmin.html',
            replace: true,
            controller: function($scope) {
                $scope.setUser = function(user) {
                    $http.get("useradmin/user/"+user.id)
                    .then(function(result) {
                        $scope.user = result.data;
                    });
                };

                $scope.clearUser = function() {
                    delete $scope.user;
                };

                $scope.newUser = function() {
                    $scope.user = {};
                };

                $scope.save = function() {
                    $http.post("useradmin/usersave", $scope.user);
                };

                $scope.editOrg = function($e) {
                    if(!$scope.orgs){
                        $scope.orgs = wbConfig.config()
                        .then(function(config) {
                            return config.orgs;
                        });;
                    }
                    pickOrganization($e);
                }

                //organization dialog
                function pickOrganization($event) {
                   var parentEl = angular.element(document.body);
                   $mdDialog.show({
                     parent: parentEl,
                     clickOutsideToClose: true,
                     preserveScope: true,
                     targetEvent: $event,
                     template:
                        '<md-dialog class="user-organization-dialog md-raised" aria-label="List dialog">'
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

                  function DialogController($scope, $mdDialog, user, organizations) {
                      $scope.orgSearch = "";
                      $scope.user = user;
                      $scope.organizations = organizations;

                      $scope.clearSearch = function() {
                          if($scope.user.organization) {
                              $scope.orgSearch = $scope.user.organization.displayName;
                          } else {
                              $scope.orgSearch = null;
                          }
                      }

                      $scope.closeDialog = function() {
                          $mdDialog.hide();
                      }
                  }
                }
                //change password dialog
                $scope.changeUserPassword = function($event) {
                  var parentEl = angular.element(document.body);
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
                function DialogController($scope, $mdDialog, user) {
                  $scope.password = null;
                  $scope.verifyPassword = "";
                  $scope.disableSave = true;

                  $scope.$watch('verifyPassword', function(newVal, oldVal) {
                    if(newVal && newVal.length > 5 && newVal == $scope.password) {
                      $scope.disableSave = false;
                    } else {
                      $scope.disableSave = true;
                    }
                  });

                  $scope.savePassword = function() {

                    if($scope.password == $scope.verifyPassword && $scope.password.length >= 6) {
                      if($scope.password.length < 6) {
                        return;
                      }
                      $http.post('useradmin/editpw/'+user.userId, $scope.password);
                    } 

                    $mdDialog.hide();
                  }

                  $scope.closeDialog = function() {
                    $mdDialog.hide();
                  }
                }
              }
            }
        }
    }]
);