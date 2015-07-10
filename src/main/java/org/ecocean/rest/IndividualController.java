package org.ecocean.rest;


import javax.servlet.http.HttpServletRequest;

import org.ecocean.MarkedIndividual;
import org.ecocean.Shepherd;
import org.ecocean.servlet.ServletUtilities;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.DatabaseException;

@RestController
@RequestMapping(value = "/obj/individual")
public class IndividualController
{
    @RequestMapping(value = "/get/{id}", method = RequestMethod.GET)
    public SimpleIndividual get(final HttpServletRequest request,
                                @PathVariable("id")
                                final String id)
        throws DatabaseException
    {
        String context = ServletUtilities.getContext(request);
        Shepherd myShepherd = new Shepherd(context);
        MarkedIndividual mi = myShepherd.getMarkedIndividual(id);

        SimpleIndividual ind = SimpleIndividual.fromMarkedIndividual(mi, context);

        //
        // TODO: Add submitter info using submitterId. Pass in Shepherd? Add submitterId
        // to SimpleEncounter and THEN look it up here?
        //
//        User user = myShepherd.getUser(submitterId);

        return ind;
    }
}