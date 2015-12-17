angular.module('wildbook.admin').directive(
    'locationEdit',
    function() {
        return {
            restrict: 'E',
            scope: {
                location: '='
            },
            templateUrl: 'util/location_edit.html'
        };
    }
);
