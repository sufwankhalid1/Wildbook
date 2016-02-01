package org.ecocean.rest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.ecocean.Global;
import org.ecocean.Organization;
import org.ecocean.Species;
import org.ecocean.admin.AdminFactory;
import org.ecocean.servlet.ServletUtils;
import org.ecocean.survey.Vessel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;

@RestController
@RequestMapping(value = "/siteadmin")
public class SiteAdminController {
    private static Logger logger = LoggerFactory.getLogger(SiteAdminController.class);

    @RequestMapping(value = "saveorg", method = RequestMethod.POST)
    public int saveOrg(final HttpServletRequest request,
                       @RequestBody @Valid final Organization org) {
         Global.INST.getUserService().saveOrganization(org);
         return org.getOrgId();
    }

    @RequestMapping(value = "deleteorg/{id}", method = RequestMethod.POST)
    public void deleteOrg(final HttpServletRequest request,
                          @PathVariable("id") final int orgid) {
        Global.INST.getUserService().deleteOrganization(orgid);
    }

    @RequestMapping(value = "savespecies/{code}", method = RequestMethod.POST)
    public void saveSpecies(final HttpServletRequest request,
                            @RequestBody @Valid final Species species,
                            @PathVariable("code") final String oldCode) throws DatabaseException {
        try (Database db = ServletUtils.getDb(request)) {
            AdminFactory.saveSpecies(db, oldCode, species);
        }
    }

    @RequestMapping(value = "deletespecies", method = RequestMethod.POST)
    public void deleteSpecies(final HttpServletRequest request,
                              @RequestBody @Valid final String code) throws DatabaseException, Throwable {
        try (Database db = ServletUtils.getDb(request)) {
            AdminFactory.deleteSpecies(db, code);
        }
    }

    @RequestMapping(value = "getvessels", method = RequestMethod.GET)
    public List<Vessel> getVessels(final HttpServletRequest request) throws DatabaseException {
        try (Database db = ServletUtils.getDb(request)) {
            return AdminFactory.getVessels(db);
        }
    }

    @RequestMapping(value = "savevessel", method = RequestMethod.POST)
    public void saveVessel(final HttpServletRequest request,
                           @RequestBody @Valid final Vessel vessel)
       throws DatabaseException {
            logger.debug(vessel.getName(), vessel.getOrgId(), vessel.getTypeId(), vessel.getVesselId(),vessel.getTest());
            try (Database db = ServletUtils.getDb(request)) {
                AdminFactory.saveVessel(db, vessel);
            }
    }

    @RequestMapping(value = "deletevessel/{id}", method = RequestMethod.POST)
    public List<Vessel> deleteVessel(final HttpServletRequest request) throws DatabaseException {
        try (Database db = ServletUtils.getDb(request)) {
            return AdminFactory.getVessels(db);
        }
    }

}
