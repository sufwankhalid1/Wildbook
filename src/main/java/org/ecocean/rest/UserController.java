package org.ecocean.rest;

import java.util.HashMap;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.ecocean.CommonConfiguration;
import org.ecocean.Shepherd;
import org.ecocean.User;
import org.ecocean.Util;
import org.ecocean.security.Stormpath;
import org.ecocean.security.UserToken;
import org.ecocean.servlet.ServletUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.DatabaseException;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.account.AccountList;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.resource.ResourceException;

@RestController
@RequestMapping(value = "/obj/user")
public class UserController {

    private static Logger logger = LoggerFactory.getLogger(MediaSubmissionController.class);

    private static User userFromStormpath(final Account acc, final String password) {
        //
        // Auto-generate username if none used or if that used is same as
        // the email. We don't want the email address to be the username for
        // security issues.
        //
        String uname = acc.getUsername();
        if (uname == null || uname.equals(acc.getEmail())) {
            uname = Util.generateUUID();
        }
        User user = new User(uname, password);

        user.setFullName(acc.getGivenName() + " " + acc.getSurname());  //so non-international of us!
        user.setEmailAddress(acc.getEmail());

        //
        // Let's assume that new stormpath people have accepted the
        // user agreement. They should be saying yes to this as they log in anyway. Shouldn't
        // be a separate thing.
        //
        user.setAcceptedUserAgreement(true);
        return user;
    }


    private static User getStormpathUser(final HttpServletRequest request,
                                         final Shepherd shepherd,
                                         final String username,
                                         final String password)
    {
        Client client = Stormpath.getClient(ServletUtilities.getConfigDir(request));

        if (client == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Stormpath not enabled.");
            }
            return null;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("checking Stormpath for login (username=" + username + ")...");
        }
        Account acc = null;
        try {
            acc = Stormpath.loginAccount(client, username, password);
        } catch (ResourceException ex) {
            logger.warn("failed to authenticate user '"
                    + username
                    + "' via Stormpath; falling back to Wildbook User: "
                    + ex.toString());
        }

        if (acc == null) {
            return null;
        }

        User user = shepherd.getUserByNameOrEmail(acc.getEmail());
        if (user == null) {
            //TODO we should probably have some kinda rules here: like stormpath user is a certain group etc
            if (logger.isDebugEnabled()) {
                logger.debug("successful authentication via Stormpath, but no Wildbook user for email "
                        + acc.getEmail()
                        + ". creating one!");
            }
            try {
                user = userFromStormpath(acc, password);
                shepherd.getPM().makePersistent(user);
            } catch (Exception ex) {
                logger.error("trouble creating Wildbook user from Stormpath: " + ex.toString());
                user = null;
            }
        }

        return user;
    }

    public static boolean notAcceptedTerms(final String context, final User user) {
        return (CommonConfiguration.getProperty("showUserAgreement",context) != null)
                && (CommonConfiguration.getProperty("userAgreementURL",context) != null)
                && (CommonConfiguration.getProperty("showUserAgreement",context).equals("true"))
                && (!user.getAcceptedUserAgreement());
    }


    public static UserToken getUserToken(final HttpServletRequest request,
                                         final String username,
                                         final String password) {
        Shepherd shepherd = ServletUtilities.getShepherd(request);
        User spUser = getStormpathUser(request, shepherd, username, password);
        User user;
        if (spUser == null) {
            //
            // Try our standard way
            //
            user = shepherd.getUserByNameOrEmail(username);

            if (user == null) {
                return null;
            }

        } else {
            user = spUser;
        }

        UsernamePasswordToken token;
        if (spUser == null) {
            token = new UsernamePasswordToken(user.getUsername(),
                                              ServletUtilities.hashAndSaltPassword(password, user.getSalt()));
        } else {
            //
            // If they got their user through stormpath then we trust their
            // password to be correct so we use the value from the db. Should be the
            // same as that on stormpath?
            //
            token = new UsernamePasswordToken(user.getUsername(), user.getPassword());
        }

        return new UserToken(user, token);
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<User> save(final HttpServletRequest request) {
        String context = "context0";
        context = ServletUtilities.getContext(request);
        Shepherd myShepherd = new Shepherd(context);
        User user = null;
        String username = null;
        if (request.getUserPrincipal() != null) username = request.getUserPrincipal().getName();
        if ((username != null) && !username.equals("")) user = myShepherd.getUser(username);
        if (user == null) {
            user = new User();
        }
        return new ResponseEntity<User>(user, HttpStatus.OK);
    }

    @RequestMapping(value = "simple", method = RequestMethod.GET)
    public SimpleUser getSimpleUser(final HttpServletRequest request) throws DatabaseException {
        String username = null;
        if (request.getUserPrincipal() != null) {
            username = request.getUserPrincipal().getName();
        }

        if (StringUtils.isBlank(username)) {
            return null;
        }
        return SimpleFactory.getUser(username);
    }

    @RequestMapping(value = "login", method = RequestMethod.POST)
    public SimpleUser loginCall(final HttpServletRequest request,
                                @RequestBody
                                final LoginAttempt loginAttempt)
    {
        UserToken userToken = getUserToken(request, loginAttempt.username, loginAttempt.password);

        if (userToken == null) {
            throw new SecurityException("No user with username [" + loginAttempt.username + "] is found.");
        }

        try {
            Subject subject = SecurityUtils.getSubject();
            subject.login(userToken.getToken());

            return SimpleFactory.fromUser(userToken.getUser());
        } finally {
            userToken.getToken().clear();
        }
    }


    @RequestMapping(value = "verify", method = RequestMethod.POST)
    public UserVerify verifyEmail(final HttpServletRequest request,
                                  @RequestBody @Valid final String email) {
        Client client = Stormpath.getClient(ServletUtilities.getConfigDir(request));

        if (client == null) {
            throw new IllegalArgumentException("Could not initiate Stormpath client.");
        }

        HashMap<String,Object> q = new HashMap<String,Object>();
        q.put("email", email);

        AccountList accs = Stormpath.getAccounts(client, q);
        if ((accs == null) || (accs.getSize() < 1)) {
            throw new IllegalArgumentException("Unknown user [" + email + "]");
        }

        Account acc = accs.iterator().next();

        UserVerify verify = new UserVerify();
        verify.user = SimpleFactory.getStormpathUser(acc);
        Object unverified = acc.getCustomData().get("unverified");
        if (unverified != null) {
            if (unverified instanceof Boolean) {
                verify.unverified = (Boolean) unverified;
            } else if (unverified instanceof String) {
                verify.unverified = Boolean.parseBoolean((String) unverified);
            }
        }

        return verify;
    }

    public static String[] parseName(final String fullname) {
        String[] name = new String[2];

        String givenName = "Unknown";
        if (! StringUtils.isBlank(fullname)) {
            givenName = fullname;
        }

        int si = givenName.indexOf(" ");
        if (si > -1) {
            name[0] = givenName.substring(0,si);
            name[1] = givenName.substring(si+1);
        } else {
            name[0] = givenName;
            //
            // WARNING: Stormpath requires a last name so we have to put *something* here.
            // Feel free to change it to something else if others agree but for now we are
            // using a dash as a placeholder.
            //
            name[1] = "-";
        }

        return name;
    }

    @RequestMapping(value = "create", method = RequestMethod.POST)
    public ResponseEntity<Object> createUser(final HttpServletRequest request,
                                             @RequestBody @Valid final UserInfo user) {
        Client client = Stormpath.getClient(ServletUtilities.getConfigDir(request));
        HashMap<String,Object> rtn = new HashMap<String,Object>();
        rtn.put("success", false);  //assuming rtn will only be used on errors -- user is returned upon success
        if (client == null) {
            rtn.put("message", "Could not initiate Stormpath client");
            return new ResponseEntity<Object>(rtn, HttpStatus.BAD_REQUEST);
        }
        if ((user == null) || Util.isEmpty(user.email)) {
            rtn.put("message", "Bad/invalid user or email passed");
            return new ResponseEntity<Object>(rtn, HttpStatus.BAD_REQUEST);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("checking on stormpath for username=" + user.email);
        }
        HashMap<String,Object> q = new HashMap<String,Object>();
        q.put("email", user.email);
        AccountList accs = Stormpath.getAccounts(client, q);
        if (accs.getSize() > 0) {
            rtn.put("message", "A user with this email already exists.");
            return new ResponseEntity<Object>(rtn, HttpStatus.BAD_REQUEST);
        }

        HashMap<String,Object> custom = new HashMap<String,Object>();
        custom.put("unverified", true);
        String errorMsg = null;
        Account acc = null;
        String[] name = parseName(user.fullName);
        String password = Stormpath.randomInitialPassword();
        try {
            acc = Stormpath.createAccount(client, name[0], name[1], user.email, password, null, custom);
            if (logger.isDebugEnabled()) {
                logger.debug("successfully created Stormpath user for " + user.email);
            }
        } catch (Exception ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("could not create Stormpath user for email=" + user.email + ": " + ex.toString());
            }
            errorMsg = ex.toString();
        }

        if (errorMsg == null) {
            //acc.setStatus(AccountStatus.UNVERIFIED);  //seems to have no effect, but also not sure if this is cool by Stormpath
            User wbuser = userFromStormpath(acc, password);
            PersistenceManager pm = ServletUtilities.getShepherd(request).getPM();
            try {
                pm.makePersistent(wbuser);
            } catch (Exception ex) {
                //not sure if this is actually a big deal, as i *think* the only way it could happen is if user already exists in wb???
                logger.error("could not create Wildbook User for email=" + user.email + ": " + ex.toString());
            }
            return new ResponseEntity<Object>(SimpleFactory.getStormpathUser(acc), HttpStatus.OK);
        } else {
            rtn.put("message", "There was an error creating the new user: " + errorMsg);
            return new ResponseEntity<Object>(rtn, HttpStatus.BAD_REQUEST);
        }
        //return new ResponseEntity<Object>(user, HttpStatus.OK);
    }

    //
    // LEAVE: This is just a test url that allows us to see if we have the correct
    // setting in our dispatcher-servlet.xml that forces Spring to not make assumptions
    // about a file type of the return value if there is a dot in the path param.
    //
    @RequestMapping(value = "test/{email:.+}", method = RequestMethod.GET)
    public UserVerifyInfo test(final HttpServletRequest request,
                               @PathVariable("email") final String email) {
        UserVerifyInfo info = new UserVerifyInfo();
        info.email = email + " - test";
        return info;
    }


    static class UserInfo {
        public String email;
        public String fullName;
    }

    static class UserVerifyInfo {
        public String email;
        public UserVerifyInfo() {
        }
        public String getEmail() {
            return email;
        }
    }

    static class LoginAttempt {
        public String username;
        public String password;
    }

    static class UserVerify {
        public SimpleUser user;
        public boolean unverified = true;
    }
}

