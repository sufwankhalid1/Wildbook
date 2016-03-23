package org.ecocean.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ecocean.Global;
import org.ecocean.Individual;
import org.ecocean.email.Emailer;
import org.ecocean.encounter.EncounterFactory;
import org.ecocean.security.User;
import org.ecocean.servlet.ServletUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;

import de.neuland.jade4j.exceptions.JadeCompilerException;
import de.neuland.jade4j.exceptions.JadeException;

@RestController
@RequestMapping(value = "api/test/email")
public class EmailTester {
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public void testEmail(final HttpServletRequest request,
                          final HttpServletResponse response,
                          @RequestParam
                          final String template,
                          @RequestParam(defaultValue = "true")
                          final boolean inlinestyles)
        throws JadeCompilerException, JadeException, IOException, NumberFormatException, DatabaseException
    {
        Map<String, Object> model = new HashMap<>();

        try (Database db = ServletUtils.getDb(request)) {
            String individualId = request.getParameter("individualid");
            if (individualId != null) {
                Individual ind = EncounterFactory.getIndividual(db, Integer.parseInt(individualId));
                if (ind != null) {
                    model.put(Emailer.TAG_INDIVIDUAL, ind.toSimple());
                }
            }

            String userId = request.getParameter("userid");
            if (userId != null) {
                User user = Global.INST.getUserService().getUserById(userId);
                if (user != null) {
                    model.put(Emailer.TAG_USER, user.toSimple());
                }
            }
        }

        PrintWriter out = response.getWriter();
        out.println(Global.INST.getEmailer().getJadeEmailBody(template, model, inlinestyles));
    }
}
