
wildbook.Model.Occurrence = wildbook.Model.BaseClass.extend({

	idAttribute: 'occurrenceID',  //magic via DataNucleus

});


wildbook.Collection.Occurrences = wildbook.Collection.BaseClass.extend({
	model: wildbook.Model.Occurrence
});

