package org.ecocean.email;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.ecocean.util.Jade4JUtils;
import org.ecocean.util.LogBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samsix.util.string.AttrString;
import com.samsix.util.string.StringUtilities;

import de.neuland.jade4j.exceptions.JadeCompilerException;
import de.neuland.jade4j.exceptions.JadeException;

public class Emailer {
    private final static String ATTR_STYLE = "style";
    private static Logger logger = LoggerFactory.getLogger(Emailer.class);
    private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    private final String host;
    private String username;
    private String password;
    private String defaultFromEmail;
    private final Map<String, Object> defaultModel = new HashMap<>();

    //
    // Used for debug purposes if you don't want a development
    // server sending emails except to the development/QA teams for debugging.
    //
    private Set<String> userIdPassFilter;
    public static final String TAG_SUBMISSION = "submission";
    public static final String TAG_INDIVIDUAL = "individual";
    public static final String TAG_TOKEN = "token";
    public static final String TAG_USER = "user";

    public Emailer(final String host) {
        this.host = host;
    }

    public Emailer(final String host,
                   final String username,
                   final String password,
                   final String defaultFromEmail) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.defaultFromEmail = defaultFromEmail;
    }

    public void addToDefaultModel(final String key, final Object value) {
        defaultModel.put(key, value);
    }

    public String getHost() {
        return host;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

//    public Set<String> getUserIdPassFilter() {
//        return userIdPassFilter;
//    }

    public void setUserIdPassFilter(final Set<String> userIdPassFilter) {
        this.userIdPassFilter = userIdPassFilter;
    }

    public void setUserIdPassFilter(final List<String> userIdPassFilter) {
        if (userIdPassFilter != null) {
            this.userIdPassFilter = new HashSet<String>(userIdPassFilter);
        } else {
            this.userIdPassFilter = null;
        }
    }

    public boolean passesFilter(final String userid) {
        if (userIdPassFilter == null) {
            return true;
        }

        return userIdPassFilter.contains(userid);
    }

    private String concatenateProperties(String oldProp, final String newProp) {
        oldProp = oldProp.trim();
        if (!oldProp.endsWith(";")) {
            oldProp += ";";
        }

        return oldProp + newProp.replaceAll("\\s{2,}", " ");
    }

    //
    // Code to inline the css so that it can be used in webmail
    //
    private String inlineCss(final String html) {
        Document doc = Jsoup.parse(html);

        //
        // to get all the style elements
        //
        doc.select(ATTR_STYLE).forEach(element -> {
            String styleRules = element.getAllElements().get(0).data().replaceAll("\n", "").trim();
            String delims = "{}";
            StringTokenizer st = new StringTokenizer(styleRules, delims);
            while (st.countTokens() > 1) {
                String selector = st.nextToken(), properties = st.nextToken();

                if (logger.isDebugEnabled()) {
                    logger.debug(LogBuilder.quickLog("selector", selector));
                }

                if (! selector.contains(":")) { // skip a:hover rules, etc.
                    doc.select(selector).forEach(selElem -> {
                        String oldProperties = selElem.attr(ATTR_STYLE);
                        selElem.attr(ATTR_STYLE,
                                     oldProperties.length() > 0
                                         ? concatenateProperties(oldProperties, properties)
                                         : properties);
                    });
                }
            }
            element.remove();
        });

        return doc.toString();
    }

    private String getPrefix(final String template) {
        return "emails/" + template;
    }

    private Map<String, Object> buildFullModel(final Map<String, Object> model) {
        if (model == null) {
            return new HashMap<>(model);
        }

        model.putAll(defaultModel);
        return model;
    }

    private String renderJadeEmailBody(final String template,
                                       final Map<String, Object> model,
                                       final boolean inlineStyles)
            throws JadeCompilerException, JadeException, IOException
    {
        String body = Jade4JUtils.renderCP(getPrefix(template) + "/body.jade", model);

        if (inlineStyles) {
            return inlineCss(body);
        }

        return body;
    }

    public String getJadeEmailBody(final String template,
                                   final Map<String, Object> model,
                                   final boolean inlineStyles)
        throws JadeCompilerException, JadeException, IOException
    {
        return renderJadeEmailBody(template, buildFullModel(model), inlineStyles);
    }

    public void sendJadeTemplate(final String recipients,
                                 final String template,
                                 final Map<String, Object> model)
        throws JadeCompilerException, JadeException, IOException, AddressException, MessagingException {
        sendJadeTemplate(defaultFromEmail, recipients, template, model);
    }

    public void sendJadeTemplate(final String sender,
                                 final String recipients,
                                 final String template,
                                 final Map<String, Object> model)
        throws JadeCompilerException, JadeException, IOException, AddressException, MessagingException {

        //
        // TODO: Add in internationalization by reading into the model a list of internationalizations.
        // Tack them into the map as a map called "trans" or something like that.
        // Should have a common set and a set specific to this template. The common set so that we
        // don't have to repeat ourselves for translations across emails, and a specific set for those
        // translations that are clearly a one-use case (just for this email template only).
        // Should we make our own cache for those then too so that these are cached along with the templates?
        // Probably.
        //

        Map<String, Object> fullModel = buildFullModel(model);

        String subject = StringUtilities.readResourceToString(getPrefix(template) + "/subject.txt");
        if (subject == null) {
            subject = template;
        } else {
            AttrString attrString = new AttrString();
            attrString.add(fullModel);
            subject = attrString.computeString(subject);
        }

        send(sender, recipients, subject, renderJadeEmailBody(template, fullModel, true));
    }


    /**
     *
     * @param sendTo comma-delimited string of email addresses to send to
     * @param subject of email
     * @param htmlMessage html content of body of email
     * @throws AddressException if email addresses are bad
     * @throws MessagingException if messaging problem
     */
    public void send(final String sendTo,
                     final String subject,
                     final String htmlMessage) throws AddressException, MessagingException {
        send(defaultFromEmail, sendTo, subject, htmlMessage);
    }

    /**
     *
     * @param from email address
     * @param sendTo comma-delimited string of email addresses to send to
     * @param subject of email
     * @param htmlMessage html content of body of email
     * @throws AddressException if email addresses are bad
     * @throws MessagingException if messaging problem
     */
    public void send(final String from,
                     final String sendTo,
                     final String subject,
                     final String htmlMessage) throws AddressException, MessagingException {
        if (StringUtils.isBlank(from)) {
            throw new MessagingException("From address is blank, cannot send");
        }
        if (StringUtils.isBlank(sendTo)) {
            throw new MessagingException("Send To address is blank, cannot send");
        }

        Properties props = new Properties();
        String protocol;
        int port;
        if (this.username != null) {
            protocol = "smtps";
            port = 465;
            props.setProperty("mail.smtp.auth", "true");
        } else {
            protocol = "smtp";
            port = 25;
        }
        props.setProperty("mail.transport.protocol", protocol);
        props.setProperty("mail." + protocol + ".host", host);
        props.setProperty("mail." + protocol + ".port", Integer.toString(port));

        Session session;
        if (username == null) {
            session = Session.getDefaultInstance(props);
        } else {
            session = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });
        }

        session.setDebug(logger.isDebugEnabled());
        final Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));

        List<String> recipients = StringUtilities.split(sendTo, ",", true);

        if (recipients.size() == 0) {
            logger.warn("No recipients for email with subject [" + subject + "], aborting.");
            return;
        }

        if (recipients.size() == 1) {
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients.get(0), false));
        } else {
            List<Address> sendTos = new ArrayList<>(recipients.size());
            for (String recipient : recipients) {
                sendTos.add(new InternetAddress(recipient, false));
            }
            msg.setRecipients(Message.RecipientType.TO, sendTos.toArray(new Address[sendTos.size()]));
        }

//        if (cc != null) {
//            msg.setRecipients(Message.RecipientType.CC, cc);
//        }
//        if (bcc != null) {
//            msg.setRecipients(Message.RecipientType.BCC, bcc);
//        }

        msg.setSubject(subject);

        msg.setContent(htmlMessage, "text/html; charset=utf-8");
        msg.setSentDate(new Date());

        executorService.execute(() -> {try {
            Transport.send(msg);
        } catch(MessagingException ex) {
            logger.error("Trouble sending email.", ex);
        }});
    }
}
