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
                latlngs.push([encounter.latitude, encounter.longitude]);
            }
        });

        map.addIndividuals(latlngs);
    }

    return {init: init};
})();