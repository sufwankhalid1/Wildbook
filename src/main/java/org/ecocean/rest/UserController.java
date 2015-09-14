package org.ecocean.rest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.ecocean.CommonConfiguration;
import org.ecocean.security.Stormpath;
import org.ecocean.security.User;
import org.ecocean.security.UserFactory;
import org.ecocean.security.UserToken;
import org.ecocean.servlet.ServletUtilities;
import org.ecocean.util.LogBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.resource.ResourceException;

@RestController
@RequestMapping(value = "/obj/user")
public class UserController {

    private static Logger logger = LoggerFactory.getLogger(MediaSubmissionController.class);
    private static String DEFAULT_SURNAME = "-";

    private static User createUserFromStormpath(final Account acc, final String password) {
        //
        // Don't use username from stormpath if it is the same as
        // the email. We don't want the email address to be the username for
        // security issues.
        //
        String uname;
        if (acc.getEmail().equals(acc.getUsername())) {
            uname = null;
        } else {
            uname = acc.getUsername();
        }

        // so non-international of us!
        User user = new User(null, uname, acc.getGivenName() + " " + acc.getSurname(), acc.getEmail());
        user.initPassword(password);

        //
        // Let's assume that new stormpath people have accepted the
        // user agreement. They should be saying yes to this as they log in anyway. Shouldn't
        // be a separate thing.
        //
        user.setAcceptedUserAgreement(true);
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
                                         final String password) throws DatabaseException {
        try (Database db = ServletUtilities.getDb(request)) {
            User user = UserFactory.getUserByNameOrEmail(db, username);

            Account acc = Stormpath.getAccount(username);

            if (acc == null) {
                if (user == null) {
                    return null;
                }

                String hashedPass = ServletUtilities.hashAndSaltPassword(password, user.getSalt());
                return new UserToken(user, new UsernamePasswordToken(user.getUserId().toString(), hashedPass));
            }

            String hashedPass;
            try {
                Stormpath.loginAccount(username, password);
                //
                // If they got their user through stormpath then we trust their
                // password to be correct so we use the value from the db. Should be the
                // same as that on stormpath?
                //
                if (user == null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("successful authentication via Stormpath, but no Wildbook user for email "
                                + acc.getEmail()
                                + ". creating one!");
                    }
                    user = createUserFromStormpath(acc, password);
                    UserFactory.saveUser(db, user);
                }

                hashedPass = user.getHashedPass();
            } catch (ResourceException ex) {
                logger.warn("failed to authenticate user '"
                        + username
                        + "' via Stormpath; falling back to Wildbook User: "
                        + ex.toString());
                hashedPass = ServletUtilities.hashAndSaltPassword(password, user.getSalt());
            }

            return new UserToken(user, new UsernamePasswordToken(user.getUserId().toString(), hashedPass));
        }
    }


    @RequestMapping(value = "isloggedin", method = RequestMethod.GET)
    public static SimpleUser isLoggedIn(final HttpServletRequest request) throws DatabaseException {
        if (request.getUserPrincipal() == null) {
            return null;
        }

        Integer userid = NumberUtils.createInteger(request.getUserPrincipal().getName());

        return SimpleFactory.getUser(userid);
    }


    @RequestMapping(value = "login", method = RequestMethod.POST)
    public SimpleUser loginCall(final HttpServletRequest request,
                                @RequestBody
                                final LoginAttempt loginAttempt) throws DatabaseException
    {
        UserToken userToken = getUserToken(request, loginAttempt.username, loginAttempt.password);

        if (userToken == null) {
            throw new SecurityException("No user with username [" + loginAttempt.username + "] is found.");
        }

        try {
            Subject subject = SecurityUtils.getSubject();
            subject.login(userToken.getToken());

            return userToken.getUser().toSimple();
        } finally {
            userToken.clear();
        }
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
            name[1] = DEFAULT_SURNAME;
        }

        return name;
    }


    @RequestMapping(value = "verify", method = RequestMethod.POST)
    public UserVerify verifyEmail(final HttpServletRequest request,
                                  @RequestBody @Valid final UserInfo userInfo) throws DatabaseException {
        if (userInfo == null) {
            throw new IllegalArgumentException("Null argument passed.");
        }

        UserVerify verify = new UserVerify();

        try (Database db = ServletUtilities.getDb(request)) {
            User user = UserFactory.getUserByEmail(db, userInfo.email);
            if (user == null) {
                //
                // Check to see if for some reason we have this user on stormpath.
                // If not create it.
                //
                String password = Stormpath.randomInitialPassword();
                Account acc = Stormpath.getAccount(userInfo.email);
                if (acc == null) {
                    String[] name = parseName(userInfo.fullName);
                    acc = Stormpath.createAccount(name[0], name[1], userInfo.email, password, null, null);

                    if (logger.isDebugEnabled()) {
                        logger.debug("successfully created Stormpath user for " + userInfo.email);
                    }
                } else {
                    //
                    // Case that should not happen but in case it does we need to keep the passwords
                    // matched up so we will ask them to reset the password I guess? maybe we should
                    // just delete the stormpath account and recreate it? Can we even do that with the api?
                    //
                    if (logger.isDebugEnabled()) {
                        logger.debug("found existing Stormpath user for " + userInfo.email);
                    }
                    Stormpath.getApplication().sendPasswordResetEmail(userInfo.email);
                }

                User wbuser = createUserFromStormpath(acc, password);
                UserFactory.saveUser(db, wbuser);
            }

            verify.user = user.toSimple();
            verify.verified = user.isVerified();

            return verify;
        }
    }


    @RequestMapping(value = "sendpassreset", method = RequestMethod.POST)
    public void sendResetEmail(final HttpServletRequest request,
                               @RequestBody @Valid final String email) {
        if (logger.isDebugEnabled()) {
            logger.debug("Sending reset email for address [" + email + "]");
        }
        //
        // We need to check to see that we have an account on stormpath first.
        // If we don't we will get an error with this call, so I guess create one?
        // I think givenName can not be null so use first part of email address as given name?
        // Bleh.
        //
        Account account = Stormpath.getAccount(email);
        if (account == null) {
            int index = email.indexOf("@");
            account = Stormpath.createAccount(email.substring(0,index),
                                              DEFAULT_SURNAME,
                                              email,
                                              Stormpath.randomInitialPassword(),
                                              null,
                                              null);
        }

        Stormpath.getApplication().sendPasswordResetEmail(email);
    }


    @RequestMapping(value = "resetpass", method = RequestMethod.POST)
    public void resetPassword(final HttpServletRequest request,
                              @RequestBody final ResetPass reset) throws DatabaseException {
        if (logger.isDebugEnabled()) {
            logger.debug(LogBuilder.quickLog("token", reset.token));
            logger.debug(LogBuilder.quickLog("password", reset.password));
        }
        Account acc = Stormpath.getApplication().resetPassword(reset.token, reset.password);
        if (acc == null) {
            return;
        }

        try (Database db = ServletUtilities.getDb(request)) {
            User user = UserFactory.getUserByEmail(db, acc.getEmail());
            user.resetPassword(reset.password);
            user.setVerified(true);
            UserFactory.saveUser(db, user);
        }
    }


    @RequestMapping(value = "verifypasstoken", method = RequestMethod.POST)
    public void verifyPassToken(final HttpServletRequest request,
                                @RequestBody @Valid final String token) {
        Stormpath.getApplication().verifyPasswordResetToken(token);
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
        public boolean verified;
        public boolean newlyCreated;
    }

    static class ResetPass {
        public String token;
        public String password;
    }
}

