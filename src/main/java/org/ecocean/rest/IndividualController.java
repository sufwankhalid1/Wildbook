package org.ecocean.rest;


import javax.servlet.http.HttpServletRequest;

import org.ecocean.servlet.ServletUtilities;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/data")
public class IndividualController
{
    @RequestMapping(value = "/individual/get/{id}", method = RequestMethod.GET)
    public SimpleIndividual getIndividual(final HttpServletRequest request,
                                          @PathVariable("id")
                                          final String id)
    {
        System.out.println(ServletUtilities.getConfigDir(request));
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
}