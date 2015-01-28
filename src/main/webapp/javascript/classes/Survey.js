
wildbook.Model.Survey = wildbook.Model.BaseClass.extend({


	classNameShort: function() { return 'survey.Survey'; },

	refClass: {
		tracks: 'SurveyTracks',
	},

	///TODO this is temporary til we figure out how to this nicerly
	getTracks: function(callback) {
		this.fetchSub('tracks', { jdoql: 'SELECT FROM org.ecocean.survey.SurveyTrack', success: callback });
	}

});


wildbook.Collection.Surveys = wildbook.Collection.BaseClass.extend({
	model: wildbook.Model.Survey
});

