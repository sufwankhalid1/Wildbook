angular.module('wildbook.admin', []);

require("../pages/encounterSearchPage.js");
require("../pages/mediaSubmitAdmin.js");

angular.module('wildbook.admin').directive(
    'locationEdit',
    function() {
        return {
            restrict: 'E',
            scope: {
                location: '='
            },
            templateUrl: 'partials/location_edit.html'
        };
    }
);
