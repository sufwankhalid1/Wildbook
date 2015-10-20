package org.ecocean.security;

import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.account.AccountList;
import com.stormpath.sdk.api.ApiKeyBuilder;
import com.stormpath.sdk.api.ApiKeys;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.application.ApplicationList;
import com.stormpath.sdk.application.Applications;
import com.stormpath.sdk.authc.AuthenticationResult;
import com.stormpath.sdk.authc.UsernamePasswordRequest;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.client.Clients;
import com.stormpath.sdk.directory.CustomData;
import com.stormpath.sdk.resource.ResourceException;
import com.stormpath.sdk.tenant.Tenant;

/************************************************************
      DOCS:  http://docs.stormpath.com/java/quickstart/
*************************************************************/

public class Stormpath {
    private static Logger logger = LoggerFactory.getLogger(Stormpath.class);

    /*  note: docs say "The client instance is intended to be an application singleton. You should reuse this instance throughout your application code.
        You should not create multiple Client instances as it could negatively affect caching." ... so this is what we reuse */
    private static Client client = null;

    public static String DEFAULT_SURNAME = "-";

    // we cache the application too.  is this lame?
    private static Application application = null;

    public static void init(final String id, final String secret, final String appName) {
        ApiKeyBuilder keyBuilder = ApiKeys.builder();
        keyBuilder.setId(id);
        keyBuilder.setSecret(secret);

        client = Clients.builder().setApiKey(keyBuilder.build()).build();

        //If using Google App Engine, you must use Basic authentication:
        //Client client = Clients.builder().setApiKey(apiKey)
        //    .setAuthenticationScheme(AuthenticationScheme.BASIC)
        //    .build();

        //
        // TODO: Pass the appName in as a configuration property.
        //
        Tenant tenant = client.getCurrentTenant();
        ApplicationList applications = tenant.getApplications(
            Applications.where(Applications.name().eqIgnoreCase(appName))
        );
        application = applications.iterator().next();
    }


//    public static Client getClient(final String configDir) {
//        if (myClient != null) return myClient;
//
//        ApiKey apiKey = ApiKeys.builder().setFileLocation(configDir + "/stormpathApiKey.properties").build();
//        myClient = Clients.builder().setApiKey(apiKey).build();
//        //If using Google App Engine, you must use Basic authentication:
//        //Client client = Clients.builder().setApiKey(apiKey)
//        //    .setAuthenticationScheme(AuthenticationScheme.BASIC)
//        //    .build();
//        return myClient;
//    }


    public static Application getApplication() {
        return application;
    }

    //note: username and custom are optional (username becomes email address if not provided); the rest are required
    public static Account createAccount(final String givenName,
                                        final String surname,
                                        final String email,
                                        final String password,
                                        final String username,
                                        final HashMap<String,Object> custom) {
        if (StringUtils.isBlank(givenName)
                || StringUtils.isBlank(surname)
                || StringUtils.isBlank(email)
                || StringUtils.isBlank(password)) {
            throw new IllegalArgumentException("missing required fields to create user");
        }

        Account account = client.instantiate(Account.class);
        account.setGivenName(givenName);
        account.setSurname(surname);
        account.setEmail(email);
        account.setPassword(password);
        if (!StringUtils.isBlank(username)) {
            account.setUsername(username);
        }

        if (custom != null) {
            CustomData customData = account.getCustomData();
            for (String k : custom.keySet()) {
                customData.put(k, custom.get(k));
            }
        }
        application.createAccount(account);
        return account;
    }

    public static Account getAccount(final String email) {
        if (application == null) {
            return null;
        }

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("username", email);
        AccountList accs = application.getAccounts(map);

        if ((accs == null) || (accs.getSize() < 1)) {
            return null;
        }

        return accs.iterator().next();
    }

//    public static AccountList getAccounts(final String username) {
//        HashMap<String, Object> q = new HashMap<String, Object>();
//        q.put("username", username);
//        return application.getAccounts(q);
//    }

    public static User createUserFromStormpath(final Account acc, final String password) {
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

    public static User getOrCreateUser(final String email, final String fullName) {
        //
        // Check to see if for some reason we have this user on stormpath.
        // If not create it.
        //
        String password = Stormpath.randomInitialPassword();
        Account acc = Stormpath.getAccount(email);
        if (acc == null) {
            String[] name = parseName(fullName);
            acc = Stormpath.createAccount(name[0], name[1], email, password, null, null);

            if (logger.isDebugEnabled()) {
                logger.debug("successfully created Stormpath user for " + email);
            }
        } else {
            //
            // Case that should not happen but in case it does we need to keep the passwords
            // matched up so we will ask them to reset the password I guess? maybe we should
            // just delete the stormpath account and recreate it? Can we even do that with the api?
            //
            if (logger.isDebugEnabled()) {
                logger.debug("found existing Stormpath user for " + email);
            }
            Stormpath.getApplication().sendPasswordResetEmail(email);
        }

        return createUserFromStormpath(acc, password);
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

    public static void verifyPasswordResetToken(final String token) {
        Stormpath.getApplication().verifyPasswordResetToken(token);
    }

    public static Account resetPassword(final String token, final String password) {
        return Stormpath.getApplication().resetPassword(token, password);
    }

    public static void sendPasswordResetEmail(final String email) {
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


    //satisfies Stormcloud requirements, and is sufficiently unguessable
    public static String randomInitialPassword() {
        return "X" + UUID.randomUUID().toString() + "X";
    }

    //note: "username" can also be email, apparently
    public static Account loginAccount(final String username, final String password) throws ResourceException {
        UsernamePasswordRequest req = new UsernamePasswordRequest(username, password);
        AuthenticationResult res = application.authenticateAccount(req);
        return res.getAccount();
    }
}
