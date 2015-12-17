package org.ecocean.rest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.ecocean.Global;
import org.ecocean.security.User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/useradmin")
public class UserAdminController {

//    private static Logger logger = LoggerFactory.getLogger(MediaSubmissionController.class);

    @RequestMapping(value = "user/{userid}", method = RequestMethod.GET)
    public User getUser(final HttpServletRequest request,
                        @PathVariable("userid") final int userid) {
        return Global.INST.getUserService().getUserById(String.valueOf(userid));
    }
    
    @RequestMapping(value = "usersave", method = RequestMethod.POST)
    public int saveUser(final HttpServletRequest request,
                        @RequestBody @Valid final User user) {
        Global.INST.getUserService().saveUser(user);
        return user.getUserId();
    }
    
    @RequestMapping(value = "editpw/{userid}", method = RequestMethod.POST)
    public void editPw(final HttpServletRequest request,
                      @PathVariable("userid") final int userid,
                      @RequestBody @Valid final String password) {
        Global.INST.getUserService().resetPass(String.valueOf(userid), password);
    }
}