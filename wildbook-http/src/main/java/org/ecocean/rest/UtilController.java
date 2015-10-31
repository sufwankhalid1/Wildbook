package org.ecocean.rest;

import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.ecocean.Global;
import org.ecocean.Organization;
import org.ecocean.Species;
import org.ecocean.servlet.ServletUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;

@RestController
@RequestMapping(value = "/util")
public class UtilController {
    @RequestMapping(value = "/render", method = RequestMethod.GET)
    public String renderJade(final HttpServletRequest request,
                             @RequestParam("j")
                             final String template)
    {
        return ServletUtils.renderJade(request, template);
    }

    @RequestMapping(value = "/init", method = RequestMethod.GET)
    public GlobalConfig init(final HttpServletRequest request) throws DatabaseException {
        try (Database db = ServletUtils.getDb(request)) {
            GlobalConfig config = new GlobalConfig();
            config.orgs = Global.INST.getUserService().getOrganizations();
            config.props = Global.INST.getWebappClientProps();
            config.species = Global.INST.getSpecies();
            return config;
        }
    }

    @SuppressWarnings("unused")
    private static class GlobalConfig
    {
        public Properties props;
        public List<Organization> orgs;
        public List<Species> species;
    }
}
