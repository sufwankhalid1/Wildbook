package org.ecocean.rest.admin;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.ecocean.CrewRole;
import org.ecocean.Global;
import org.ecocean.Organization;
import org.ecocean.Species;
import org.ecocean.Vessel;
import org.ecocean.VesselType;
import org.ecocean.admin.AdminFactory;
import org.ecocean.servlet.ServletUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;

@RestController
@RequestMapping(value = "admin/api/site")
public class AdminSiteController {

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
    public Integer saveVessel(final HttpServletRequest request,
                           @RequestBody @Valid final Vessel vessel)
       throws DatabaseException {
            try (Database db = ServletUtils.getDb(request)) {
                return AdminFactory.saveVessel(db, vessel);
            }
    }

    @RequestMapping(value = "savevesseltype", method = RequestMethod.POST)
    public int saveVesselType(final HttpServletRequest request,
                           @RequestBody @Valid final VesselType vesseltype)
       throws DatabaseException {
            try (Database db = ServletUtils.getDb(request)) {
                return AdminFactory.saveVesselType(db, vesseltype);
            }
    }

    @RequestMapping(value = "savecrewrole", method = RequestMethod.POST)
    public int saveCrewRole(final HttpServletRequest request,
                           @RequestBody @Valid final CrewRole crewrole)
       throws DatabaseException {
            try (Database db = ServletUtils.getDb(request)) {
                return AdminFactory.saveCrewRole(db, crewrole);
            }
    }

    @RequestMapping(value = "deletevessel/{id}", method = RequestMethod.POST)
    public List<Vessel> deleteVessel(final HttpServletRequest request) throws DatabaseException {
        try (Database db = ServletUtils.getDb(request)) {
            return AdminFactory.getVessels(db);
        }
    }

}
