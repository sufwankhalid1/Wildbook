'use strict';

var individualPage = (function () {
    function init(data) {
        var map = maptool.createMap('map-sightings');
        //
        // Add encounters to map and set view to be centered around these encounters.
        //
        var latlngs = [];
        data.encounters.forEach(function(encounter) {
            if (encounter.latitude && encounter.longitude) {
                var popup = $("<div>");
                popup.append($("<span>").addClass("sight-date").text(moment(encounter.dateInMilliseconds).format('LL')));
                popup.append($("<br>"));
                popup.append($("<span>").addClass("sight-date-text").text(encounter.verbatimLocation));
                popup.append($("<br>"));
                popup.append(app.userDiv(encounter.submitter));
                latlngs.push({latlng: [encounter.latitude, encounter.longitude],
                              popup: popup[0]});
            }
        });

        map.addIndividuals(latlngs);
    }

    return {init: init};
})();