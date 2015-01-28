
wildbook.Model.SurveyTrack = wildbook.Model.BaseClass.extend({


	classNameShort: function() { return 'survey.SurveyTrack'; },

	refClass: {
		points: 'Points',
	},

	///TODO this is temporary til we figure out how to this nicerly
	getPoints: function(callback) {
		this.fetchSub('points', { jdoql: 'SELECT FROM org.ecocean.Point', success: callback });
	}


});


wildbook.Collection.SurveyTracks = wildbook.Collection.BaseClass.extend({
	model: wildbook.Model.SurveyTrack
});

