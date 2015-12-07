package org.ecocean.rest;

import javax.servlet.http.HttpServletRequest;

import org.ecocean.security.User;
import org.ecocean.security.UserFactory;
import org.ecocean.servlet.ServletUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;

@RestController
@RequestMapping(value = "/useradmin")
public class UserAdminController {

//    private static Logger logger = LoggerFactory.getLogger(MediaSubmissionController.class);

    @RequestMapping(value = "user/{userid}", method = RequestMethod.GET)
    public User getUser(final HttpServletRequest request,
                        @PathVariable("userid") final int userid) throws DatabaseException {
        try (Database db = ServletUtils.getDb(request)) {
            return UserFactory.getUserById(db, userid);
        }
    }
}