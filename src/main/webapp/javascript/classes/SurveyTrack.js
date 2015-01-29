
wildbook.Model.SurveyTrack = wildbook.Model.BaseClass.extend({

	classNameShort: function() { return 'survey.SurveyTrack'; },

});


wildbook.Collection.SurveyTracks = wildbook.Collection.BaseClass.extend({
	model: wildbook.Model.SurveyTrack
});

