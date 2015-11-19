wildbook.app.directive('locationEdit', function() {
    return {restrict: 'A',
        scope: {
            location: '='
        },
        templateUrl: 'partials/location_edit.html'
    };
});
