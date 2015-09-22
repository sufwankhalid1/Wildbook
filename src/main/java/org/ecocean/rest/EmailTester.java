package org.ecocean.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ecocean.email.EmailUtils;
import org.ecocean.security.User;
import org.ecocean.security.UserFactory;
import org.ecocean.servlet.ServletUtilities;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;

import de.neuland.jade4j.exceptions.JadeCompilerException;
import de.neuland.jade4j.exceptions.JadeException;

@RestController
@RequestMapping(value = "/test/email")
public class EmailTester {
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public void testEmail(final HttpServletRequest request,
                          final HttpServletResponse response,
                          @RequestParam
                          final String template)
        throws JadeCompilerException, JadeException, IOException, NumberFormatException, DatabaseException
    {
        Map<String, Object> model = new HashMap<String, Object>();

        try (Database db = ServletUtilities.getDb(request)) {
            String individualId = request.getParameter("individualid");
            if (individualId != null) {
                SimpleIndividual ind = SimpleFactory.getIndividual(db, Integer.parseInt(individualId));
                if (ind != null) {
                    model.put("individual", ind);
                }
            }

            String userId = request.getParameter("userid");
            if (userId != null) {
                User user = UserFactory.getUserById(db, Integer.parseInt(userId));
                if (user !=null) {
                    model.put("user", user);
                }
            }
        }

        PrintWriter out = response.getWriter();
        out.println(EmailUtils.getJadeEmailBody(template, model));
    }
}
