var alertplus = (function () {
    var dialog = $("<div>").css("display", "none")
    var messageArea = $("<pre>");
    dialog.append($("<div>").append(messageArea).addClass("messagearea"));
    dialog.append($("<br>"));
    var detailsButton = $("<button>").css("display", "none").html("Details &gt;&gt;");
    dialog.append(detailsButton);
    var detailsContainer = $("<div>").css("display", "none").addClass("detailsarea");
    var detailsContent = $("<pre>");
    detailsContainer.append(detailsContent);
    dialog.append(detailsContainer);

    function showAlert(message, details) {
        //
        //   Positioning at top so that when the details are clicked we can
        //   expand the form to show the whole details without it going off
        //   the screen and the user having to move the form with the mouse.
        //
        dialog.dialog({
            autoOpen: true,
            dialogClass: "alertplus",
            modal: true,
            title: "Error",
            closeOnEscape: true,
            buttons: { "OK": function() { $(this).dialog("close"); } },
            open: function() {
                detailsButton.button();
                detailsButton.click( function(e) {
                    detailsContainer.toggle();
                } );

                if (details) {
                    detailsButton.show();
                } else {
                    detailsButton.hide();
                }

                messageArea.html( message );

                detailsContainer.hide();
                detailsContent.html(details);
            },
            width: 600,
            appendTo: "body",
            resizable: false
        });
    }

    function showError(ex) {
        var message;
        var details;
        if (ex.status === 500) {
            if (ex.responseJSON) {
                message = ex.responseJSON.message;
                details = ex.responseJSON.totalStackTrace;
            } else {
                message = ex.message;
                details = ex.totalStackTrace;
            }
        } else {
            if (ex.message) {
                if (ex.message.indexOf("<html>") >= 0) {
                    //
                    // Extract just <body> from it because otherwise any styles inside
                    // of the document can mess up your parent document.
                    //
                    var start = ex.message.indexOf("<body>");
                    var end = ex.message.indexOf("</body>");
                    if (start > 0 && end > 0) {
                        message = ex.message.slice(start + 6, end);
                    } else {
                        message = ex.message;
                    }

                    if (ex.status) {
                        message = "<h2>Error " + ex.status + "</h2>" + message;
                    }

                    details = ex.details;
                } else {
                    if (ex.status) {
                        message = "Error " + ex.status + ": " + ex.message;
                    } else {
                        message = ex.message;
                    }
                    details = ex.details;
                }
            } else {
                message = "Error " + ex.status + ": " + ex.statusText;
                details = null;
            }
        }

        showAlert(message, details);
    }

    return {alert: showAlert,
            error: showError};
}());
