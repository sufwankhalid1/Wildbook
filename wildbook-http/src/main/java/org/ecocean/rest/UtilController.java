package org.ecocean.rest;

import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.ecocean.CrewRole;
import org.ecocean.Global;
import org.ecocean.Organization;
import org.ecocean.Species;
import org.ecocean.VesselType;
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
            config.defaultSpecies = Global.INST.getSpecies(Global.INST.getAppResources().getString("species.default", null));
            config.imageTags = Global.INST.getAppResources().getStringList("imagetags", (List<String>) null);
            config.crewroles = Global.INST.getCrewRoles();
            config.vesseltypes = Global.INST.getVesselTypes();
            return config;
        }
    }

    @SuppressWarnings("unused")
    private static class GlobalConfig
    {
        public Properties props;
        public List<Organization> orgs;
        public List<Species> species;
        public Species defaultSpecies;
        public List<String> imageTags;
        public List<CrewRole> crewroles;
        public List<VesselType> vesseltypes;
    }
}
