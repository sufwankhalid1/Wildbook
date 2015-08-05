'use strict';

var individualPage = (function () {
    function init(photos, encounters) {
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
        var latlngs = [];
        if (encounters) {
            encounters.forEach(function(encounter) {
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
        }

        map.addIndividuals(latlngs);
    }

    return {init: init};
})();