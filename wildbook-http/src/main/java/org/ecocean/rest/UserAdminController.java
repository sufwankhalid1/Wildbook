package org.ecocean.rest;

import java.util.Collections;
import java.util.Set;

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
        return user.getId();
    }

    @RequestMapping(value = "editpw/{userid}", method = RequestMethod.POST)
    public void editPw(final HttpServletRequest request,
                      @PathVariable("userid") final int userid,
                      @RequestBody @Valid final String password) {
        Global.INST.getUserService().resetPass(String.valueOf(userid), password);
    }

    @RequestMapping(value = "userdelete", method = RequestMethod.POST)
    public void deleteUser(final HttpServletRequest request,
                           @RequestBody @Valid final User user) {
        Global.INST.getUserService().deleteUser(user);
    }

    @RequestMapping(value = "clearUserCache", method = RequestMethod.POST)
    public void clearUserCache(final HttpServletRequest request) {
        Global.INST.getUserService().clearUserCache();
    }

    @RequestMapping(value = "roles/{userid}", method = RequestMethod.GET)
    public Set<String> getRoles(final HttpServletRequest request,
                         @PathVariable("userid") final Integer userid) {
        Set<String> roles = Global.INST.getUserService().getAllRolesForUserInContext(userid.toString(), "context0");
        if (roles == null) {
            return Collections.emptySet();
        }
        return roles;
    }

    @RequestMapping(value = "roles/update/{userid}", method = RequestMethod.POST)
    public void updateRoles(final HttpServletRequest request,
                         @PathVariable("userid") final int userid,
                         @RequestBody @Valid final Set<String> roles) {
            Global.INST.getUserService().updateRoles(userid, "context0", roles);
            Global.INST.getUserService().clearUserCache();
    }

    @RequestMapping(value = "roles/remove/{userid}", method = RequestMethod.POST)
    public void deleteRoles(final HttpServletRequest request,
                         @PathVariable("userid") final Integer userid) {
            Global.INST.getUserService().deleteAllRoles(userid.toString());
            Global.INST.getUserService().clearUserCache();
    }
}











