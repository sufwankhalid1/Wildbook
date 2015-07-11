/*!
 * AlertPlus v0.1.0 (https://github.com/crowmagnumb/alertplus)
 * Copyright 2015 CrowMagnumb
 * Licensed under MIT (https://github.com/crowmagnumb/alertplus/blob/master/LICENSE)
 */
var alertplus = (function () {
    var dialog = $("<div>").addClass("modal").addClass("fade").addClass("alertplus").attr("data-keyboard","true");
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
            showAlert(ex);
            return;
        }

        if  (ex instanceof Error) {
            displayError(ex.message, ex.stack);
            return;
        }

        //
        // Duck typing looking for a particular type of error message.
        //
        if (ex.status === 500) {
            if (ex.responseJSON) {
                displayError(ex.responseJSON.message, ex.responseJSON.totalStackTrace);
            } else {
                displayError(ex.message, ex.totalStackTrace);
            }
            return;
        }

        if (ex.status && ex.statusText) {
            displayError(ex.status + ": " + ex.statusText, ex.responseText);
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
