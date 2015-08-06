package org.ecocean.rest;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.ecocean.Encounter;
import org.ecocean.MarkedIndividual;
import org.ecocean.Shepherd;
import org.ecocean.servlet.ServletUtilities;
import org.ecocean.util.LogBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.DatabaseException;

@RestController
@RequestMapping(value = "/data")
public class IndividualController
{
    private static Logger logger = LoggerFactory.getLogger(IndividualController.class);

    @RequestMapping(value = "/individual/get/{id}", method = RequestMethod.GET)
    public IndividualInfo getIndividual(final HttpServletRequest request,
                                        @PathVariable("id")
                                        final String id) throws DatabaseException
    {
        String context = ServletUtilities.getContext(request);
        String configDir = ServletUtilities.getConfigDir(request);

        Shepherd myShepherd = new Shepherd(context);
        MarkedIndividual mi = myShepherd.getMarkedIndividual(id);

        if (mi == null) {
            return null;
        }

        IndividualInfo indinfo = new IndividualInfo();

        indinfo.individual = SimpleFactory.getIndividual(context, mi);
        indinfo.photos = SimpleFactory.getIndividualPhotos(context, indinfo.individual.getId());

        java.util.Iterator<Encounter> it = mi.getEncounters().iterator();
        while (it.hasNext()) {
            indinfo.addEncounter(SimpleFactory.getEncounter(context, configDir, it.next()));
        }

        return indinfo;
    }


    @RequestMapping(value = "/user/get/{username}", method = RequestMethod.GET)
    public SimpleUser getUser(final HttpServletRequest request,
                              @PathVariable("username")
                              final String username)
    {
        return SimpleFactory.getUser(ServletUtilities.getContext(request), username);
    }


    @RequestMapping(value = "/userinfo/get/{username}", method = RequestMethod.GET)
    public UserInfo getUserInfo(final HttpServletRequest request,
                                @PathVariable("username")
                                final String username) throws DatabaseException
    {
        return SimpleFactory.getUserInfo(ServletUtilities.getContext(request),
                                         ServletUtilities.getConfigDir(request),
                                         username);
    }


    @RequestMapping(value = "/config/value/{var}", method = RequestMethod.GET)
    public String getConfigValue(final HttpServletRequest request,
            @PathVariable("var")
            final String var)
    {
        if (logger.isDebugEnabled()) {
            logger.debug(LogBuilder.get()
                    .appendVar("var", var)
                    .appendVar("value", request.getServletContext().getInitParameter(var)).toString());
        }
        return request.getServletContext().getInitParameter(var);
    }

    public class IndividualInfo
    {
        public SimpleIndividual individual;
        public List<SimpleEncounter> encounters = new ArrayList<SimpleEncounter>();
        private List<SimplePhoto> photos;

        public void addEncounter(final SimpleEncounter encounter) {
            encounters.add(encounter);
        }

        public List<SimpleUser> getSubmitters() {
            //
            // Use a hash set to keep from getting duplicates
            //
            Set<SimpleUser> submitters = new HashSet<SimpleUser>();
            for (SimpleEncounter encounter : encounters) {
                if (encounter.getSubmitter() != null) {
                    submitters.add(encounter.getSubmitter());
                }
            }

            return new ArrayList<SimpleUser>(submitters);
        }
    }
}
