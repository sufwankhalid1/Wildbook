/*

https://github.com/pac4j/pac4j/wiki/Authenticate-with-Facebook,-Twitter-or-Google,-with-the-pac4j-library,-in-5-minutes

*/

package org.ecocean.security;

import org.ecocean.Global;
import org.pac4j.oauth.client.FacebookClient;
//for flickr
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FlickrApi;
import org.scribe.oauth.OAuthService;

public class SocialAuth {

    public static final String FLICKR_URL = "https://api.flickr.com/services/rest/";

    public static FacebookClient getFacebookClient(final String context) throws Exception {
        String appid = Global.INST.getAppResources().getString("social.facebook.auth.appid", null);
        String secret = Global.INST.getAppResources().getString("social.facebook.auth.secret", null);

        if (appid == null || secret == null) {
            return null;
        }
        return new FacebookClient(appid, secret);
    }



    public static OAuthService getFlickrOauth(final String context, final String callbackUrl) throws Exception {
        String key = Global.INST.getAppResources().getString("social.flickr.auth.key", null);
        String secret = Global.INST.getAppResources().getString("social.flickr.auth.secret", null);

        if (key == null || secret == null) {
            return null;
        }
        return new ServiceBuilder().provider(FlickrApi.class).apiKey(key).apiSecret(secret).callback(callbackUrl).build();
    }


}

