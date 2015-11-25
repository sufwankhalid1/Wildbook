angular.module('wildbook.admin', []);

require("./encounter_edit.js");
require("./encounter_search.js");
require("./survey_edit.js");
require("./survey_search.js");
require("../pages/encounterSearchPage.js");
require("../pages/mediaSubmitAdmin.js");

angular.module('wildbook.admin').directive(
    'locationEdit',
    function() {
        return {
            restrict: 'A',
            scope: {
                location: '='
            },
            templateUrl: 'partials/location_edit.html'
        };
    }
);
