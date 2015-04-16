
wildbook.Model.survey_Survey = wildbook.Model.BaseClass.extend({


	//classNameShort: function() { return 'survey.Survey'; },

});


wildbook.Collection.survey_Surveys = wildbook.Collection.BaseClass.extend({
	model: wildbook.Model.survey_Survey
});

