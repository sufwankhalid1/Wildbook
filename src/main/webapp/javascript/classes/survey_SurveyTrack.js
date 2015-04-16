
wildbook.Model.survey_SurveyTrack = wildbook.Model.BaseClass.extend({
	idAttribute: '_id',  //magic via DataNucleus

	//classNameShort: function() { return 'survey.SurveyTrack'; },

});


wildbook.Collection.survey_SurveyTracks = wildbook.Collection.BaseClass.extend({
	model: wildbook.Model.survey_SurveyTrack
});

