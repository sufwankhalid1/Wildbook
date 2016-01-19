/* globals angular, alertplus, XMLHttpRequest, FileReader */
'use strict';

require('../user/user_edit_fields');

angular.module('wildbook.admin').directive(
    'wbMyAccountPage',
    ["$http", "$exceptionHandler", "$mdToast", "$timeout", "Blob", "FileSaver",
     function($http, $exceptionHandler, $mdToast, $timeout, Blob, FileSaver) {
        return {
            restrict: 'E',
            templateUrl: 'pages/myAccountPage.html',
            replace: true,
            link: function($scope, element, attr) {
                var origSelf;
                $scope.finishedExports = 0;
                $scope.pendingExports = 0;
                $scope.exportErrors = 0;

                $http.get('obj/user/self').then(function(response){
                    $scope.self = response.data;
                    origSelf = angular.copy(response.data);
                });

                $scope.getExports = function() {
                    $scope.finishedExports = 0;
                    $scope.pendingExports = 0;
                    $scope.exportErrors = 0;

                    $http.get('obj/user/exports').then(function(response){
                        $scope.exports = response.data;

                        $scope.exports.forEach(function(item){
                            if (item.status === 2) {
                                $scope.finishedExports++;
                            }

                            if (item.status === 1) {
                                $scope.pendingExports++;
                            }

                            if (item.error) {
                                $scope.exportErrors++;
                            }

                        });
                    });
                };

                $scope.$watch('refresh', function(){
                    function refreshExports() {
                        if (!$scope.refresh) {
                            return false;
                        }

                        $scope.getExports();
                        refreshTimeout();
                    }

                    function refreshTimeout(){
                        $timeout(function(){
                            refreshExports();
                        }, 5000);
                    }

                    refreshExports();
                });

                $scope.timestampToDate = function(timestamp) {
                    if (!timestamp) {
                        return "Not Set";
                    }
                    var date = new Date(timestamp);
                    var hours = date.getHours();
                    var minutes = "0" + date.getMinutes();
                    var seconds = "0" + date.getSeconds();
                    return hours + ':' + minutes.substr(-2) + ':' + seconds.substr(-2);
                };

                $scope.hideExports = function() {
                    $scope.showExports = false;
                };

                $scope.save = function() {
                    $http.post("useradmin/usersave", $scope.self)
                    .then(function(response) {
/*                        $mdToast.show(
                            $mdToast.simple()
                                .content('Info Updated!')
                                .position('middle')
                                .hideDelay(3000)
                        )*/
                        $scope.edit = false;
                        origSelf = angular.copy($scope.self);
                    });
                };

                $scope.viewError = function(err) {
                    alertplus.error(err);
                };

                $scope.cancel = function() {
                    $scope.edit = false;
                    $scope.self = angular.copy(origSelf);
                };

                $scope.downloadObj = [];
                $scope.download = function(exportitem, id) {
                    var currentDownload = {id: id, progress: 0};
                    $scope.downloadObj.push(currentDownload);

                    var header;

                    function updateProgress(evt) {
                        if (header) {
                            for (var ii = 0; ii < $scope.downloadObj.length; ii++){
                                if ($scope.downloadObj[ii].id === id) {
                                    $scope.downloadObj[ii].progress = (evt.loaded / header)*100;
                                }
                            }
                        }
                    }

                    var url = "export/download/" + exportitem.exportId;
                    var filename = exportitem.type + ".zip";

                    //
                    // Using old school XMLHttpRequest because angular ($http) AND jquery ($.ajax)
                    // both give the WRONG result, the number of bytes are too small. And a fix
                    // submitted by another user for a similar issue (though his response size was too large)
                    // in which he passed it through a Uint8Array, does not work here. I have no idea why.
                    //
                    var xhr = new XMLHttpRequest();
                    xhr.open('GET', url, true);
                    xhr.responseType = "blob";
                    xhr.withCredentials = true;
                    xhr.onprogress = updateProgress;
                    xhr.onreadystatechange = function () {
                        if (xhr.readyState === 2) {
                            header = xhr.getResponseHeader("blobsize");
                        }
                        if (xhr.readyState === 4) {
                            var blob = xhr.response;
                            if (xhr.status === 200) {
                                FileSaver.saveAs(blob, filename);
                                $scope.refresh = true;
                            } else {
                                var reader = new FileReader();
                                reader.onload = function(){
                                    alertplus.error(JSON.parse(reader.result));
                                };
                                reader.readAsText(blob);
                            }
                        }
                    };
                    xhr.send();

                    // $http({url: url, withCredentials: true})
                    // .then(function(response) {
                    //     var data = response.data;
                    //     function str2bytes(str) {
                    //         var bytes = new Uint8Array(str.length);
                    //         for (var ii = 0; ii < str.length; ii++) {
                    //             bytes[ii] = str.charCodeAt(ii);
                    //         }
                    //         return bytes;
                    //     }
                    //     console.log(data.length);
                    ////     var blob = new Blob(data], {type: 'application/octet-stream'});
                    //     var blob = new Blob([str2bytes(data)], {type: 'application/octet-stream'});
                    //     console.log(blob.size);
                    //     FileSaver.saveAs(blob, filename);
                    //     $scope.refresh = true;
                    // }, $exceptionHandler);
                };

                $scope.getExports();
            }
        };
    }]
);
