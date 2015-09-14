package org.ecocean.security;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.ecocean.ShepherdPMF;
import org.ecocean.servlet.ServletUtilities;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;

public class WildbookBasicHttpAuthenticationFilter extends
    BasicHttpAuthenticationFilter {


@Override
protected AuthenticationToken createToken(final ServletRequest request, final ServletResponse response) {
        String authorizationHeader = getAuthzHeader(request);
        if (authorizationHeader == null || authorizationHeader.length() == 0) {
            // Create an empty authentication token since there is no
           // Authorization header.
            return createToken("", "", request, response);
       }

        String[] prinCred = getPrincipalsAndCredentials(authorizationHeader, request);
        if (prinCred == null || prinCred.length < 2) {
           // Create an authentication token with an empty password,
            // since one hasn't been provided in the request.
            String username = prinCred == null || prinCred.length == 0 ? "" : prinCred[0];
           return createToken(username, "", request, response);
        }

        String username = prinCred[0];
        String password = prinCred[1];

        try (Database db = ShepherdPMF.getDb()) {
            User user = UserFactory.getUserByNameOrEmail(db, username);
            if (user != null) {
                //
                // What is going on here?
                // Refactored but just preserved the functionality that was there.
                //
                return createToken(username, ServletUtilities.hashAndSaltPassword(password, ""), request, response);
            }

            if (request.getParameter("acceptUserAgreement") != null){
                user.setAcceptedUserAgreement(true);
                UserFactory.saveUser(db, user);
            }
            return createToken(username,
                               ServletUtilities.hashAndSaltPassword(password, user.getSalt()),
                               request,
                               response);
        } catch (DatabaseException ex) {
            //
            // Again, not sure about this. See above.
            //
            return createToken(username, ServletUtilities.hashAndSaltPassword(password, ""), request, response);
        }
  }



}
