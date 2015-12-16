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

                //hide the close button on the search view
                $scope.showClose = false;

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
                        $scope.orgs = wbConfig.config().orgs;
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
                       +'        md-items="org in organizations" md-item-text="org.displayName">'
                       +'           <span md-highlight-text="searchText">{{org.displayName}}</span>'
                       +'       </md-autocomplete>'
                       +'       <md-select ng-model="user.organization" aria-label="select organization"'
                       +'        ng-change="clearSearch()" placeholder="Select Organization">'
                       +'           <md-option ng-value="org" ng-repeat="org in organizations|filter: orgSearch"> {{org.displayName}}'
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
                    $scope.user = user;
                    $scope.organizations = organizations;

                    $scope.closeDialog = function() {
                      $mdDialog.hide();
                    }
                  }
              }
            }
        }
    }]
);