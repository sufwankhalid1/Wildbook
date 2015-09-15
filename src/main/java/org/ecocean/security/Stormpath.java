package org.ecocean.security;




import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.ecocean.User;
import org.ecocean.Util;
import org.ecocean.rest.UserController;

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

    /*  note: docs say "The client instance is intended to be an application singleton. You should reuse this instance throughout your application code.
        You should not create multiple Client instances as it could negatively affect caching." ... so this is what we reuse */
    private static Client client = null;

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

    public static Account createAccount(final User user) throws Exception {
        String[] name = UserController.parseName(user.getFullName());
        HashMap<String,Object> h = new HashMap<String,Object>();
        h.put("creationNote", "created from Wildbook User");

        return createAccount(name[0], name[1], user.getEmailAddress(), randomInitialPassword(), user.getUsername(), h);
    }

    //satisfies Stormcloud requirements, and is sufficiently unguessable
    public static String randomInitialPassword() {
        return "X" + Util.generateUUID() + "X";
    }

    //note: "username" can also be email, apparently
    public static Account loginAccount(final String username, final String password) throws ResourceException {
        UsernamePasswordRequest req = new UsernamePasswordRequest(username, password);
        AuthenticationResult res = application.authenticateAccount(req);
        return res.getAccount();
    }
}
