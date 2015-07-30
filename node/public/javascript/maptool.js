var maptool = (function () {
    var iconIndObj = null;
    var iconIndividual = null;

    app.configPromise.then(function() {
        var config = app.config.maptool;
        if (config) {
            iconIndObj = config.iconIndividual;
            if (config.iconIndividual) {
                iconIndividual = {icon: L.icon(config.iconIndividual)};
            }
        }
    });

    var MapWrap = function(theMap) {
        var map = theMap;
        var currentPopup = null;

        map.on("popupopen", function(evt) {
            currentPopup = evt.popup;

            //
            // This is not working, I don't know why. Oh well.
            //
            $(currentPopup._container).keypress(function(evt) {
                if (evt.keyCode == 27) {
                    closeCurrentPopup();
                }
            });

            //
            // Not sure I want this afterall. I think it's fine to leave the popup on until
            // they close it (or open another popup)
            //
//            currentPopup._container.onmouseleave = function(evt) {
//                closeCurrentPopup();
//            };
        });

        function closeCurrentPopup() {
            if (!currentPopup) {
                return;
            }

            currentPopup._source.closePopup();
            currentPopup = null;
        }

        //
        // To close the current popup. popup source is marker. can also do marker.closePopup.
        //
//        if (currentPopup != null) currentPopup._source.closePopup();

        function addIndividuals(latlngs, maxZoom) {
            var markers = new L.MarkerClusterGroup({
                iconCreateFunction: function(cluster) {
                    return new L.divIcon({className: 'individual-cluster',
                                          iconSize: iconIndObj.iconSize,
                                          iconAnchor: iconIndObj.iconAnchor,
                                          html: '<div class="individual-cluster-count"><span>' + cluster.getChildCount() + '</span></div><img src="' + iconIndObj.iconUrl + '"/>'});
                }
            });

            var boundPoints = [];
            latlngs.forEach(function(latlng) {
                var marker;
                if (Array.isArray(latlng)) {
                    boundPoints.push(latlng);
                    marker = L.marker(latlng, iconIndividual);
                } else {
                    boundPoints.push(latlng.latlng);
                    marker = L.marker(latlng.latlng, iconIndividual);

                    if (latlng.popup) {
                        marker.bindPopup(latlng.popup);

                        marker.on('mouseover', function (e) {
                            closeCurrentPopup();
                            this.openPopup();
                        });
                    }

                    //
                    // Want to be able to mouse over and click on things in the popup
                    //
//                    marker.on('mouseout', function (evt) {
//                        this.closePopup();
//                    });
                }
//                markers.push(marker.addTo(map));
                markers.addLayer(marker);
            });

            if (!maxZoom) {
                maxZoom = 8;
            }
            map.addLayer(markers);
            map.fitBounds(boundPoints, {maxZoom: maxZoom});

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
