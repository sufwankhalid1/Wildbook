angular.module('wildbook.admin').directive(
    'wbIndividualEdit',
    ["$http", "$exceptionHandler", "wbConfig", "wbEncounterUtils", "$mdDialog",
     function($http, $exceptionHandler, wbConfig, wbEncounterUtils, $mdDialog) {
        return {
            restrict: 'E',
            scope: {
                data: "=indData",
                editIndividualDone: "&"
            },
            templateUrl: 'partials/individual_edit.html',
            replace: true,
            controller: function($scope) {
                $scope.module = {};
                $scope.getSpecies = function() {
                    return wbConfig.config().species;
                };
                
//                function getDisplayName(individual) {
//                    var name = individual.nickname || "[Unnamed]";
//                    if (! individual.alternateId) {
//                        return name;
//                    }
//                    
//                    return name + " (" + individual.alternateId + ")";
//                }
//                
                $scope.avatarDialog = function($event) {
                   var parentEl = angular.element(document.body);
                   $mdDialog.show({
                         parent: parentEl,
                         targetEvent: $event,
                         clickOutsideToClose:true,
                         template:
                           '<md-dialog class="individual-avatar-container" aria-label="List dialog">' +
                           '    <md-toolbar>' +
                           '        <div class="md-toolbar-tools">' +
                           '            <h2>Individual Avatar</h2>' +
                           '            <span flex></span>' +
                           '            <md-button class="md-icon-button" ng-click="closeDialog()">' +
                           '                <md-icon md-svg-icon="close" aria-label="Close dialog"></md-icon>' +
                           '            </md-button>' +
                           '        </div>' +
                           '    </md-toolbar>' +
                           '    <md-dialog-content layout-align="center center" class="individual-avatar md-dialog-content" layout="row" layout-wrap>' +
                           '        <div ng-click="selected(photo)"' +
                           '        class="mlrb-8" ng-repeat="photo in photos">' +
                           '            <img ng-src="{{photo.thumbUrl}}">'+
                           '        </div>'+
                           '    </md-dialog-content>' +
                           '</md-dialog>',
                         locals: {
                            parentScope: $scope,
                            individual: $scope.data
                         },
                         controller: avatarController
                  });

                function avatarController($scope, $mdDialog, individual, parentScope) {
                    $scope.active = [];

                    $http.get("obj/individual/photos/"+individual.id)
                    .then(function(photos){
                        $scope.photos = photos.data;
                    });

                    $scope.selected = function(avatar) {
                        parentScope.data.avatar = avatar.thumbUrl;
                        parentScope.data.avatarid = avatar.id;

                        $scope.closeDialog();
                    };

                    $scope.closeDialog = function() {
                        $mdDialog.hide();
                    };
                  };
              }

                $scope.save = function() {
                    $http.post('obj/individual/save', $scope.data)
                    .then(function(result) {
                        $scope.data.id = result.data.id;
                        //
                        // Reset the display name to the new one.
                        //
                        //$scope.data.displayName = getDisplayName($scope.data);
                        $scope.data.displayName = result.data.displayName;
                        
                        $scope.editIndividualDone({individual: $scope.data});
                    }, $exceptionHandler);
                };
                
                //
                // wb-key-handler-form
                //
                $scope.cancel = function() {
                    $scope.editIndividualDone({individual: null});
                }
                
                $scope.cmdEnter = function() {
                    $scope.save();
                }
            }
        };
    }]
);
