package org.ecocean.rest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.ecocean.Global;
import org.ecocean.location.Country;
import org.ecocean.servlet.ServletUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.DatabaseException;

@RestController
@RequestMapping(value = "/api/util")
public class UtilController {
//    private final Logger logger = LoggerFactory.getLogger(UtilController.class);

    @RequestMapping(value = "/render", method = RequestMethod.GET)
    public String renderJade(final HttpServletRequest request,
                             @RequestParam("j")
                             final String template)
    {
        return ServletUtils.renderJade(request, template);
    }

    @RequestMapping(value = "/init", method = RequestMethod.GET)
    public GlobalConfig init(final HttpServletRequest request) throws DatabaseException {
        GlobalConfig config = new GlobalConfig();
        config.countries = Global.INST.getLocationService().getCountries();
        return config;
    }

    @SuppressWarnings("unused")
    private static class GlobalConfig
    {
        //public List<String> imageTags;
        public List<Country> countries;
    }
}
