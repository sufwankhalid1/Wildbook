
wildbook.Model.Point = wildbook.Model.BaseClass.extend({

	idAttribute: '_id',  //magic via DataNucleus

	placeGoogleMarker: function(gmap, title) {
		return new google.maps.Marker({ map: gmap, position: this.toGoogleLatLng(), title: (title||'') });
	},

	centerGoogleMap: function(gmap) {
		gmap.setCenter(this.toGoogleLatLng());
	},

	toGoogleLatLng: function() {
		return new google.maps.LatLng(this.get('latitude'), this.get('longitude'));
	}
});


wildbook.Collection.Points = wildbook.Collection.BaseClass.extend({
	model: wildbook.Model.Point
});

