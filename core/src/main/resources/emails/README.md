# Email Templates

## Jade setup

### General
Emails are organized in folders where the folder name is used to generate the email. Each email has a body.jade template and a subject.txt file for its subject.

Each email includes the mixins file, and extends the generic email layout under partials/layout:

```
include ../../partials/mixins.jade
extends ../../partials/layout
```

```block content``` is used to add content inside the email body:
```
block content
    include ../../partials/message-header.jade
    +container()
        p One of your submissions has been matched to a known #{individual.species.name}. Thank you for providing us information that will help scientists track the health and wellbeing of wildlife.
        p
            h5 Here are the details:
            +match(subinfo.photo, photos[0].midUrl, individual.displayName)
        p
            span You can view this individual by clicking the button below. There you'll be able to share your finding with friends and family! If you feel that the match was false, submit a report using the link at the bottom of this message.
    +centeredbutton('VIEW INDIVIDUAL', cust.info.website + '/individual/' +  individual.id)
        p.center(style="text-align: center !important; color: #222222 !important")
            a(href=cust.info.website + '/reportFalse/' + individual.id', class="alert") REPORT A FALSE MATCH
```

### Inlining
The css is applied to individual elements before the email is sent, in order to have full client support.

## notificationmailer

This folder contains Wildbook email templates. They are used by the
*org.ecocean.NotificationMailer* class, which fills them with data appropriate
to the function being performed.

Emails can be sent either as plain text only, or as MIME-multipart plain/HTML.
If only a plain text template exists for a given template type, then only a
plain text email will be sent. If both ```.txt``` and ```.html``` template files
exist, then the default is to send a MIME-multipart email, although this can be
prevented in the *NotificationMailer* instance used. At least the plain text
template must exist. Subject lines are specified in the first line of the plain
text template, with the prefix **SUBJECT:** to highlight the fact.

The base templates for all emails are:

* ```email-template.txt```
* ```email-template.html```

The *NotificationMailer* class also allows to specify a template **type**, which
might be, for example, "newSubmission". This would load these templates:

* ```newSubmission.txt```
* ```newSubmission.html```

then use the text in each of those to replace the **@EMAIL_CONTENT@** tag in the
respective base templates. Once these final text/HTML templates have been
collated (during initialization of *NotificationMailer*) the tag-map specified
at creation time is used to perform tag search/replacement in the templates.

When creating an instance of the *NotificationMailer* class to send an email, it
requires a tag-map comprising tags to be replaced with text values. Tags are
generally specified in the templates with delimiting @ characters to isolate
them from other text.

The *NotificationMailer* also supports being created without a tag-map, but just
a single text string instead. In this case it is used to replace a standard
**@TEXT_CONTENT@** tag in the templates.