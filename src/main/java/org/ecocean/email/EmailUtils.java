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
        String prefix = "emails/" + template;
        String subject = StringUtilities.readResourceToString(prefix + "/subject.text");
        if (subject == null) {
            subject = template;
        } else {
            AttrString attrString = new AttrString();
            attrString.add(model);
            subject = attrString.computeString(subject);
        }

        String body = Jade4JUtils.renderCP(prefix + "/body.jade", model);

        Global.INST.getEmailer().send(sender, recipients, subject, body);
    }
}
