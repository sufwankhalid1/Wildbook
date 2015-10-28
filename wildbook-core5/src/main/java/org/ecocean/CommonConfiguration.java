/*
 * The Shepherd Project - A Mark-Recapture Framework
 * Copyright (C) 2011 Jason Holmberg
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.ecocean;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.ecocean.mmutil.StringUtilities;
import org.ecocean.servlet.ServletUtils;

public class CommonConfiguration {

  private static final String COMMON_CONFIGURATION_PROPERTIES = "commonConfiguration.properties";

  private static Map<String, Properties> propMap = new HashMap<String, Properties>();


  private static Properties get(final String context) {
    Properties props = propMap.get(context);
    if (props == null) {
      props = loadProps(context);
      propMap.put(context, props);
    }

    return props;
  }



  private static synchronized Properties loadProps(final String context) {
      Properties props=new Properties();
      try {
        props=ShepherdProperties.getProperties(COMMON_CONFIGURATION_PROPERTIES, "",context);

      } catch (Exception ioe) {
        ioe.printStackTrace();
      }

    return props;
  }

  //start getter methods
  public static String getURLLocation(final HttpServletRequest request) {
      return ServletUtils.getURLLocation(request);
  }


  /**
   * Utility method to return a {@code URI} instance for the specified
   * context path of the server relating to the servlet request.
   * This method ensures all appropriate encoding is performed for the respective
   * parts of the URI.
   * @param req HttpServletRequest for which to return server root URI
   * @param contextPath context path for the URI (must start with '/')
   * @return URI for the specified context path
   * @throws URISyntaxException if thrown when creating URI
   */
  public static URI getServerURI(final HttpServletRequest req, final String contextPath) throws URISyntaxException {
    return new URI(req.getScheme(), null, req.getServerName(), req.getServerPort(), contextPath, null, null).normalize();
  }

  /**
   * Utility method to return a URL string for the specified
   * context path of the server relating to the servlet request.
   * This method ensures all appropriate encoding is performed for the respective
   * parts of the URI.
   * @param req HttpServletRequest for which to return server root URL
   * @param contextPath context path for the URI (must start with '/')
   * @return URI string for the server's root (without context path)
   * @throws URISyntaxException if thrown when creating URI
   */
  public static String getServerURL(final HttpServletRequest req, final String contextPath) throws URISyntaxException {
    return getServerURI(req, contextPath).toASCIIString();
  }

  public static String getWikiLocation(final String context) {
    Properties props=get(context);
    if(props.getProperty("wikiLocation")!=null){return props.getProperty("wikiLocation").trim();}
    return null;
  }

  public static String getDBLocation(final String context) {
    return getProperty("dbLocation",context).trim();
  }

  public static String getR(final String context) {
    return getProperty("R", context).trim();
  }

  public static String getEpsilon(final String context) {
    return getProperty("epsilon",context).trim();
  }

  public static String getSizelim(final String context) {
    return getProperty("sizelim",context).trim();
  }

  public static String getMaxTriangleRotation(final String context) {
    return getProperty("maxTriangleRotation",context).trim();
  }

  public static String getC(final String context) {
    return getProperty("C",context).trim();
  }

  public static String getHTMLDescription(final String context) {
    return getProperty("htmlDescription",context).trim();
  }

  public static int getMaxMediaSizeInMegabytes(final String context){
    int maxSize=10;

    try{
      String sMaxSize=getProperty("maxMediaSize", context);
      if(sMaxSize!=null){
        Integer value=new Integer(sMaxSize);
        maxSize=value.intValue();
      }
    }
    catch(Exception e){
      System.out.println("\n\nHit an exception trying to read maxMediaSize property from commonConfiguration.properties.");
      e.printStackTrace();
    }
    return maxSize;
  }

  public static String getHTMLKeywords(final String context) {
    return getProperty("htmlKeywords",context).trim();
  }

  public static String getHTMLTitle(final String context) {
    return getProperty("htmlTitle",context).trim();
  }


  public static String getCSSURLLocation(final HttpServletRequest request, final String context) {
    return (request.getScheme() + "://" +
      getURLLocation(request) + "/" +
      getProperty("cssURLLocation",context)).trim();
  }

  public static String getHTMLAuthor(final String context) {
    return getProperty("htmlAuthor",context).trim();
  }

  public static String getHTMLShortcutIcon(final String context) {
    return getProperty("htmlShortcutIcon",context).trim();
  }

  public static String getGlobalUniqueIdentifierPrefix(final String context) {
    return getProperty("GlobalUniqueIdentifierPrefix",context);
  }

  public static String getURLToMastheadGraphic(final HttpServletRequest request, final String context) {
    String prop = getProperty("urlToMastheadGraphic",context);
    if (prop.startsWith("http:")) {
        return prop;
    }

    return "http://" + CommonConfiguration.getURLLocation(request) + prop;
  }

  public static String getURLToFooterGraphic(final String context) {
    return getProperty("urlToFooterGraphic",context);
  }

  public static String getGoogleMapsKey(final String context) {
    return getProperty("googleMapsKey",context);
  }

  public static String getGoogleSearchKey(final String context) {
    return getProperty("googleSearchKey",context);
  }

  public static String getProperty(final String name, final String context) {
    return get(context).getProperty(name);
  }

  public static Enumeration<?> getPropertyNames(final String context) {
    return get(context).propertyNames();
  }

  public static ArrayList<String> getSequentialPropertyValues(final String propertyPrefix, final String context){
    Properties myProps=get(context);
    //System.out.println(myProps.toString());
    ArrayList<String> returnThese=new ArrayList<String>();

    //System.out.println("Looking for: "+propertyPrefix);

    int iter=0;
    while(myProps.getProperty(propertyPrefix+iter)!=null){
      //System.out.println("Found: "+propertyPrefix+iter);
      returnThese.add(myProps.getProperty((propertyPrefix+iter)));
      iter++;
    }

    return returnThese;
  }


  /*
   * This method is used to determined the show/hide condition of an element of the UI.
   * It simply looks to see if a property is defined AND if the property is false.
   * For any other value or if the value is absent, the method returns true. Thsi means that conditional elements
   * are shown by default.
   *
   * @param thisString The name of the property to show/hide.
   * @return true if the property is not defined or has any other value than "false". Otherwise, returns false.
   */
  public static boolean showProperty(final String thisString, final String context) {
    if((getProperty(thisString, context)!=null)&&(getProperty(thisString, context).equals("false"))){return false;}
    return true;
  }

  /**
   * This configuration option defines whether nicknames are allowed for MarkedIndividual entries.
   *
   * @return true if nicknames are displayed for MarkedIndividual entries. False otherwise.
   */
  public static boolean allowNicknames(final String context) {
    get(context);
    boolean canNickname = true;
    if ((getProperty("allowNicknames",context) != null) && (getProperty("allowNicknames",context).equals("false"))) {
      canNickname = false;
    }
    return canNickname;
  }

  /**
   * This configuration option defines whether users can edit this catalog. Some studies may wish to use the framework only for data display.
   *
   * @return true if EXIF data should be shown. False otherwise.
   */
  public static boolean showEXIFData(final String context) {
    get(context);
    boolean showEXIF = true;
    if ((getProperty("showEXIF",context) != null) && (getProperty("showEXIF", context).equals("false"))) {
      showEXIF = false;
    }
    return showEXIF;
  }

  /**
   * This configuration option defines whether a pre-installed TapirLink provider will be used in conjunction with this database to expose mark-recapture data to biodiversity frameworks, such as the GBIF.
   *
   * @return true if a TapirLink provider is used with the framework. False otherwise.
   */
  public static boolean useTapirLinkURL(final String context) {
    get(context);
    boolean useTapirLink = true;
    if ((getProperty("tapirLinkURL",context) != null) && (getProperty("tapirLinkURL",context).equals("false"))) {
      useTapirLink = false;
    }
    return useTapirLink;
  }

  public static boolean showMeasurements(final String context) {
    return showCategory("showMeasurements",context);
  }

  public static boolean showMetalTags(final String context) {
    return showCategory("showMetalTags",context);
  }

  public static boolean showAcousticTag(final String context) {
    return showCategory("showAcousticTag",context);
  }

  public static boolean showSatelliteTag(final String context) {
    return showCategory("showSatelliteTag",context);
  }

  public static boolean showReleaseDate(final String context) {
    return showCategory("showReleaseDate",context);
  }

  public static String appendEmailRemoveHashString(final HttpServletRequest request, String
                                                   originalString, final String emailAddress, final String context) {
    get(context);
    if (getProperty("removeEmailString",context) != null) {
      originalString = originalString.replaceAll("REMOVEME",("\n\n" + getProperty("removeEmailString",context)
              + "\nhttp://"
              + getURLLocation(request)
              + "/removeEmailAddress.jsp?hashedEmail="
              + StringUtilities.getHashOfCommaList(emailAddress)));
    }
    return originalString;
  }

  public static Map<String, String> getIndexedValuesMap(final String baseKey, final String context) {
    Map<String, String> map = new TreeMap<>();
    boolean hasMore = true;
    int index = 0;
    while (hasMore) {
      String key = baseKey + index++;
      String value = CommonConfiguration.getProperty(key, context);
      if (value != null) {
        value = value.trim();
        if (value.length() > 0) {
          map.put(key, value.trim());
        }
      }
      else {
        hasMore = false;
      }
    }
    return map;
  }

  public static List<String> getIndexedValues(final String baseKey, final String context) {
    List<String> list = new ArrayList<String>();
    boolean hasMore = true;
    int index = 0;
    while (hasMore) {
      String key = baseKey + index++;
      String value = CommonConfiguration.getProperty(key, context);
      if (value != null) {
        value = value.trim();
        if (value.length() > 0) {
          list.add(value.trim());
        }
      }
      else {
        hasMore = false;
      }
    }
    return list;
  }

  public static Integer getIndexNumberForValue(final String baseKey, final String checkValue, final String context){
    System.out.println("getIndexNumberForValue started for baseKey "+baseKey+" and checkValue "+checkValue);
    boolean hasMore = true;
    int index = 0;
    while (hasMore) {
      String key = baseKey + index;
      String value = CommonConfiguration.getProperty(key, context);
      System.out.println("     key "+key+" and value "+value);
      if (value != null) {
        value = value.trim();
        System.out.println("CommonConfiguration: "+value);
        if(value.equals(checkValue)){return (new Integer(index));}
      }
      else {
        hasMore = false;
      }
      index++;
    }
    return null;
  }


  private static boolean showCategory(final String category, final String context) {
    String showMeasurements = getProperty(category,context);
    return !Boolean.FALSE.toString().equals(showMeasurements);
  }


  public static String getDataDirectoryName(final String context) {
      get(context);

      String dataDirectoryName = ContextConfiguration.getDataDirForContext(context);

      // if(props.getProperty("dataDirectoryName")!=null){return props.getProperty("dataDirectoryName").trim();}

      if (StringUtils.isEmpty(dataDirectoryName)) {
          return "shepherd_data_dir";
      }

      return dataDirectoryName;
  }


  public static File getDataDirectory(final String context) {
      return new File(new File("webapps"), getDataDirectoryName(context));
  }

  /**
   * This configuration option defines whether information about User objects associated with Encounters and MarkedIndividuals will be displayed to web site viewers.
   *
   * @return true if edits are allows. False otherwise.
   */
  public static boolean showUsersToPublic(final String context) {
    get(context);
    boolean showUsersToPublic = true;
    if ((getProperty("showUsersToPublic",context) != null) && (getProperty("showUsersToPublic",context).equals("false"))) {
      showUsersToPublic = false;
    }
    return showUsersToPublic;
  }

  /**
   * Gets the directory for holding website data ('shepherd_data_dir').
   * @param sc ServletContext as reference for finding directory
   * @return The data directory used for web application storage.
   * @throws FileNotFoundException if folder not found (or unable to create)
   */
  public static File getDataDirectory(final ServletContext sc, final String context) throws FileNotFoundException {
    String webappRoot = sc.getRealPath("/");
    File dataDir = new File(webappRoot).getParentFile();
    File f = new File(dataDir, getDataDirectoryName(context));
    if (!f.exists() && !f.mkdir())
      throw new FileNotFoundException("Unable to find/create folder: " + f.getAbsolutePath());
    return f;
  }

  /**
   * Gets the directory for holding user-specific data folders (i.e. parent
   * folder of each user-specific folder).
   * @param sc ServletContext as reference for finding directory
   * @return The user-specific data directory used for web application storage.
   * @throws FileNotFoundException if folder not found (or unable to create)
   */
  public static File getUsersDataDirectory(final ServletContext sc, final String context) throws FileNotFoundException {
    File f = new File(getDataDirectory(sc, context), "users");
    if (!f.exists() && !f.mkdir())
      throw new FileNotFoundException("Unable to find/create folder: " + f.getAbsolutePath());
    return f;
  }

  /**
   * Gets the directory for holding user-specific data (e.g. profile photo).
   * @param sc ServletContext as reference for finding directory
   * @param username username for which to locate directory
   * @return The user-specific data directory used for web application storage.
   * @throws FileNotFoundException if folder not found (or unable to create)
   */
  public static File getDataDirectoryForUser(final ServletContext sc, final String username, final String context) throws FileNotFoundException {
    if (username == null)
      throw new NullPointerException();
    if ("".equals(username.trim()))
      throw new IllegalArgumentException();
    File f = new File(getUsersDataDirectory(sc, context), username);
    if (!f.exists() && !f.mkdir())
      throw new FileNotFoundException("Unable to find/create folder: " + f.getAbsolutePath());
    return f;
  }


  public static boolean isIntegratedWithWildMe(final String context){

    get(context);
    boolean integrated = true;
    if ((getProperty("isIntegratedWithWildMe",context) != null) && (getProperty("isIntegratedWithWildMe",context).equals("false"))) {
      integrated = false;
    }
    return integrated;
  }


  /**
   * This configuration option defines whether the spot pattern recognition software embedded in the framework is used for the species under study.
   *
   * @return true if this catalog is for a species for which the spot pattern recognition software component can be used. False otherwise.
   */
  public static boolean useSpotPatternRecognition(final String context) {
//    CommonConfiguration.get(context);
    boolean useSpotPatternRecognition = true;
    if ((getProperty("useSpotPatternRecognition",context) != null)
          && (getProperty("useSpotPatternRecognition",context).equals("false"))) {
      useSpotPatternRecognition = false;
    }
    return useSpotPatternRecognition;
  }


  /**
   * This configuration option defines whether adoptions of MarkedIndividual or encounter objects are allowed. Generally adoptions are used as a fundraising or public awareness tool.
   *
   * @return true if adoption functionality should be displayed. False if adoptions are not supported in this catalog.
   */
  public static boolean allowAdoptions(final String context) {
    boolean canAdopt = true;
    if ((getProperty("allowAdoptions",context) != null) && (getProperty("allowAdoptions", context).equals("false"))) {
      canAdopt = false;
    }
    return canAdopt;
  }

  /**
   * This configuration option defines whether users can edit this catalog. Some studies may wish to use the framework only for data display.
   *
   * @return true if edits are allows. False otherwise.
   */
  public static boolean isCatalogEditable(final String context) {
    boolean isCatalogEditable = true;
    if ((getProperty("isCatalogEditable", context) != null) && (getProperty("isCatalogEditable", context).equals("false"))) {
      isCatalogEditable = false;
    }
    return isCatalogEditable;
  }


  /**
   * Helper method to parse boolean from string.
   * @param s string to parse
   * @param def default value
   * @return true if s is one of { true, yes, ok, 1 }
   */
  private static boolean parseBoolean(final String s, final boolean def) {
    if (s == null)
      return def;
    String prop = s.trim().toLowerCase(Locale.US);
    if ("true".equals(prop) || "yes".equals(prop) || "ok".equals(prop) || "1".equals(prop)) {
      return true;
    }
    return false;
  }
}
