
wildbook.Model.MediaSubmission = wildbook.Model.BaseClass.extend({
    classNameShort: function() { return 'media.MediaSubmission'; },
    xurl: function() {
        return "obj/mediasubmission/save";
    }
});

wildbook.Collection.MediaSubmissions = wildbook.Collection.BaseClass.extend({
    model: wildbook.Model.MediaSubmission
});
