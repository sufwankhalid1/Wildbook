var maptool = (function () {
    function createMap(divId) {
        map = L.map(divId);

        L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; <a href="http://openstreetmap.org/copyright">OpenStreetMap</a> contributors',
            maxZoom: 18
        }).addTo(map);

        return map;
    }

    return {create: createMap};
})();
