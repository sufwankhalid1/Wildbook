/*!
 * AlertPlus v0.1.4 (https://github.com/crowmagnumb/alertplus)
 * Copyright 2015 CrowMagnumb
 * Licensed under MIT (https://github.com/crowmagnumb/alertplus/blob/master/LICENSE)
 */
var alertplus = (function () {
    var dialog = $("<div>").addClass("modal").addClass("fade").addClass("alertplus").attr("tabindex","-1");
    dialog.appendTo('body');

    var content = $("<div>").addClass("modal-content");
    $("<div>").addClass("modal-dialog").append(content).appendTo(dialog);

    var titleDiv = $("<h4>").addClass("modal-title").addClass("alert");
    $("<div>").addClass("modal-header")
              .append($("<button>").addClass("close")
                      .attr("type", "button")
                      .attr("data-dismiss", "modal")
                      .attr("aria-hidden", "true")
                      .html("&times;"))
              .append(titleDiv)
              .appendTo(content);

    var messageArea = $("<div>").addClass("messagearea");
    var detailsButton = $("<button>").addClass("btn").css("display", "none");// &gt;&gt;");
    var leftChev = $("<span>").addClass("glyphicon").addClass("glyphicon-chevron-left").css("display", "none").appendTo(detailsButton);
    $("<span>").text("Details").appendTo(detailsButton);
    var rightChev = $("<span>").addClass("glyphicon").addClass("glyphicon-chevron-right").appendTo(detailsButton);
    var detailsContainer = $("<div>").css("display", "none").addClass("detailsarea");
    var detailsContent = $("<pre>");
    detailsContainer.append(detailsContent);

    $("<div>").addClass("modal-body")
              .append(messageArea)
              .append(detailsButton)
              .append(detailsContainer).appendTo(content);
    $("<div>").addClass("modal-footer")
         .append($("<button>").addClass("btn")
                              .addClass("btn-primary")
                              .attr("data-dismiss", "modal")
                              .attr("type", "button")
                              .text("OK"))
          .appendTo(content);


    detailsButton.button();
    detailsButton.click( function(evt) {
        detailsContainer.toggle();
        leftChev.toggle();
        rightChev.toggle();
    } );

    function showAlert(message, details, title, dialogClass) {
        //
        // Remove all possible other classes that could have been applied.
        //
        titleDiv.removeClass("alert-danger");
        titleDiv.removeClass("alert-info");

        leftChev.hide();
        rightChev.show();

        //
        // Now add in the passed class.
        //
        if (dialogClass) {
            titleDiv.addClass("alert-" + dialogClass);
        } else {
            titleDiv.addClass("alert-info");
        }

        if (details) {
            detailsButton.show();
        } else {
            detailsButton.hide();
        }

        messageArea.html( message );
        if (!title) {
            titleDiv.text("Information");
        } else {
            titleDiv.text(title);
        }

        detailsContainer.hide();
        detailsContent.html(details);

        dialog.modal('show');
    }

    function displayError(message, details) {
        showAlert(message, details, "Error", "danger");
    }

    function showError(ex) {
        if (typeof ex == "string") {
            displayError(ex);
            return;
        }

        if  (ex instanceof Error) {
            displayError(ex.message, ex.stack);
            return;
        }


        //
        // Various duck typings looking for a particular type of error objects.
        //
        if (ex.responseJSON && ! $.isEmptyObject(ex.responseJSON)) {
            if (ex.responseJSON.stack) {
                displayError(ex.responseJSON.message, ex.responseJSON.stack);
            } else {
                displayError(ex.responseJSON.message, ex.responseJSON.totalStackTrace);
            }
            return;
        }

        //
        // I was going to check for status = 500 and only then just show responsText but
        // I think it makes sense to ignore all statuses if we have an actual responseText
        // which hopefully is much more specific. Indeed, my intended usage for this is
        // to have this be the way to display a simple message that is not a system error but rather
        // a user error. I can't find a specific html code for that or I would use it. But at least
        // this way a basic 500 error that is not JSON formatted will be assumed to be an informational
        // message with the message in responseText.
        //
        var rtNotEmpty = (ex.responseText && ex.responseText !== "{}");

        if (rtNotEmpty) {
            displayError(ex.responseText);
            return;
        }

        //
        // In some Ajax calls we can get status = 0 and statusText = "error". Not very helpful but
        // it is all we have to go on in this case. So basically I can't just check for (ex.status)
        // I need to check if it is defined.
        //
        if (ex.status !== undefined && ex.statusText) {
            displayError(ex.status + ": " + ex.statusText, rtNotEmpty ? ex.responseText : null);
            return;
        }

        if (ex.message && ex.totalStackTrace) {
            displayError(ex.message, ex.totalStackTrace);
            return;
        }

        if (ex.message) {
            var message;
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
            } else {
                if (ex.status) {
                    message = "Error " + ex.status + ": " + ex.message;
                } else {
                    message = ex.message;
                }
            }
            displayError(message, ex.details);
            return;
        }

        //
        // You got me. Punt. Just convert to string and display.
        //
        displayError(ex.toString());
    }

    return {alert: showAlert,
            error: showError};
}());
