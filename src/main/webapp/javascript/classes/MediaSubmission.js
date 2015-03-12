
wildbook.Model.MediaSubmission = wildbook.Model.BaseClass.extend({
    classNameShort: function() { return 'media.MediaSubmission'; },
});

wildbook.Collection.MediaSubmissions = wildbook.Collection.BaseClass.extend({
    model: wildbook.Model.MediaSubmission
});

