'use strict';

var individualPage = (function () {
    function init(photos, encounters, voyages) {
        // build photos array for phototool
        var fotos = [];

        if (photos) {
            photos.forEach(function(photo){
                fotos.push({src: app.config.wildbook.staticUrl + photo.url, w: 0, h:0});
            });
        }

        phototool.setPhotos(fotos);

        var map = maptool.createMap('map-sightings');
        //
        // Add encounters to map and set view to be centered around these encounters.
        //
        var individuals = [];
        if (encounters) {
            encounters.forEach(function(encounter) {
                map.addEncounter(encounter);
            });
        }

        if (voyages) {
            voyages.forEach(function(voyage) {
                var popup = $("<div>");
                popup.append($("<span>").addClass("sight-data-text").text(voyage.name));
                map.addVoyage(voyage.points, popup);
            });
        }

        map.fitToData();
    }

    return {init: init};
})();