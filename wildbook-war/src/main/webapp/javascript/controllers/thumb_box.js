wildbook.app.directive(
    'wbThumbBox',
    ["wbDateUtils", "keyEvents", function(wbDateUtils, keyEvents) {
        function getKeySetup(priority) {
            return function(scope, element) {
                return keyEvents.handler(priority ? parseInt(priority) : 100)
                .keydown(
                    function handleKeyDown(event) {
                        if (event.is.leftarrow) {
                            scope.$applyAsync(scope.panLeft);
                            return false;
                        }
                        if ( event.is.rightarrow ) {
                            scope.$applyAsync(scope.panRight);
                            return false;
                        }
                    }
                );
            };
        }
        
        return {
            restrict: 'E',
            scope: {
                photos: "=",
                delphoto: "&",
                numPhotos: "@"
            },
            link: function(scope, element, attrs) {
                KeyEventHandler.link(getKeySetup(attrs.wbKeyHandlerPriority), scope, element, attrs);
            },
            templateUrl: 'util/render?j=partials/wb_thumb_box',
            replace: true,
            controller: function($scope) {
                var startIdx = 0;
                var idx;
                
                var numPhotos;
                if (! $scope.numPhotos) {
                    numPhotos = 18;
                } else {
                    numPhotos = parseInt($scope.numPhotos);
                }

                $scope.getTimestamp = function(photo) {
                    return wbDateUtils.dateStringFromRest(photo.timestamp);
                }
                
                function pageLeft() {
                    startIdx = startIdx - numPhotos;
                    if (startIdx < 0) {
                        startIdx = 0;
                    }
                }
                
                $scope.panLeft = function() {
                    if (! $scope.photos) {
                        return;
                    }
                    if ($scope.zoomimage) {
                        if (idx <= 0) {
                            return;
                        }
                        idx--;
                        $scope.zoomimage = $scope.photos[idx];
                        
                        //
                        // If we have panned far enough such that we are now on the next
                        // "page" of thumbnails, let's reflect that.
                        //
                        if (idx < startIdx) {
                            pageLeft();
                        }
                    } else {
                        pageLeft();
                    }
                }

                function pageRight() {
                    startIdx = startIdx + numPhotos;
                    if (startIdx + numPhotos > $scope.photos.length) {
                        startIdx = $scope.photos.length - numPhotos;
                        if (startIdx < 0) {
                            startIdx = 0;
                        }
                    }
                }
                
                $scope.panRight = function() {
                    if (! $scope.photos) {
                        return;
                    }
                    if ($scope.zoomimage) {
                        if (idx >= $scope.photos.length - 1) {
                            return;
                        }
                        idx++;
                        $scope.zoomimage = $scope.photos[idx];
                        
                        //
                        // If we have panned far enough such that we are now on the next
                        // "page" of thumbnails, let's reflect that.
                        //
                        if (idx >= startIdx + numPhotos) {
                            pageRight();
                        }
                    } else {
                        pageRight();
                    }
                }
                
                $scope.isLeftDisabled = function() {
                    if ($scope.zoomimage) {
                        return (idx <= 0);
                    }
                    return (startIdx <= 0);
                }
                
                $scope.isRightDisabled = function() {
                    if ($scope.zoomimage) {
                        return (idx >= $scope.photos.length - 1);
                    }
                    
                    if (! $scope.photos) {
                        return true;
                    }
                    return (startIdx >= $scope.photos.length - numPhotos);
                }
                
                $scope.viewImage = function(photo) {
                    $scope.zoomimage = photo;
                    
                    $scope.photos.forEach(function(value, index) {
                        if (value.id === photo.id) {
                            idx = index;
                        }
                    });
                }
                
                $scope.getVisPhotos = function() {
                    if (! $scope.photos) {
                        return [];
                    }
                    return $scope.photos.slice(startIdx, startIdx + numPhotos);
                }
                
                $scope.removePhoto = function(id) {
                    return alertplus.confirm('Are you sure you want to delete this image?', "Delete Image", true)
                    .then(function() {
                        $scope.delphoto({id: id})
                        .then(function() {
                            $scope.zoomimage = null;
                            $scope.photos = $scope.photos.filter(function(photo) {
                                return (photo.id !== id);
                            });
                        });
                    });
                }
                
                //
                // wb-key-handler-form
                //
                $scope.cancel = function() {
                    $scope.zoomimage = null;
                }
                
                $scope.cmdEnter = function() {
                    // do nothing
                    // want this here to override any parent scope cmdEnter event though.
                }
            }
        };
    }]
);
