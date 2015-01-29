
wildbook.Model.Survey = wildbook.Model.BaseClass.extend({


	classNameShort: function() { return 'survey.Survey'; },

});


wildbook.Collection.Surveys = wildbook.Collection.BaseClass.extend({
	model: wildbook.Model.Survey
});

