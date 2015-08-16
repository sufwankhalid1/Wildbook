$(document).ready(function() {
    $.get("obj/mediasubmission/get/status")
    .then(function(data) {
        $("#mstable").DataTable({
            data: data,
            columns: [
                {
                    data: 'id',
                    title: 'Edit'
                },
                {
                    data: 'timeSubmitted',
                    title: 'Submitted',
                    render: function(timeSubmitted) {
                        return moment(timeSubmitted).format('lll');
                    }
                },
                {
                    data: 'user',
                    title: 'Submitted By',
                    render: function(user) {
                        if (user) {
                            return user.displayName;
                        }
                        return null;
                    }
                },
                {
                    data: 'submissionid',
                    title: "Survey ID"
                },
                {
                    data: 'description',
                    title: "Description"
                },
                {
                    data: 'verbatimLocation',
                    title: "Location"
                },
                {
                    data: 'status',
                    title: 'Status'
                }
                ]
        });
    }, function(error) {
        alertplus.error(error);
    });
});
