wildbook.app.directive('locationEdit', function() {
    return {restrict: 'E',
        scope: {
            location: '='
        },
        templateUrl: 'util/render?j=partials/location_edit.jade'
    };
});
