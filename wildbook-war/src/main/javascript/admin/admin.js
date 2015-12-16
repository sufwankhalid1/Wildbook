angular.module('wildbook.admin', []);

require("../pages/encounterSearchPage.js");
require("../pages/mediaSubmitAdmin.js");
require("../pages/userAdmin.js");

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
