package org.ecocean.email.old;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>Convenience template mechanism for sending emails using JavaMail.
 * It provides support for both plain text and HTML emails, although HTML
 * emails must only reference remotely hosted images, not use embedded ones.</p>
 *
 * <p>A plain text file is mandatory, but the HTML version is optional.
 * If both are specified, the plain text should be specified first.
 * For HTML emails a MIME-multipart message will be sent using
 * the content of both files. If just the text version is present then a
 * simple plain text email will be sent.</p>
 *
 * @author Giles Winstanley
 * @see <a href="http://www.oracle.com/technetwork/java/javamail/">Oracle JavaMail page</a>
 * @see <a href="https://javamail.java.net/">JavaMail API project on Java.net</a>
 */
public final class EmailTemplate {
  /** SLF4J logger instance for writing log entries. */
  private static final Logger log = LoggerFactory.getLogger(EmailTemplate.class);
  /** Default character set encoding for email body texts. */
  private static final Charset DEFAULT_CHARSET = Charset.forName("ISO-8859-1");
  /** Template for message subject. */
  private final TemplateFiller subj;
  /** Template for plain text message body. */
  private TemplateFiller plainBody;
  /** Template for HTML message body. */
  private TemplateFiller htmlBody;
  /** Character set encoding for plain text. */
  private Charset charsetPlain;
  /** Character set encoding for HTML text. */
  private Charset charsetHTML;
  /** Determines whether to use SMTP over SSL. */
  private boolean useSSL = false;
  private String host;
  private int port = 25;
  /** {@code Authenticator} instance to use if SMTP Authentication is required. */
  private SMTPAuthenticator auth;

  /**
   * Creates a new email template, using the subject and files provided
   * The files are used to create body text representations for both the
   * plain text and HTML style emails.
   *
   * @param subj subject header for the email
   * @param plain file containing body text for plain text email
   * @param html file containing body text for HTML email
   * @param csP {@code Charset} for body text (plain text version)
   * @param csH {@code Charset} for body text (HTML text version)
   * @param host host name of SMTP mail server
   * @param port port number of SMTP mail server
   * @param useSSL whether to use SMTP over SSL
   * @throws IOException if any of the input files cannot be loaded
   */
  public EmailTemplate(final String subj, final Path plain, final Path html, final Charset csP, final Charset csH, final String host, final int port, final boolean useSSL) throws IOException {
    this.subj = new TemplateFiller(subj);
    this.plainBody = new TemplateFiller(plain);
    if (html != null)
      this.htmlBody = new TemplateFiller(html);
    this.charsetPlain = csP;
    this.charsetHTML = csH;

    setHost(host, useSSL);

    if (port < 0)
      throw new IllegalArgumentException("Invalid port number specified");
    this.port = port;
  }

  /**
   * Creates a new email template, using the subject and files provided
   * The files are used to create body text representations for both the
   * plain text and HTML style emails.
   *
   * @param subj subject header for the email
   * @param plain file containing body text for plain text email
   * @param html file containing body text for HTML email
   * @throws IOException if any of the input files cannot be loaded
   */
  public EmailTemplate(final String subj, final Path plain, final Path html) throws IOException {
    this.subj = new TemplateFiller(subj);
    this.plainBody = new TemplateFiller(plain);
    if (html != null)
      this.htmlBody = new TemplateFiller(html);
  }


  /**
   * Creates a new email template, using the subject and body provided,
   * which will send a plain text email.
   *
   * @param subject email subject
   * @param body email body text
   * @param cs {@code Charset} for body text
   */
  public EmailTemplate(final String subject, final String body, final Charset cs) {
    this.subj = new TemplateFiller(subject);
    this.plainBody = new TemplateFiller(body);
    charsetPlain = cs;
  }

  /**
   * Performs a string search/replace on the subject and body of the template.
   *
   * @param search term to find in both subject and body
   * @param replace term to substitute when a match is found
   * @param all whether to perform search throughout, or simply the first match
   */
  public void replace(final String search, final String replace, final boolean all) {
    subj.replace(search, replace, all);
    plainBody.replace(search, replace, all);
    if (htmlBody != null) {
      htmlBody.replace(search, replace, all);
    }
  }

  /**
   * Performs a string search/replace on the subject and body of the template.
   * This method is a convenience to perform all replacements throughout.
   *
   * @param search term to find in both subject and body
   * @param replace term to substitute when a match is found
   */
  public void replace(final String search, final String replace) {
    replace(search, replace, true);
  }

  /**
   * Substitutes a specified string with the contents of a text file.
   *
   * @param search the string for which to search
   * @param replace the text file to use as replacement text
   * @throws IOException if the replacement text file cannot be loaded
   */
  public void replace(final String search, final Path replace) throws IOException {
    replace(search, TemplateFiller.loadTextFromFile(replace));
  }

  /**
   * Performs a string search/replace on the subject and body of the template.
   *
   * @param search regex search term
   * @param replace regex replacement term
   * @param flags regex flags (defined in {@link Pattern})
   * @param all whether to perform search throughout, or simply the first match
   */
  public void replaceRegex(final String search, final String replace, final int flags, final boolean all) {
    subj.replaceRegex(search, replace, all);
    plainBody.replaceRegex(search, replace, all);
    if (htmlBody != null)
      htmlBody.replaceRegex(search, replace, all);
  }

  /**
   * Searches and replaces one or all occurrences of the specified regular
   * expression search term with the specified replacement.
   *
   * @param search regex search term
   * @param replace regex replacement term
   * @param all whether to replace all occurrences or just the first
   */
  public void replaceRegex(final String search, final String replace, final boolean all) {
    replaceRegex(search, replace, 0, all);
  }

  /**
   * Searches and replaces one or all occurrences of the specified regular
   * expression search term with the specified replacement.
   * This method is a convenience to perform all replacements throughout.
   *
   * @param search regex search term
   * @param replace regex replacement term
   */
  public void replaceRegex(final String search, final String replace) {
    replaceRegex(search, replace, 0, true);
  }

  /**
   * Performs a string search/replace on the message subject template.
   *
   * @param search term to find
   * @param replace term to substitute
   * @param all whether to perform search throughout, or simply the first match
   */
  public void replaceInSubject(final String search, final String replace, final boolean all) {
    subj.replace(search, replace, all);
  }

  /**
   * Performs a regex search/replace on the message subject template.
   *
   * @param search regex search term
   * @param replace regex replacement term
   * @param all whether to perform search throughout, or simply the first match
   */
  public void replaceRegexInSubject(final String search, final String replace, final boolean all) {
    subj.replaceRegex(search, replace, all);
  }

  /**
   * Performs a string search/replace on the plain text message body template.
   *
   * @param search term to find
   * @param replace term to substitute
   * @param all whether to perform search throughout, or simply the first match
   */
  public void replaceInPlainText(final String search, final String replace, final boolean all) {
    plainBody.replace(search, replace, all);
  }

  /**
   * Performs a regex search/replace on the plain text message body template.
   *
   * @param search regex search term
   * @param replace regex replacement term
   * @param all whether to perform search throughout, or simply the first match
   */
  public void replaceRegexInPlainText(final String search, final String replace, final boolean all) {
    plainBody.replaceRegex(search, replace, all);
  }

  /**
   * Performs a string search/replace on the HTML message body template.
   *
   * @param search term to find in both subject and body
   * @param replace term to substitute when a match is found
   * @param all whether to perform search throughout, or simply the first match
   */
  public void replaceInHtmlText(final String search, final String replace, final boolean all) {
    if (htmlBody != null)
      htmlBody.replace(search, replace, all);
    else
      throw new IllegalStateException("No HTML message body exists");
  }

  /**
   * Performs a regex search/replace on the HTML message body template.
   *
   * @param search regex search term
   * @param replace regex replacement term
   * @param all whether to perform search throughout, or simply the first match
   */
  public void replaceRegexInHtmlText(final String search, final String replace, final boolean all) {
    if (htmlBody != null)
      htmlBody.replaceRegex(search, replace, all);
    else
      throw new IllegalStateException("No HTML message body exists");
  }

  /**
   * Resets the template content to the base-state.
   * This allows the template to be re-used multiple times.
   */
  public void reset() {
    subj.reset();
    plainBody.reset();
    if (htmlBody != null)
      htmlBody.reset();
  }

  /**
   * Sets the base-state of the email template to the current content.
   * The base state is the state to which the template reverts when the
   * {@link #reset()} method is called.
   */
  public void setBaseState() {
    subj.setBaseState();
    plainBody.setBaseState();
    if (htmlBody != null)
      htmlBody.setBaseState();
  }

    /**
     * Converts a list of email addresses from strings to an {@code Address} array.
     * @param x list of email addresses
     * @return array of {@code Address} objects
     * @throws AddressException if thrown when creating {@code Address} instances
      */
    private static Address[] convertAddresses(final Collection<String> x) throws AddressException {
        List<Address> list = new ArrayList<>(x.size());
        for (String s : x) {
            try {
                if (s != null && !"".equals(s.trim()))
                    list.add(new InternetAddress(s.trim()));
                else
                    log.warn("Invalid email address; ignoring: " + s);
            } catch (AddressException ex) {
                log.warn("Failed to convert email address: " + s);
            }
        }
        return list.toArray(new Address[list.size()]);
    }

  /**
   * Sends this email to the recipients specified.
   * If an HTML format has been included in the template, then a call
   * with the {@code html} parameter as {@code true} will attempt
   * to send the email as a multipart/alternative MIME type.
   *
   * @param from email sender
   * @param to email recipients
   * @param cc email recipients (CC)
   * @param bcc email recipients (BCC)
   * @param html whether to try to send in HTML format (will default to plain text if not possible)
   * @return number of successful messages sent
   */
    public boolean sendSingle(final String from,
                              final Collection<String> to,
                              final Collection<String> cc,
                              final Collection<String> bcc) {
        //
        // Just preserving previous behavior of returning boolean
        //
        try {
            String html;
            if (htmlBody != null) {
                html = htmlBody.getText();
            } else {
                html = null;
            }
            sendSingle(from, to, cc, bcc, useSSL, host, port, this.auth, false,
                       subj.getText(), plainBody.getText(), getPlainTextCharset(),
                       html, getHtmlTextCharset());
            return true;
        } catch (Exception ex) {
            log.error("Can't send email.", ex);
            return false;
        }
    }


    public static void sendSingle(final String from,
                                  final Collection<String> to,
                                  final Collection<String> cc,
                                  final Collection<String> bcc,
                                  final boolean useSSL,
                                  final String host,
                                  final int port,
                                  final SMTPAuthenticator auth,
                                  final boolean useStartTLS,
                                  final String subj,
                                  final String plainBody,
                                  final Charset plainCharset,
                                  final String htmlBody,
                                  final Charset htmlCharset) throws AddressException, MessagingException {
        if (StringUtils.isBlank(from)) {
          throw new IllegalArgumentException();
        }
        if (CollectionUtils.isEmpty(to)) {
          throw new IllegalArgumentException();
        }

        ByteArrayOutputStream debugOS = null;
        PrintStream debugPS = null;
        Transport transport = null;
        try {
            // Setup properties ready for obtaining session.
            String protocol = useSSL ? "smtps" : "smtp";
            Properties props = new Properties();
            props.setProperty("mail.from", from);

            if (useSSL) {
                props.setProperty("mail.transport.protocol", "smtps");
                props.setProperty("mail.smtps.host", host);
                props.setProperty("mail.smtps.port", Integer.toString(port));
            } else {
                props.setProperty("mail.transport.protocol", "smtp");
                props.setProperty("mail.smtp.host", host);
                props.setProperty("mail.smtp.port", Integer.toString(port));
            }
            if (auth != null) {
                props.setProperty("mail.smtp.auth", "true");
            }

            if (useStartTLS) {
                props.setProperty("mail." + protocol + ".starttls.enable", Boolean.toString(useStartTLS));
            }

            Session session = Session.getDefaultInstance(props, auth);

            if (log.isDebugEnabled()) {
                session.setDebug(true);
                debugOS = new ByteArrayOutputStream();
                debugPS = new PrintStream(debugOS);
                session.setDebugOut(debugPS);
            }

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));

            message.setRecipients(Message.RecipientType.TO, convertAddresses(to));
            if (! CollectionUtils.isEmpty(cc)) {
                message.setRecipients(Message.RecipientType.CC, convertAddresses(cc));
            }
            if (! CollectionUtils.isEmpty(bcc)) {
                message.setRecipients(Message.RecipientType.BCC, convertAddresses(bcc));
            }

            message.setSubject(subj, plainCharset.name());
            if (! StringUtils.isBlank(htmlBody)) {
                // Create multipart.
                Multipart mp = new MimeMultipart("alternative");

                // Add plain text content.
                BodyPart part = new MimeBodyPart();
                part.setContent(plainBody,
                                String.format("text/plain; charset=\"%s\"",
                                plainCharset.name()));
                mp.addBodyPart(part);

                // Add HTML content.
                part = new MimeBodyPart();
                part.setContent(htmlBody,
                                String.format("text/html; charset=\"%s\"",
                                htmlCharset.name()));
                mp.addBodyPart(part);

                message.setContent(mp);
            } else {
                message.setText(plainBody, plainCharset.name());
            }
            message.saveChanges();

            // Obtain/configure session for sending messages.
            session.setDebug(log.isDebugEnabled());
            if (useSSL) {
                session.setProtocolForAddress("rfc822", "smtps");
            }

            // Obtain/connect message transport.
            transport = session.getTransport(protocol);
            if (auth != null) {
                transport.connect(auth.getPA().getUserName(), auth.getPA().getPassword());
            } else {
                transport.connect();
            }
            transport.sendMessage(message, message.getAllRecipients());

            if (debugOS != null) {
                log.debug(debugOS.toString());
            }
        } finally {
            try {
                if (transport != null) {
                    transport.close();
                }
            }
            catch (MessagingException me) {
                log.debug(me.getMessage(), me);
            }
            if (debugPS != null) {
                debugPS.close();
            }
        }
    }

  /**
   * Resolves mail template files using the specified filename base.
   * Template files are assumed to have the extensions &quot;.txt&quot;
   * and &quot;.html&quot; (or &quot;.htm&quot;) for plain text
   * and HTML messages respectively.
   * This method doesn't attempt to load the files, just resolves them to
   * {@code File} instances based on name and existence.
   *
   * @param path folder path to use to locate files
   * @param baseName base name of email template files to load
   * @return pair (as 2-element array) of files (plain text, HTML text)
   * @throws IOException if a problem occurs in locating the template files
   */
  public static Path[] resolveTemplatesFromRoot(final Path path, final String baseName) throws IOException {
    Objects.requireNonNull(path);
    Objects.requireNonNull(baseName);
    Path fP = getTemplateFile(path, baseName, new String[]{".txt",".TXT",""});
    Path fH = getTemplateFile(path, baseName, new String[]{".html",".HTML",".htm",".HTM"});
    return new Path[]{fP, fH};
  }

  /**
   * Resolves mail template files using the specified filename base.
   * Template files are assumed to have the extensions &quot;.txt&quot;
   * and &quot;.html&quot; (or &quot;.htm&quot;) for plain text
   * and HTML messages respectively.
   * This method doesn't attempt to load the files, just resolves them to
   * {@code File} instances based on name and existence.
   *
   * @param path folder path to use to locate files
   * @param baseName base name of email template files to load
   * @return pair of files (plain text, HTML text)
   * @throws IOException if a problem occurs in locating the template files
   */
  public static Path[] resolveTemplatesFromRoot(final String path, final String baseName) throws IOException {
    return resolveTemplatesFromRoot(Paths.get(path), baseName);
  }

  /**
   * Loads a mail template using the specified pair of files, specified as
   * plain text and HTML text in order.
   *
   * @param fP plain text email template file
   * @param fH HTML text email template file
   * @param csP {@code Charset} for plain text file
   * @param csH {@code Charset} for HTML text file
   * @return EmailTemplate instance ready for use
   * @throws IOException if a problem occurs in loading the template
   */
  public static EmailTemplate load(final Path fP, final Path fH, final Charset csP, final Charset csH) throws IOException {
    Objects.requireNonNull(fP);
    if (!Files.exists(fP))
      throw new IllegalArgumentException("Invalid file specified: " + fP);

    // Process plain text file for subject line.
    String pt = join("\n", Files.readAllLines(fP, csP));
    String[] subjAndBody = extractSubjectLine(pt);
    EmailTemplate x = new EmailTemplate(subjAndBody[0], subjAndBody[1], csP);

    if (fH != null) {
      if (!Files.exists(fH))
        throw new IllegalArgumentException("Invalid file specified: " + fH);
      List<String> ht = Files.readAllLines(fH, csH);

      // Perform quick check for matching HTML page charset definition.
      Pattern pat = Pattern.compile("[<\\s]meta\\s.*\\scharset\\s*=\\s*\"?([^\"]+)\"?", Pattern.CASE_INSENSITIVE);
      for (String s : ht) {
        if (s.toLowerCase(Locale.US).contains("charset")) {
          Matcher m = pat.matcher(s);
          if (m.find()) {
            String cs = m.group(1).trim();
            if (!cs.equalsIgnoreCase(csH.name())) {
              log.warn(String.format("Found HTML charset mismatch; %s (page specifies: %s): %s", csH.name(), cs, fH));
            }
          }
        }
        break;
      }
            x.setHtmlText(new TemplateFiller(join("\n", ht)), csH);
        }

        return x;
  }

  /**
   * Attempts to extract an email subject line from the specified text, which
   * should be prefixed with &quot;SUBJECT:&quot;. If found, the subject line
   * of the email is assigned, and the text returned has the subject line
   * removed.
   * @param text text to examine for subject line
   * @return two-element array of subject line and remaining text (subject may be null of not found)
   */
  static String[] extractSubjectLine(final String text) {
    log.trace(text);
    final Pattern p = Pattern.compile("^SUBJECT:(.*)$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    Matcher m = p.matcher(text);
    if (m.find())
      return new String[]{ m.group(1), text.substring(1 + m.end(1)) };
    return new String[]{ null, text };
  }

  static String join(final CharSequence sep, final Iterable<? extends CharSequence> elem) {
    StringBuilder sb = new StringBuilder();
    Iterator<? extends CharSequence> it = elem.iterator();
    if (it.hasNext()) {
      sb.append(it.next());
      while (it.hasNext())
        sb.append(sep).append(it.next());
    }
    return sb.toString();
  }

  /**
   * Loads a mail template using the specified name as a base for the files.
   * Template files are assumed to have the extensions &quot;.txt&quot;
   * and &quot;.html&quot; (or &quot;.htm&quot;) for plain text
   * and HTML messages respectively. A plain text message must be present
   * to successfully create an {@code EmailTemplate} instance, and an HTML
   * message file can be optionally present.
     *
   * @param fP plain text email template file
   * @param fH HTML text email template file
     * @param cs {@code Charset} for text files (must be the same)
   * @return EmailTemplate instance ready for use
   * @throws IOException if a problem occurs in loading the template
   */
  public static EmailTemplate load(final Path fP, final Path fH, final Charset cs) throws IOException {
    return load(fP, fH, cs, cs);
  }

  /**
   * Loads a mail template using the specified name as a base for the files.
   * Template files are assumed to have the extensions &quot;.txt&quot;
   * and &quot;.html&quot; (or &quot;.htm&quot;) for plain text
   * and HTML messages respectively. A plain text message must be present
   * to successfully create an {@code EmailTemplate} instance, and an HTML
   * message file can be optionally present.
     *
   * @param path folder path to use to locate files
   * @param baseName base name of email template files to load
   * @param csP {@code Charset} for plain text file
   * @param csH {@code Charset} for HTML text file
   * @return EmailTemplate instance ready for use
   * @throws IOException if a problem occurs in loading the template
   */
  public static EmailTemplate load(final Path path, final String baseName, final Charset csP, final Charset csH) throws IOException {
    Path[] f = resolveTemplatesFromRoot(path, baseName);
    return load(f[0], f[1], csP, csH);
  }

  /**
   * Loads a mail template using the specified name as a base for the files.
   * Template files are assumed to have the extensions &quot;.txt&quot;
   * and &quot;.html&quot; (or &quot;.htm&quot;) for plain text
   * and HTML messages respectively. A plain text message must be present
   * to successfully create an {@code EmailTemplate} instance, and an HTML
   * message file can be optionally present.
     *
   * @param path folder path to use to locate files
   * @param baseName base name of email template files to load
     * @param cs {@code Charset} for text files (must be the same)
   * @return EmailTemplate instance ready for use
   * @throws IOException if a problem occurs in loading the template
   */
  public static EmailTemplate load(final Path path, final String baseName, final Charset cs) throws IOException {
    Path[] f = resolveTemplatesFromRoot(path, baseName);
    return load(f[0], f[1], cs, cs);
  }

  /**
   * Loads a mail template using the specified name as a base for the files.
     *
   * @param path folder path to use to locate files
   * @param baseName base name of email template files to load
     * @param cs {@code Charset} for text files (must be the same)
   * @return EmailTemplate instance ready for use
   * @throws IOException if a problem occurs in loading the template
   */
  public static EmailTemplate load(final String path, final String baseName, final Charset cs) throws IOException {
    return load(Paths.get(path), baseName, cs, cs);
  }

  /**
   * Finds the a file with an option of suffixes.
     *
   * @param path file path for template file
   * @param name root name of the template
   * @param suffix file suffixes to use to find template
   * @return File instance representing existing template file, or null
   */
  private static Path getTemplateFile(final Path path, final String name, final String[] suffix) {
    for (String s : suffix) {
      Path f = Paths.get(path.toString(), name + s);
      if (Files.exists(f))
        return f;
    }
    return null;
  }

  public void setSubject(final String x) {
        subj.setText(x);
        subj.setBaseState();
    }

    public String getSubject() {
        return subj.getText();
    }

  public void setPlainText(final TemplateFiller x, final Charset cs) {
        plainBody = x;
        charsetPlain = (cs == null) ? DEFAULT_CHARSET : cs;
    }

    public void setPlainText(final String x, final Charset cs) {
        plainBody.setText(x);
        plainBody.setBaseState();
        charsetPlain = (cs == null) ? DEFAULT_CHARSET : cs;
    }

    public String getPlainText() {
        return plainBody.getText();
    }

    public Charset getPlainTextCharset() {
        return (charsetPlain == null) ? DEFAULT_CHARSET : charsetPlain;
    }

  public void setHtmlText(final TemplateFiller x, final Charset cs) {
        htmlBody = x;
        charsetHTML = (cs == null) ? DEFAULT_CHARSET : cs;
    }

    public void setHtmlText(final String x, final Charset cs) {
        if (htmlBody == null)
            htmlBody = new TemplateFiller(x);
        else {
            htmlBody.setText(x);
            htmlBody.setBaseState();
        }
        charsetHTML = (cs == null) ? DEFAULT_CHARSET : cs;
    }

    public String getHtmlText() {
        return htmlBody.getText();
    }

    public Charset getHtmlTextCharset() {
        return (charsetHTML == null) ? DEFAULT_CHARSET : charsetHTML;
    }

    public boolean hasHtmlText() {
        return htmlBody != null;
    }

  public void removeHtmlText() {
    this.htmlBody = null;
  }

  /**
   * Tells the mail transport whether to use SMTP Authentication.
   *
   * @param useAuth whether to use SMTP Authentication.
   * @param username username for authentication (can be {@code null} if {@code useAuth=false})
   * @param password password for authentication (can be {@code null} if {@code useAuth=false})
   *
   * @see <a href="http://www.ietf.org/rfc/rfc2554.txt">RFC2554</a>
   */
  public void setUseAuth(final boolean useAuth, final String username, final String password) {
    if (useAuth) {
      auth = new SMTPAuthenticator(username, password);
    }
    else {
      auth = null;
    }
  }

  /**
   * Sets the host name of the mail server, and whether to use SMTP over SSL.
   *
   * @param host email server host
   * @param useSSL whether to use SSL
   */
    public final void setHost(final String host, final boolean useSSL) {
        if (StringUtils.isBlank(host)) {
            throw new IllegalArgumentException("Invalid host specified");
        }

        // Check for port number suffix on host name.
        port = useSSL ? 465 : 25;  // Default mail server port.
        Matcher matcher = Pattern.compile("^(.+):(\\d+)$").matcher(host);
        if (matcher.matches()) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Setting host: %s:%d", host, port));
                }

                this.host = matcher.group(1);
                port = Integer.parseInt(matcher.group(2));
            }
            catch (NumberFormatException nfx) {
                throw new IllegalArgumentException("Invalid host/port specified", nfx);
            }
        } else {
            this.host = host;
        }

        this.useSSL = useSSL;
    }
}
