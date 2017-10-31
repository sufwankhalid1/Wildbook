package org.ecocean.plugins.WildbookIA;

import java.util.HashMap;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.UUID;
import java.net.URL;
import org.ecocean.CommonConfiguration;
import org.ecocean.RestClient;
import java.net.MalformedURLException;
import java.util.List;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;

/*
import org.ecocean.ImageAttributes;

import org.ecocean.Annotation;
import org.ecocean.Util;
import org.ecocean.YouTube;
import org.ecocean.media.YouTubeAssetStore;
import org.ecocean.ocr.ocr;
//import org.ecocean.youtube.PostQuestion;
import org.ecocean.translate.DetectTranslate;
import org.ecocean.Shepherd;
import org.ecocean.ShepherdProperties;
import org.ecocean.Encounter;
import org.ecocean.Occurrence;
import org.ecocean.MarkedIndividual;
import org.ecocean.ContextConfiguration;
import org.ecocean.servlet.ServletUtilities;
import org.ecocean.CommonConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.ecocean.media.*;
import javax.servlet.ServletException;
import java.io.File;
import java.io.PrintWriter;
import org.joda.time.DateTime;
import org.apache.commons.lang3.StringUtils;
import javax.servlet.http.HttpServletRequest;

import java.util.concurrent.atomic.AtomicBoolean;

//date time
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.text.DateFormatSymbols;
import java.util.Locale;
//natural language processing for date/time
//import com.joestelmach.natty.*;
import java.util.Date;
import org.joda.time.Instant;

import twitter4j.Status;
import twitter4j.*;

*/

////// TODO some day bring identify/IBEISIA over here!!!!!

public class Comm {
    private static String CONFIG_NAME_BASEURL = "IBEISIARestUrlBase";
    private static HashMap<String,URL> urlCache = new HashMap<String,URL>();

    public static URL getBaseUrl(String context) throws MalformedURLException {
        if (urlCache.get(CONFIG_NAME_BASEURL) != null) return urlCache.get(CONFIG_NAME_BASEURL);
        String u = CommonConfiguration.getProperty(CONFIG_NAME_BASEURL, context);
        if (u == null) throw new MalformedURLException("URL configuration not set for '" + CONFIG_NAME_BASEURL + "'");
        urlCache.put(CONFIG_NAME_BASEURL, new URL(u));
        return urlCache.get(CONFIG_NAME_BASEURL);
    }
    public static URL getUrl(String name, String context) throws MalformedURLException {
        if (urlCache.get(name) != null) return urlCache.get(name);
        String u = CommonConfiguration.getProperty(name, context);
        if (u == null) throw new MalformedURLException("URL configuration not set for '" + name + "'");
        if (u.startsWith("/")) {
            urlCache.put(name, new URL(getBaseUrl(context), u));
        } else {
            urlCache.put(name, new URL(u));
        }
        return urlCache.get(name);
    }

    public static JSONObject post(String urlName, String context, HashMap<String,Object> data) throws RuntimeException, MalformedURLException, IOException, NoSuchAlgorithmException, InvalidKeyException {
System.out.println("FAKE Comm.post to " + getUrl(urlName, context) + " ------------------------------\n" + hashMapToJSONObject(data) + "\n---------------"); return null;
        //return RestClient.post(getUrl(urlName, context), hashMapToJSONObject(data));
    }

/*
        //this should only be checking for missing images, i guess?
        boolean tryAgain = true;
        JSONObject res = null;
        while (tryAgain) {
            res = RestClient.post(url, hashMapToJSONObject(map));
            tryAgain = iaCheckMissing(res, context);
        }
        return res;
*/


    //// TOTAL HACK... buy jon a drink and he will tell you about these.....
    public static JSONObject hashMapToJSONObject(HashMap<String,Object> map) {
        if (map == null) return null;
        //return new JSONObject(map);  // this *used to work*, i swear!!!
        JSONObject rtn = new JSONObject();
        for (String k : map.keySet()) {
            rtn.put(k, map.get(k));
        }
        return rtn;
    }

    public static String fromFancyUUID(JSONObject u) {
        if (u == null) return null;
        return u.optString("__UUID__", null);
    }
    public static JSONObject toFancyUUID(UUID u) {
        return toFancyUUID(u.toString());
    }
    public static JSONObject toFancyUUID(String u) {
        JSONObject j = new JSONObject();
        j.put("__UUID__", u);
        return j;
    }
}
