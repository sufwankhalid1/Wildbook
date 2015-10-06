wildbook.app.directive('locationEdit', function() {
    return {restrict: 'A',
        scope: {
            location: '='
        },
        templateUrl: 'util/render?j=partials/location_edit.jade'
    };
});
