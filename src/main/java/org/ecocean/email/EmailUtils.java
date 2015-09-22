package org.ecocean.email;

import java.io.IOException;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.ecocean.Global;
import org.ecocean.util.Jade4JUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samsix.util.string.AttrString;
import com.samsix.util.string.StringUtilities;

import de.neuland.jade4j.exceptions.JadeCompilerException;
import de.neuland.jade4j.exceptions.JadeException;

//import jsoup here?

public class EmailUtils {
    private static Logger logger = LoggerFactory.getLogger(EmailUtils.class);

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

    public static String getJadeEmailBody(final String template, final Map<String, Object> model)
            throws JadeCompilerException, JadeException, IOException
    {
        return Jade4JUtils.renderCP(getPrefix(template) + "/body.jade", model);
    }
    public static String getJadeEmailSubject(final String template, final Map<String, Object> model)
            throws JadeCompilerException, JadeException, IOException
    {
        return Jade4JUtils.renderCP(getPrefix(template) + "/subject.jade", model);
    }
    //Is this legit?
  //   public static String inlineCss(final String html) {
  //       final String style = "style";
  //       Document doc = Jsoup.parse(html);
  //       Elements els = doc.select(style);// to get all the style elements
  //       for (Element e : els) {
  //         String styleRules = e.getAllElements().get(0).data().replaceAll("\n", "").trim();
  //         String delims = "{}";
  //         StringTokenizer st = new StringTokenizer(styleRules, delims);
  //         while (st.countTokens() > 1) {
  //           String selector = st.nextToken(), properties = st.nextToken();
  //           if (!selector.contains(":")) { // skip a:hover rules, etc.
  //             Elements selectedElements = doc.select(selector);
  //             for (Element selElem : selectedElements) {
  //               String oldProperties = selElem.attr(style);
  //               selElem.attr(style,
  //                   oldProperties.length() > 0 ? concatenateProperties(
  //                       oldProperties, properties) : properties);
  //             }
  //           }
  //         }
  //         e.remove();
  //       }
  //       return doc.toString();
  //   }

  // private static String concatenateProperties(String oldProp, String newProp) {
  //   oldProp = oldProp.trim();
  //   if (!oldProp.endsWith(";"))
  //     oldProp += ";";
  //   return oldProp + newProp.replaceAll("\\s{2,}", " ");
  // }


    public static void sendJadeTemplate(final String sender,
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
        String subject = getJadeEmailSubject(template, model);
        if (subject == null) {
            subject = template;
        } else {
            AttrString attrString = new AttrString();
            attrString.add(model);
            subject = attrString.computeString(subject);
        }

        Global.INST.getEmailer().send(sender,
                                      recipients,
                                      getJadeEmailSubject(template, model),
                                      getJadeEmailBody(template, model));
    }
}
