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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.ecocean.mmutil.StringUtilities;

public class CommonConfiguration {

  private static final String COMMON_CONFIGURATION_PROPERTIES = "commonConfiguration.properties";

  private static Map<String, Properties> propMap = new HashMap<String, Properties>();


  private static Properties get(String context) {
    Properties props = propMap.get(context);
    if (props == null) {
      props = loadProps(context);
      propMap.put(context, props);
    }

    return props;
  }



  private static synchronized Properties loadProps(String context) {
      Properties props=new Properties();
      try {
        props=ShepherdProperties.getProperties(COMMON_CONFIGURATION_PROPERTIES, "",context);

      } catch (Exception ioe) {
        ioe.printStackTrace();
      }

    return props;
  }

  //start getter methods
  public static String getURLLocation(HttpServletRequest request) {
    return request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
  }

  public static String getMailHost(String context) {
    return getProperty("mailHost", context).trim();
  }


  public static String getWikiLocation(String context) {
    Properties props=get(context);
    if(props.getProperty("wikiLocation")!=null){return props.getProperty("wikiLocation").trim();}
    return null;
  }

  public static String getDBLocation(String context) {
    return getProperty("dbLocation",context).trim();
  }

  public static String getAutoEmailAddress(String context) {
    return getProperty("autoEmailAddress", context).trim();
  }

  public static String getNewSubmissionEmail(String context) {
    return getProperty("newSubmissionEmail",context).trim();
  }

  public static String getR(String context) {
    return getProperty("R", context).trim();
  }

  public static String getEpsilon(String context) {
    return getProperty("epsilon",context).trim();
  }

  public static String getSizelim(String context) {
    return getProperty("sizelim",context).trim();
  }

  public static String getMaxTriangleRotation(String context) {
    return getProperty("maxTriangleRotation",context).trim();
  }

  public static String getC(String context) {
    return getProperty("C",context).trim();
  }

  public static String getHTMLDescription(String context) {
    return getProperty("htmlDescription",context).trim();
  }

  public static int getMaxMediaSizeInMegabytes(String context){
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

  public static String getHTMLKeywords(String context) {
    return getProperty("htmlKeywords",context).trim();
  }

  public static String getHTMLTitle(String context) {
    return getProperty("htmlTitle",context).trim();
  }


  public static String getCSSURLLocation(HttpServletRequest request, String context) {
    return (request.getScheme() + "://" +
      getURLLocation(request) + "/" +
      getProperty("cssURLLocation",context)).trim();
  }

  public static String getHTMLAuthor(String context) {
    return getProperty("htmlAuthor",context).trim();
  }

  public static String getHTMLShortcutIcon(String context) {
    return getProperty("htmlShortcutIcon",context).trim();
  }

  public static String getGlobalUniqueIdentifierPrefix(String context) {
    return getProperty("GlobalUniqueIdentifierPrefix",context);
  }

  public static String getURLToMastheadGraphic(HttpServletRequest request, String context) {
    String prop = getProperty("urlToMastheadGraphic",context);
    if (prop.startsWith("http:")) {
        return prop;
    }

    return "http://" + CommonConfiguration.getURLLocation(request) + prop;
  }

  public static String getTapirLinkURL(String context) {
    return getProperty("tapirLinkURL",context);
  }

  public static String getIPTURL(String context) {
    return getProperty("iptURL",context);
  }

  public static String getURLToFooterGraphic(String context) {
    return getProperty("urlToFooterGraphic",context);
  }

  public static String getGoogleMapsKey(String context) {
    return getProperty("googleMapsKey",context);
  }

  public static String getGoogleSearchKey(String context) {
    return getProperty("googleSearchKey",context);
  }

  public static String getProperty(String name, String context) {
    return get(context).getProperty(name);
  }

  public static Enumeration<?> getPropertyNames(String context) {
    return get(context).propertyNames();
  }

  public static ArrayList<String> getSequentialPropertyValues(String propertyPrefix, String context){
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
  public static boolean showProperty(String thisString, String context) {
    if((getProperty(thisString, context)!=null)&&(getProperty(thisString, context).equals("false"))){return false;}
    return true;
  }

  /**
   * This configuration option defines whether adoptions of MarkedIndividual or encounter objects are allowed. Generally adoptions are used as a fundraising or public awareness tool.
   *
   * @return true if adoption functionality should be displayed. False if adoptions are not supported in this catalog.
   */
  public static boolean allowAdoptions(String context) {
    get(context);
    boolean canAdopt = true;
    if ((getProperty("allowAdoptions",context) != null) && (getProperty("allowAdoptions", context).equals("false"))) {
      canAdopt = false;
    }
    return canAdopt;
  }

  public static boolean sendEmailNotifications(String context) {
    get(context);
    boolean sendNotifications = true;
    if ((getProperty("sendEmailNotifications",context) != null) && (getProperty("sendEmailNotifications", context).equals("false"))) {
      sendNotifications = false;
    }
    return sendNotifications;
  }

  /**
   * This configuration option defines whether nicknames are allowed for MarkedIndividual entries.
   *
   * @return true if nicknames are displayed for MarkedIndividual entries. False otherwise.
   */
  public static boolean allowNicknames(String context) {
    get(context);
    boolean canNickname = true;
    if ((getProperty("allowNicknames",context) != null) && (getProperty("allowNicknames",context).equals("false"))) {
      canNickname = false;
    }
    return canNickname;
  }

  /**
   * This configuration option defines whether the spot pattern recognition software embedded in the framework is used for the species under study.
   *
   * @return true if this catalog is for a species for which the spot pattern recognition software component can be used. False otherwise.
   */
  public static boolean useSpotPatternRecognition(String context) {
    get(context);
    boolean useSpotPatternRecognition = true;
    if ((getProperty("useSpotPatternRecognition",context) != null) && (getProperty("useSpotPatternRecognition",context).equals("false"))) {
      useSpotPatternRecognition = false;
    }
    return useSpotPatternRecognition;
  }

  /**
   * This configuration option defines whether users can edit this catalog. Some studies may wish to use the framework only for data display.
   *
   * @return true if edits are allows. False otherwise.
   */
  public static boolean isCatalogEditable(String context) {
    get(context);
    boolean isCatalogEditable = true;
    if ((getProperty("isCatalogEditable", context) != null) && (getProperty("isCatalogEditable", context).equals("false"))) {
      isCatalogEditable = false;
    }
    return isCatalogEditable;
  }

  /**
   * This configuration option defines whether users can edit this catalog. Some studies may wish to use the framework only for data display.
   *
   * @return true if EXIF data should be shown. False otherwise.
   */
  public static boolean showEXIFData(String context) {
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
  public static boolean useTapirLinkURL(String context) {
    get(context);
    boolean useTapirLink = true;
    if ((getProperty("tapirLinkURL",context) != null) && (getProperty("tapirLinkURL",context).equals("false"))) {
      useTapirLink = false;
    }
    return useTapirLink;
  }

  public static boolean showMeasurements(String context) {
    return showCategory("showMeasurements",context);
  }

  public static boolean showMetalTags(String context) {
    return showCategory("showMetalTags",context);
  }

  public static boolean showAcousticTag(String context) {
    return showCategory("showAcousticTag",context);
  }

  public static boolean showSatelliteTag(String context) {
    return showCategory("showSatelliteTag",context);
  }

  public static boolean showReleaseDate(String context) {
    return showCategory("showReleaseDate",context);
  }

  public static String appendEmailRemoveHashString(HttpServletRequest request, String
                                                   originalString, String emailAddress, String context) {
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

  public static List<String> getIndexedValues(String baseKey, String context) {
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

  public static Integer getIndexNumberForValue(String baseKey, String checkValue, String context){
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


  private static boolean showCategory(final String category, String context) {
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
  public static boolean showUsersToPublic(String context) {
    get(context);
    boolean showUsersToPublic = true;
    if ((getProperty("showUsersToPublic",context) != null) && (getProperty("showUsersToPublic",context).equals("false"))) {
      showUsersToPublic = false;
    }
    return showUsersToPublic;
  }


  public static boolean isIntegratedWithWildMe(String context){

    get(context);
    boolean integrated = true;
    if ((getProperty("isIntegratedWithWildMe",context) != null) && (getProperty("isIntegratedWithWildMe",context).equals("false"))) {
      integrated = false;
    }
    return integrated;
  }


}
