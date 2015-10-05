package org.ecocean.rest;

import javax.servlet.http.HttpServletRequest;

import org.ecocean.servlet.ServletUtilities;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/util")
public class UtilController {
    @RequestMapping(value = "/render", method = RequestMethod.GET)
    public String renderJade(final HttpServletRequest request,
                             @RequestParam("j")
                             final String template)
    {
        return ServletUtilities.renderJade(request, template);
    }
}
