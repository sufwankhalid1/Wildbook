# Email Templates with Jade
- The email templates use the [Ink grid system for emails](http://zurb.com/ink/docs.php "read the documentation"). It is based on tables layouts.
- All email templates extend layout.jade
- There are hooks for a message title (html view), content (the main message and action button) and boxes (additional content pieces to show before the footer)
- The hook for boxes should consist of include statements to include other box partials
- Make sure to pass all the data needed for each box