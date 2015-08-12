var maptool = (function () {
    var config = null;
    var encIcons = {};

    function init(conf) {
        config = conf;
    }

    var MapWrap = function(theMap) {
        var map = theMap;

        var voyages;
        var currentPopup = null;

        var encounters;

        function getEncounterLayer() {
            if (encounters) {
                return encounters;
            }

            encounters = new L.MarkerClusterGroup({
                iconCreateFunction: function(cluster) {
                    var iconDef = config.encounter.icons.cluster;
                    return new L.divIcon({className: 'individual-cluster',
                                          iconSize: iconDef.iconSize,
                                          iconAnchor: iconDef.iconAnchor,
                                          html: '<div class="individual-cluster-count"><span>'
                                              + cluster.getChildCount()
                                              + '</span></div><img src="'
                                              + iconDef.iconUrl + '"/>'});
                }
            });
            map.addLayer(encounters);
            return encounters;
        }

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

        function getEncounterIcon(species) {
            if (encIcons[species]) {
                return encIcons[species];
            }

            if (config.encounter.icons[species]) {
                var icon = {icon: L.icon(config.encounter.icons[species])};
                encIcons[species] = icon;
                return icon;
            }

            getEncounterIcon("default");
        }

        function getMarker(latlng, icon, popup) {
            var marker = L.marker(latlng, icon);
            if (popup) {
                marker.bindPopup(popup);

                marker.on('mouseover', function (evt) {
                    closeCurrentPopup();
                    this.openPopup();
                });
            }

            return marker;
        }

        function addEncounter(encounter) {
            //
            // Just passed in a latlng because we have no other info. Also use default icon.
            // Later, we can make sure we pass in a species somehow if we have it.
            //
            var layer = getEncounterLayer();

            if (Array.isArray(encounter)) {
                layer.addLayer(getMarker(encounter, getEncounterIcon("default")));
                return;
            }

            if (! encounter.latitude || ! encounter.longitude) {
                return;
            }

            var iconIndividual;
            var popup = $("<div>");
            if (encounter.individual) {
                popup.append(app.beingDiv(encounter.individual));
                popup.append($("<span>").addClass("sight-date-text").text(encounter.individual.displayName));
                iconIndividual = getEncounterIcon(encounter.individual.species);
            } else {
                popup.append($("<span>").addClass("sight-date-text").text("<Unknown>"));
                iconIndividual = getEncounterIcon("default");
            }

            popup.append($("<br>"));
            popup.append("Sighted: ")
            popup.append($("<span>").addClass("sight-date").text(moment(encounter.dateInMilliseconds).format('LL')));
            popup.append($("<br>"));
            popup.append($("<span>").addClass("sight-date-text").text(encounter.verbatimLocation));
            popup.append($("<br>"));
            popup.append("by: ");
            popup.append(app.beingDiv(encounter.submitter));

            layer.addLayer(getMarker([encounter.latitude, encounter.longitude], iconIndividual, popup[0]));
        }

        function addVoyage(points, popup) {
            var vPoints = [];
            points.forEach(function(point) {
                vPoints.push([point.latitude, point.longitude]);
            })

            L.polyline(vPoints, {color: 'red'}).addTo(map);
        }

        return {
            map: map,
            addEncounter: addEncounter,
            addVoyage: addVoyage,
            fitToData: function(maxZoom) {
                var data = [];
                if (encounters) {
                    data.push(encounters);
                }

                if (voyages) {
                    data.push(voyages);
                }

                if (data.length === 0) {
                    map.fitWorld();
                    return;
                }

                if (! maxZoom) {
                    maxZoom = 8;
                }

                var group = new L.featureGroup(data);
                map.fitBounds(group.getBounds(), {maxZoom: maxZoom});
            },
            clear: function() {
                map.removeLayer(encounters);
                map.removeLayer(voyages);
                encounters = null;
                voyages = null;
            }
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
        init: init,
        createMap: createMap
    };
})();
