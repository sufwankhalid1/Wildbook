package org.ecocean.rest;


import javax.servlet.http.HttpServletRequest;

import org.ecocean.ShepherdPMF;
import org.ecocean.servlet.ServletUtilities;
import org.ecocean.util.LogBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;

@RestController
@RequestMapping(value = "/data")
public class IndividualController
{
    private static Logger logger = LoggerFactory.getLogger(IndividualController.class);

    @RequestMapping(value = "/individual/get/{id}", method = RequestMethod.GET)
    public SimpleIndividual getIndividual(final HttpServletRequest request,
                                          @PathVariable("id")
                                          final String id) throws DatabaseException
    {
        return SimpleFactory.getIndividual(ServletUtilities.getContext(request),
                                           ServletUtilities.getConfigDir(request),
                                           id);
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
                                final String username)
    {
        UserInfo userinfo;
        userinfo = new UserInfo(SimpleFactory.getUser(ServletUtilities.getContext(request), username));

        //
        // Add:
        // 1) Highlighted photos. (4 random photos if no highlighted one's?)
        // 2) Total photo submission count
        // 3) Number of encounters
        // 4) Indivduals identified (unique Individuals through Encounters
        // 5) Voyages on
        //
        try (Database db = new Database(ShepherdPMF.getConnectionInfo())) {
//            RecordSet rs = db.getRecordSet(sql);
//            List<SinglePhotoVideo> spvs = new ArrayList<SinglePhotoVideo>();
//            while (rs.next()) {
//
//            }
        }

        return userinfo;
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
}
