/*
 * The Shepherd Project - A Mark-Recapture Framework
 * Copyright (C) 2011 Jason Holmberg
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.ecocean.email.old;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.ecocean.Global;
import org.ecocean.email.EmailUtils;
import org.ecocean.email.Emailer;
import org.ecocean.util.FileUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Sends out an email notification.
 * This class in designed to run on an independent thread, so can be scheduled
 * for background operation.
 * <p>When an instance is created, a delegate {@code EmailTemplate} instance
 * is created which handles loading of relevant text/HTML content templates.
 * The template mechanism can be thought of as having three levels:</p>
 * <ol>
 * <li>Base template (text/HTML versions; used for all emails).</li>
 * <li>Type template (text/HTML versions; used for specific type emails).</li>
 * <li>Tag assignments (user-specified text replacements; apply to both text/HTML versions).</li>
 * </ol>
 *
 * <p>For example, if client code calls the following:</p>
 * <pre>
 * String context = "context0";
 * String langCode = "en";
 * String from = "from&#64;bar.org";
 * String to = "to&#64;wibble.org";
 * String type = "individualAddEncounter";
 * Map&lt;String, String&gt; tagMap = new HashMap&lt;&gt;();
 * NotificationMailer mailer = new NotificationMailer(context, langCode, from, to, type, tagMap);
 * </pre>
 * <p>then the following will occur:</p>
 * <ol>
 * <li>Base templates loaded (<code>email-template.txt</code>, <code>email-template.html</code>).</li>
 * <li>Type templates loaded (<code>individualAddEncounter.txt</code>, <code>individualAddEncounter.html</code>).</li>
 * <li>Type templates inserted into respective base templates (via <em>&#64;EMAIL_CONTENT&#64;</em> tag).</li>
 * <li>Email subject line extracted from first line of plain text template (if possible).</li>
 * <li>Replacements performed for tags specified in map.</li>
 * </ol>
 * <p>Initialization is now complete, and no email will be sent until the
 * {@link #run()} method is invoked, usually via a wrapping
 * {@link java.lang.Thread} or {@link java.util.concurrent.Executor}.</p>
 * <p>If the <em>type</em> parameter is null, then no type-template is loaded,
 * and the base tag (<em>&#64;EMAIL_CONTENT&#64;</em>) is simply replaced by the
 * standard content tag (<em>&#64;TEXT_CONTENT&#64;</em>) before proceeding as usual.</p>
 *
 * <p>If instead the other constructor is invoked:</p>
 * <pre>
 * String context = "context0";
 * String langCode = "en";
 * String from = "from&#64;bar.org";
 * String to = "to&#64;wibble.org";
 * String type = "individualAddEncounter";
 * String text = "Thank you for submitting a new individual to the database!";
 * NotificationMailer mailer = new NotificationMailer(context, langCode, from, to, type, text);
 * </pre>
 * <p>then the standard content tag (<em>&#64;TEXT_CONTENT&#64;</em>) will be replaced
 * with the specified text, and no other tag replacements will occur during
 * initialization.</p>
 * <p>If instead you want different text inserted for each of the plain/HTML
 * templates, you must do the replacements after initialization:</p>
 * <pre>
 * String context = "context0";
 * String langCode = "en";
 * String from = "from&#64;bar.org";
 * String to = "to&#64;wibble.org";
 * String type = "individualAddEncounter";
 * NotificationMailer mailer = new NotificationMailer(context, langCode, from, to, type);
 * String text = "Thank you for submitting a new individual to the database!";
 * mailer.replaceInPlainText("&#64;TEXT_CONTENT&#64;", text);
 * mailer.replaceInHtmlText("&#64;TEXT_CONTENT&#64;", "&lt;div id=\&quot;thanks&quot;\&gt;" + text + "&lt;/div&gt;");
 * </pre>
 *
 * <p>An optional REMOVEME section is also supported, which is designed to
 * support inclusion of a section for removing a user from an email
 * subscription. This optional section can be delimited by
 * <em>&#64;REMOVEME_START&#64;</em> &amp; <em>&#64;REMOVEME_END&#64;</em>
 * tags in the plain text template, or by commented versions
 * (<em>&lt;!--&#64;REMOVEME_START&#64;--&gt;</em> &amp;
 * <em>&lt;!--&#64;REMOVEME_END&#64;--&gt;</em>) in the HTML template.
 * If a user-specific email-hash tag is specified in the tag map, these
 * delimiters are removed, and the section remains in place, otherwise the
 * entire section is removed during initialization.</p>
 * <p>For example, this in the plain text version:</p>
 * <pre>
 * &#64;REMOVEME_START&#64;
 * To be unsubscribed from these emails, follow this link:
 *     &#64;REMOVE_LINK&#64;
 * &#64;REMOVEME_END&#64;
 * </pre>
 * <p>or the following in the HTML version:</p>
 * <pre>
 * &lt;!--&#64;REMOVEME_START&#64;--&gt;
 * &lt;p&gt;To be unsubscribed from these emails, &lt;a href="&#64;REMOVE_LINK&#64;"&gt;click here&lt;/a&gt;.&lt;/p&gt;
 * &lt;!--&#64;REMOVEME_END&#64;--&gt;
 * </pre>
 *
 * <p>After initialization, and before invoking {@link #run()}, the template
 * can still be used to make arbitrary tag replacements using the
 * {@link #replace(String, String)} or {@link #replaceRegex(String, String)}
 * methods.</p>
 *
 * @author Giles Winstanley
 */
public final class NotificationMailer implements Runnable {
  /** SLF4J logger instance for writing log entries. */
  private static final Logger log = LoggerFactory.getLogger(NotificationMailer.class);
  /** Charset for plain text email. */
  private static final Charset CHARSET_PLAIN = Charset.forName("UTF-8");
  /** Charset for HTML text email. */
  private static final Charset CHARSET_HTML = Charset.forName("UTF-8");
  /** Search path for email templates (relative to root resources). */
  private static final String SEARCH_PATH = "emails/notificationmailer";
  /** Base email template from which all emails are derived. */
  private static final String BASE_TEMPLATE_ROOT = "email-template";
  /** Tag in base template to replace with email-specific content. */
  private static final String BASE_CONTENT_TAG = "@EMAIL_CONTENT@";
  /** Generic tag in to replace with text content. */
  public static final String STANDARD_CONTENT_TAG = "@TEXT_CONTENT@";
  /** Tag to replace with email &quot;dontTrack&quot; link when specifying for REMOVEME section. */
  public static final String EMAIL_NOTRACK = "@EMAIL_NOTRACK@";
  /** Tag to replace with email hash when specifying for REMOVEME section. */
  public static final String EMAIL_HASH_TAG = "@EMAIL_HASH@";
  /** SMTP host. */
  private final String host;
  /** Email address of sender. */
  private final String sender;
  /** Email addresses of recipients. */
  private final Collection<String> recipients;
  /** Email template processor. */
  private EmailTemplate mailer;
  /** Flag indicating whether setup failed. */
  private boolean failedSetup;

  /**
   * Creates a new NotificationMailer instance.
   *
   * @param context webapp context
   * @param langCode language code for template loading (defaults to &quot;en&quot;)
   * @param to email recipients
   * @param types list of email types to try ((e.g. [<em>individualAddEncounter-auto</em>, <em>individualAddEncounter</em>])
   * @param map map of search/replace strings for email template (if order is important, supply {@code LinkedHashMap}
   */
  public NotificationMailer(final String context,
                            final String langCode,
                            final Collection<String> to,
                            final String type,
                            final Map<String, String> map) {
    Objects.requireNonNull(context);
    Objects.requireNonNull(to);

    for (String s : to) {
      if (s == null || "".equals(s.trim()))
        throw new IllegalArgumentException("Invalid email TO address specified");
    }
    this.sender = EmailUtils.getAdminSender();
    this.recipients = to;

    //
    // NOTE: Only grabbing the emailer here for now to get host and/or
    // auth info. Eventually this class should either use the emailer
    // to actually send the email or actually the class should probably just
    // go away in lieu of the other class. That is, this code is replaced by Emailer.
    //
    Emailer emailer = Global.INST.getEmailer();
    this.host = emailer.getHost();
    boolean useSSL = (emailer.getUsername() != null);

    try {
      mailer = findAndLoadEmailTemplate(langCode, type);
      mailer.setHost(host, useSSL);
      if (useSSL) {
          mailer.setUseAuth(true, emailer.getUsername(), emailer.getPassword());
      }

      // Can also set port/SSL/etc. here if needed.
      // Perform tag replacements.
      if (map != null) {
        for (Map.Entry<String, String> me : map.entrySet()) {
          try {
            mailer.replace(me.getKey(), me.getValue() == null ? "" : me.getValue());
          } catch (IllegalStateException ex) {
            // Additional safe-guard for when key's value is missing in some map implementations.
          }
        }
        // Remove REMOVEME section when not applicable (i.e. no hashed email info).
        if (map.containsKey("@URL_LOCATION@") && map.containsKey(EMAIL_HASH_TAG) && map.containsKey(EMAIL_NOTRACK)) {
          mailer.replaceInPlainText("@REMOVEME_START@", null, false);
          mailer.replaceInPlainText("@REMOVEME_END@", null, false);
          if (mailer.hasHtmlText()) {
            mailer.replaceInHtmlText("<!--@REMOVEME_START@-->", null, false);
            mailer.replaceInHtmlText("<!--@REMOVEME_END@-->", null, false);
          }
          // Extra layer to help prevent chance of URL spoof attacks.
          String noTrack = map.get(EMAIL_NOTRACK);
          if (noTrack.matches("([a-z]+)=(.+)")) {
            String link = String.format("http://%s/DontTrack?%s&email=%s", map.get("@URL_LOCATION@"), noTrack, map.get(EMAIL_HASH_TAG));
            mailer.replace("@REMOVEME_LINK@", link, true);
          }
        } else {
          mailer.replaceRegexInPlainText("(?s)@REMOVEME_START@.*@REMOVEME_END@", null, false);
          if (mailer.hasHtmlText())
            mailer.replaceRegexInHtmlText("(?s)<!--@REMOVEME_START@.*@REMOVEME_END@-->", null, false);
        }
      }
    } catch (IOException ex) {
      // Logged/flagged as error to avoid interrupting client code processing.
      log.error(ex.getMessage(), ex);
      failedSetup = true;
    }
  }


  /**
   * Creates a new NotificationMailer instance.
   *
   * @param context webapp context
   * @param langCode language code for template loading
   * @param to email recipient
   * @param type email type ((e.g. <em>individualAddEncounter</em>)
   * @param map map of search/replace strings for email template (if order is important, supply {@code LinkedHashMap}
   */
  public NotificationMailer(final String context, final String langCode, final String to, final String type, final Map<String, String> map) {
    this(context, langCode, Arrays.asList(to), type, map);
  }

  /**
   * Creates a new NotificationMailer instance.
   * If the <em>type</em> parameter is null, the specified <em>text</em> is
   * placed directly into the standard email template, instead of also loading
   * an type-template.
   * @param context webapp context
   * @param langCode language code for template loading
   * @param to email recipients
   * @param type email type ((e.g. <em>individualAddEncounter</em>)
   * @param text text with which to replace standard content tag
   */
  @SuppressWarnings("serial")
  public NotificationMailer(final String context, final String langCode, final Collection<String> to, final String type, final String text) {
    this(context, langCode, to, type, new HashMap<String, String>(){{ put(STANDARD_CONTENT_TAG, text); }});
  }

  /**
   * Creates a new NotificationMailer instance.
   * If the <em>type</em> parameter is null, the specified <em>text</em> is
   * placed directly into the standard email template, instead of also loading
   * an type-template.
   *
   * @param context webapp context
   * @param langCode language code for template loading
   * @param to email recipient
   * @param type email type ((e.g. <em>individualAddEncounter</em>)
   * @param text text with which to replace standard content tag
   */
  public NotificationMailer(final String context, final String langCode, final String to, final String type, final String text) {
    this(context, langCode, Arrays.asList(to), type, text);
  }

  private boolean existsEmailTemplate(final String langCode, final String type) {
    try {
      return resolveTemplatesFromRoot(langCode, type)[0] != null;
    } catch (IOException ex) {
      return false;
    }
  }

  private EmailTemplate findAndLoadEmailTemplate(final String langCode, final String type) throws IOException {
    if (langCode != null && !"".equals(langCode.trim())) {
        if (existsEmailTemplate(langCode, type)) {
            return loadEmailTemplate(langCode, type);
        }
    }

    // Default to "en" if none found yet.
    if (existsEmailTemplate("en", type)) {
        return loadEmailTemplate("en", type);
    }

    throw new FileNotFoundException("Failed to find valid email template in specified types");
  }

  /**
   * Loads an email template for the specified email type.
   * An email template references two files, one for each of plain/HTML text.
   *
   * @param langCode language code for template loading
   * @param type string specifying type of email (e.g. <em>individualAddEncounter</em>)
   * @return {@code EmailTemplate} instance
   */
  private EmailTemplate loadEmailTemplate(final String langCode, final String type) throws IOException {
    // Load generic email template for context.
    Path[] fBase = resolveTemplatesFromRoot(langCode, BASE_TEMPLATE_ROOT);
    if (fBase[0] == null || !Files.isRegularFile(fBase[0]))
      throw new FileNotFoundException(String.format("Failed to find core plain text email template: %s.txt", BASE_TEMPLATE_ROOT));
    if (fBase[1] == null || !Files.isRegularFile(fBase[1])) {
      log.trace(String.format("Failed to find core HTML text email template: %s.html", BASE_TEMPLATE_ROOT));
      fBase[1] = null;
    }

    EmailTemplate template = EmailTemplate.load(fBase[0], fBase[1], CHARSET_PLAIN, CHARSET_HTML);

    // Load content relating to specified email type.
    if (type != null) {
      Path[] fCont = resolveTemplatesFromRoot(langCode, type);
      if (fCont[0] == null || !Files.isRegularFile(fCont[0]))
        throw new FileNotFoundException(String.format("Failed to find plain text email template: %s.txt", type));
      if (fCont[1] == null || !Files.isRegularFile(fCont[1])) {
        log.trace(String.format("Failed to find HTML text email template: %s.html", type));
        fCont[1] = null;
        template.removeHtmlText();
      }
      // Place content in base template.
      String pt = TemplateFiller.loadTextFromFile(fCont[0]);
      String[] subjAndBody = EmailTemplate.extractSubjectLine(pt);
      if (subjAndBody[0] != null) {
        template.setSubject(subjAndBody[0]);
        template.setPlainText(subjAndBody[1], template.getPlainTextCharset());
      }
      String ht = (fCont[1] == null) ? null : TemplateFiller.loadTextFromFile(fCont[1]);
      template.replaceInPlainText(BASE_CONTENT_TAG, pt, false);
      if (template.hasHtmlText()) {
        template.replaceInHtmlText(BASE_CONTENT_TAG, ht, false);
      }
    } else {
      // Place content in base template.
      template.replaceInPlainText(BASE_CONTENT_TAG, STANDARD_CONTENT_TAG, false);
      if (template.hasHtmlText()) {
        template.replaceInHtmlText(BASE_CONTENT_TAG, STANDARD_CONTENT_TAG, false);
      }
    }

    // Return template.
    return template;
  }

  /**
   * Resolves mail template files using the specified filename base.
   * Template files are assumed to have the extensions &quot;.txt&quot;
   * and &quot;.html&quot; (or &quot;.htm&quot;) for plain text
   * and HTML messages respectively.
   * This method doesn't attempt to load the files, just resolves them to
   * {@code File} instances based on name and existence.
   *
   * @param langCode language code for template loading
   * @param baseName base name of email template files to load
   * @return pair (as 2-element array) of files (plain text, HTML text)
   * @throws IOException if a problem occurs in locating the template files
   */
  private Path[] resolveTemplatesFromRoot(final String langCode, final String baseName) throws IOException {
    Objects.requireNonNull(langCode);
    Objects.requireNonNull(baseName);

    String s = baseName + ".txt";
    Path path = FileUtilities.findResourceOnFileSystem(String.format("%s/%s/%s", SEARCH_PATH, langCode, s));
    if (path == null) {
      s = baseName + ".TXT";
      path = FileUtilities.findResourceOnFileSystem(String.format("%s/%s/%s", SEARCH_PATH, langCode, s));
    }
    if (path == null)
      throw new FileNotFoundException(String.format("Failed to find plain text email template: %s.txt", baseName));
    return EmailTemplate.resolveTemplatesFromRoot(path.getParent(), baseName);
  }

  public void appendToSubject(final String text) {
      if (text == null) {
          return;
      }

      String subj = mailer.getSubject();
      mailer.setSubject(subj == null ? text : subj + text);
  }

  @Override
  public void run() {
    if (failedSetup) {
      log.info("*** Not processing email as setup failed; see previous error log. ***");
      return;
    }

    mailer.sendSingle(sender, recipients, null, null);
  }
}
