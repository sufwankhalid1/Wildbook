package org.ecocean.rest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.ecocean.Global;
import org.ecocean.Organization;
import org.ecocean.Species;
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
@RequestMapping(value = "/siteadmin")
public class SiteAdminController {

    //save org
    @RequestMapping(value = "saveorg", method = RequestMethod.POST)
    public int saveOrg(final HttpServletRequest request,
            @RequestBody @Valid final Organization org) {
         Global.INST.getUserService().saveOrganization(org);
         return org.getOrgId();
    }
    
    //save species
    @RequestMapping(value = "savespecies/{code}", method = RequestMethod.POST)
    public void setSpecies(final HttpServletRequest request,
            @RequestBody @Valid final Species species,
            @PathVariable("code") final String oldCode) throws DatabaseException {
        try (Database db = ServletUtils.getDb(request)) {
            AdminFactory.saveSpecies(db, oldCode, species);
        }
    }
    
}
