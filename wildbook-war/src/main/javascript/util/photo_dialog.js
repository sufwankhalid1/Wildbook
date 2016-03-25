/* global angular, document */
'use strict';

angular.module('wildbook.util').directive(
    'photoDialog',
    ['$http', '$mdDialog', function($http, $mdDialog) {
        return {
            restrict: 'A',
            scope: {
                data: '=',
                type: '@',
                showBtn: '@?',
                icon: '@?',
                photos: '=?',
                photo: '=',
                avatar: '='
            },
            templateUrl: 'util/photo_dialog.html',
            link: function($scope, ele, attr) {
                $scope.avatarDialog = function($event) {

                    function avatarController($scope, $mdDialog) {
                        console.log($scope.data);

                        var type = $scope.type.toLowerCase();

                        $scope.active = [];

                        if (type === "individual") {
                            $http.get("api/" + type + "/photos/" + $scope.data.id)
                            .then(function(photos) {
                                $scope.photos = photos.data;
                            });
                        }

                        $scope.selected = function(avatar) {
                            $scope.avatar = avatar.thumbUrl;
                            $scope.photo = avatar;
                            console.log($scope.data);
                            $scope.closeDialog();
                        };

                        $scope.closeDialog = function() {
                            $mdDialog.hide();
                        };
                    }

                    $mdDialog.show({
                         parent: angular.element(document.body),
                         targetEvent: $event,
                         clickOutsideToClose:true,
                         preserveScope: true,
                         scope: $scope,
                         template:
                           '<md-dialog class="individual-avatar-container" aria-label="List dialog">' +
                           '    <md-toolbar>' +
                           '        <div class="md-toolbar-tools">' +
                           '            <h2>{{type}} Avatar</h2>' +
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
                         controller: avatarController
                    });
                };
            }
        };
}]);
