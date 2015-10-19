package org.ecocean.email;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samsix.util.string.StringUtilities;

public class Emailer {
    private static Logger logger = LoggerFactory.getLogger(Emailer.class);
    private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    private String host;
    private String username;
    private String password;

    public Emailer(final String host) {
        this.host = host;
    }


    public Emailer(final String host, final String username, final String password) {
        this.host = host;
        this.username = username;
        this.password = password;
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
