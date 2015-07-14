package org.ecocean.rest;


import javax.servlet.http.HttpServletRequest;

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
        return SimpleFactory.getIndividual(ServletUtilities.getContext(request), id);
    }
}