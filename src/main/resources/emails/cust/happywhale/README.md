# Email Templates with Jade
- All email templates extend layout.jade
- There are hooks for a message title (html view), content (the main message and action button) and boxes (additional content pieces to show before the footer)
- The hook for boxes should consist of include statements to include other box partials
- Make sure to pass all the data needed for each box