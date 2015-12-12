package org.ecocean.security;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.ecocean.Global;
import org.ecocean.util.LogBuilder;
import org.ecocean.util.WildbookUtils;
import org.slf4j.Logger;

public class WildbookBasicHttpAuthenticationFilter
        extends BasicHttpAuthenticationFilter {
    private static Logger logger = UserService.logger;

    @Override
    protected AuthenticationToken createToken(final ServletRequest request, final ServletResponse response) {
        if (logger.isDebugEnabled()) {
            logger.debug("WildbookBasicHttpAuthenticationFilter.createToken entered.");
        };

        String authorizationHeader = getAuthzHeader(request);
        if (authorizationHeader == null || authorizationHeader.length() == 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("No authorization header found.");
            };

            // Create an empty authentication token since there is no
            // Authorization header.
            return createToken("", "", request, response);
        }

        String[] prinCred = getPrincipalsAndCredentials(authorizationHeader, request);
        if (prinCred == null || prinCred.length < 2) {
            // Create an authentication token with an empty password,
            // since one hasn't been provided in the request.
            String username = prinCred == null || prinCred.length == 0 ? "" : prinCred[0];

            if (logger.isDebugEnabled()) {
                logger.debug("No principals and credentials found, using username [" + username + "]");
            };

            return createToken(username, "", request, response);
        }

        String username = prinCred[0];
        String password = prinCred[1];

        if (logger.isDebugEnabled()) {
            LogBuilder.get().appendVar("username", username).appendVar("password", password).debug(logger);;
        };

        try {
            UserService userService = Global.INST.getUserService();
            User user = userService.getUserByNameOrEmail(username);
            if (user == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("User not found, but pushing forward anyway");
                }
                //
                // What is going on here?
                // Refactored but just preserved the functionality that was there.
                //
                return createToken(username, WildbookUtils.hashAndSaltPassword(password, ""), request, response);
            }

            if (request.getParameter("acceptUserAgreement") != null){
                user.setAcceptedUserAgreement(true);
                userService.saveUser(user);
            }
            return createToken(username,
                               WildbookUtils.hashAndSaltPassword(password, user.getSalt()),
                               request,
                               response);
        } catch (Throwable ex) {
            logger.error("Error authenticating.", ex);
            //
            // Again, not sure about this. See above.
            //
            return createToken(username, WildbookUtils.hashAndSaltPassword(password, ""), request, response);
        }
    }
}
