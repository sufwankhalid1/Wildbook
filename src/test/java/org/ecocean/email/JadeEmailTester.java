package org.ecocean.email;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.ecocean.Location;
import org.ecocean.rest.SimpleEncounter;
import org.ecocean.util.Jade4JUtils;
import org.junit.Test;

import com.vividsolutions.jts.util.Assert;

import de.neuland.jade4j.exceptions.JadeCompilerException;
import de.neuland.jade4j.exceptions.JadeException;

public class JadeEmailTester {
    @Test
    public void testRenderJade() throws JadeCompilerException, AddressException, JadeException, IOException, MessagingException {
        //
        // TODO: Figure out why the encounter date does not work.
        //
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("name", "Bob Wills");
        SimpleEncounter encounter = new SimpleEncounter(42, LocalDate.parse("2015-09-21"));
        encounter.setLocation(new Location("Santa Fe", 42.0, -120.9, null));

        model.put("encounter", encounter);

        String body = Jade4JUtils.renderCP("emails/test/furtherdown/body.jade", model);

        Assert.equals("<html><h1>Hello Bob Wills</h1><p>You saw something in: Santa Fe</p><h2>We have some footer stuff</h2></html>", body);
    }
}
