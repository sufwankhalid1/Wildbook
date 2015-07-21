var maptool = (function () {
    var iconIndividual = null;

    app.configPromise.then(function() {
        var config = app.config.maptool;
        if (config) {
            if (config.iconIndividual) {
                iconIndividual = L.icon(config.iconIndividual);
//                iconIndividual = L.icon({"iconUrl": "cust/images/individual-icon.png"});
            }
        }
    });

    var MapWrap = function(theMap) {
        var map = theMap;

        function addIndividuals(latlngs) {
            var markers = [];
            latlngs.forEach(function(latlng) {
                markers.push(L.marker(latlng, iconIndividual).addTo(map));
            });

            map.fitBounds(latlngs, {maxZoom: 8});

            return markers;
        }

        return {
            map: map,
            addIndividuals: addIndividuals
        };
    };


    function createMap(divId) {
        var map = L.map(divId, {scrollWheelZoom: false});

        L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; <a href="http://openstreetmap.org/copyright">OpenStreetMap</a> contributors',
            maxZoom: 18
        }).addTo(map);

        return new MapWrap(map);
    }

    return {
        createMap: createMap
    };
})();
