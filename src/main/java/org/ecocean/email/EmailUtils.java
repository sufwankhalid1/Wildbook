package org.ecocean.email;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.ecocean.Global;
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


public class EmailUtils {
    private final static String ATTR_STYLE = "style";
    private final static Logger logger = LoggerFactory.getLogger(EmailUtils.class);

    private final static List<String> genericKeys =
            Arrays.asList(new String[]{"cust.displayname",
                                       "cust.info.website",
                                       "cust.info.logo",
                                       "cust.info.social.instagram",
                                       "cust.info.social.facebook",
                                       "cust.info.social.twitter"});


    private EmailUtils() {
        // prevent instantiation
    }

    public static String getAdminRecipients() {
        return Global.INST.getAppResources().getString("email.admin.recipients", null);
    }

    public static String getAdminSender() {
        return Global.INST.getAppResources().getString("email.admin.sender", null);
    }

    private static String getPrefix(final String template) {
        return "emails/" + template;
    }

    public static String getJadeEmailBody(final String template,
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

    //
    // Code to inline the css so that it can be used in webmail
    //
    private static String inlineCss(final String html) {
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

    private static String concatenateProperties(String oldProp, final String newProp) {
        oldProp = oldProp.trim();
        if (!oldProp.endsWith(";")) {
            oldProp += ";";
        }

        return oldProp + newProp.replaceAll("\\s{2,}", " ");
    }


    public static void sendJadeTemplate(final String sender,
                                        final String recipients,
                                        final String template,
                                        final Map<String, Object> model)
        throws JadeCompilerException, JadeException, IOException, AddressException, MessagingException {

        //
        // Add generic key stuff to the email model
        //
        genericKeys.forEach(key -> model.put(key, Global.INST.getAppResources().getString(key, null)));

        //
        // TODO: Add in internationalization by reading into the model a list of internationalizations.
        // Tack them into the map as a map called "trans" or something like that.
        // Should have a common set and a set specific to this template. The common set so that we
        // don't have to repeat ourselves for translations across emails, and a specific set for those
        // translations that are clearly a one-use case (just for this email template only).
        // Should we make our own cache for those then too so that these are cached along with the templates?
        // Probably.
        //
        String subject = StringUtilities.readResourceToString(getPrefix(template) + "/subject.txt");
        if (subject == null) {
            subject = template;
        } else {
            AttrString attrString = new AttrString();
            attrString.add(model);
            subject = attrString.computeString(subject);
        }

        Global.INST.getEmailer().send(sender,
                                      recipients,
                                      subject,
                                      getJadeEmailBody(template, model, true));
    }
}
