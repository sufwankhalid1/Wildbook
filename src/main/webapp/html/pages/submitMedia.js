//
// This makes it so that it appears in the chrome debugger as a source so
// you can set breakpoints and debug. It shows up under the "(no domain)" section.
// By default, scripts loaded via ajax (in this case the jquery.load() method will
// not appear in the debugger.
//
//# sourceURL=pages::submitMedia.js

var submitMedia = (function () {
    'use strict';

    function initUpload() {
        // Initialize the jQuery File Upload widget:
//        $('#fileupload').fileupload({
//            // Uncomment the following to send cross-domain cookies:
//            //xhrFields: {withCredentials: true},
//            url: 'placeholder'
//        });
        $('#fileupload').fileupload();
    
        // Enable iframe cross-domain access via redirect option:
    //    $('#fileupload').fileupload(
    //        'option',
    //        'redirect',
    //        window.location.href.replace(
    //            /\/[^\/]*$/,
    //            '/cors/result.html?%s'
    //        )
    //    );
    
        // Load existing files:
        $('#fileupload').addClass('fileupload-processing');
        $.ajax({
            // Uncomment the following to send cross-domain cookies:
            //xhrFields: {withCredentials: true},
            url: $('#fileupload').fileupload('option', 'url'),
            dataType: 'json',
            context: $('#fileupload')[0]
        }).always(function () {
            $(this).removeClass('fileupload-processing');
        }).done(function (result) {
            $(this).fileupload('option', 'done')
                   .call(this, $.Event('done'), {result: result});
        });
    }
    
    pager.register(
        "submitMedia",
        function() {
//            alert("onShow");
//            $('#fileupload').fileupload({
//                dataType: 'json',
//                done: function (e, data) {
//                    $.each(data.result.files, function (index, file) {
//                        console.log(document);
//                        $('<p/>').text(file.name).appendTo(document.body);
//                    });
//                }
//            });
            initUpload();
        },
        function() {
            $('#fileupload').fileupload('destroy');
        });
//    alert("onload");
    //pager.show("submitMedia");
})();
