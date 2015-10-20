/*
 * Wildbook - A Mark-Recapture Framework
 * Copyright (C) 2011-2014 Jason Holmberg
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

package org.ecocean.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor;

import javax.jdo.Query;
import javax.servlet.http.HttpServletRequest;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.ecocean.CommonConfiguration;
import org.ecocean.Encounter;
import org.ecocean.MarkedIndividual;
import org.ecocean.Occurrence;
import org.ecocean.Shepherd;
import org.ecocean.email.old.MailThreadExecutorService;
import org.ecocean.email.old.NotificationMailer;
import org.ecocean.email.old.NotificationMailerHelper;
import org.ecocean.mmutil.StringUtilities;
import org.ecocean.util.FileUtilities;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndCategoryImpl;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.io.XmlReader;

//ATOM feed


public class ServletUtilities {
    public static Logger logger = LoggerFactory.getLogger(ServletUtilities.class);

  public static String getHeader(final HttpServletRequest request) {
    try {
      FileReader fileReader = new FileReader(FileUtilities.findResourceOnFileSystem("servletResponseTemplate.htm"));
      BufferedReader buffread = new BufferedReader(fileReader);
      String templateFile = "", line;
      StringBuffer SBreader = new StringBuffer();
      while ((line = buffread.readLine()) != null) {
        SBreader.append(line).append("\n");
      }
      fileReader.close();
      buffread.close();
      templateFile = SBreader.toString();

      String context=getContext(request);

      //process the CSS string
      templateFile = templateFile.replaceAll("CSSURL", CommonConfiguration.getCSSURLLocation(request,context));

      //set the top header graphic
      templateFile = templateFile.replaceAll("TOPGRAPHIC", CommonConfiguration.getURLToMastheadGraphic(request, context));

      int end_header = templateFile.indexOf("INSERT_HERE");
      return (templateFile.substring(0, end_header));
    }
    catch (Exception e) {
      //out.println("I couldn't find the template file to read from.");
      e.printStackTrace();
      String error = "<html><body><p>An error occurred while attempting to read from the template file servletResponseTemplate.htm. This probably will not affect the success of the operation you were trying to perform.";
      return error;
    }
  }

  public static String getFooter(final String context) {
    try {
      FileReader fileReader = new FileReader(FileUtilities.findResourceOnFileSystem("servletResponseTemplate.htm"));
      BufferedReader buffread = new BufferedReader(fileReader);
      String templateFile = "", line;
      StringBuffer SBreader = new StringBuffer();
      while ((line = buffread.readLine()) != null) {
        SBreader.append(line).append("\n");
      }
      fileReader.close();
      buffread.close();
      templateFile = SBreader.toString();
      templateFile = templateFile.replaceAll("BOTTOMGRAPHIC", CommonConfiguration.getURLToFooterGraphic(context));

      int end_header = templateFile.indexOf("INSERT_HERE");
      return (templateFile.substring(end_header + 11));
    } catch (Exception e) {
      //out.println("I couldn't find the template file to read from.");
      e.printStackTrace();
      String error = "An error occurred while attempting to read from an HTML template file. This probably will not affect the success of the operation you were trying to perform.</p></body></html>";
      return error;
    }


  }

  /**
   * Inform (via email) researchers who've logged an interest in encounter.
   * @param request servlet request
   * @param encounterNumber ID of encounter to inform about
   * @param message message to include in email notification
   * @param context webapp context
   */
  public static void informInterestedParties(final HttpServletRequest request, final String encounterNumber, final String message, final String context) {
    Shepherd shep = new Shepherd(context);
    shep.beginDBTransaction();
    if (shep.isEncounter(encounterNumber)) {
      Encounter enc = shep.getEncounter(encounterNumber);
      if(enc.getInterestedResearchers() != null){
        Collection<String> notifyMe = enc.getInterestedResearchers();
        if (!notifyMe.isEmpty()) {
          ThreadPoolExecutor es = MailThreadExecutorService.getExecutorService();
          for (String mailTo : notifyMe) {
            Map<String, String> tagMap = NotificationMailerHelper.createBasicTagMap(request, enc);
            tagMap.put(NotificationMailer.EMAIL_NOTRACK, "number=" + encounterNumber);
            tagMap.put(NotificationMailer.EMAIL_HASH_TAG, StringUtilities.getHashOf(mailTo));
            tagMap.put(NotificationMailer.STANDARD_CONTENT_TAG, message == null ? "" : message);
//            String langCode = ServletUtilities.getLanguageCode(request);
            NotificationMailer mailer = new NotificationMailer(context, null, mailTo, "encounterDataUpdate", tagMap);
            es.execute(mailer);
          }
          es.shutdown();
        }
      }
    }
    shep.rollbackDBTransaction();
    shep.closeDBTransaction();
  }

  /**
   * Inform (via email) researchers who've logged an interest in individual.
   * @param request servlet request
   * @param individualID ID of individual to inform about
   * @param message message to include in email notification
   * @param context webapp context
   */
  public static void informInterestedIndividualParties(final HttpServletRequest request, final String individualID, final String message, final String context) {
    Shepherd shep = new Shepherd(context);
    shep.beginDBTransaction();
    if (shep.isMarkedIndividual(individualID)) {
      MarkedIndividual ind = shep.getMarkedIndividual(individualID);
      if (ind.getInterestedResearchers() != null) {
        Collection<String> notifyMe = ind.getInterestedResearchers();
        if (!notifyMe.isEmpty()) {
          ThreadPoolExecutor es = MailThreadExecutorService.getExecutorService();
          for (String mailTo : notifyMe) {
            Map<String, String> tagMap = NotificationMailerHelper.createBasicTagMap(request, ind);
            tagMap.put(NotificationMailer.EMAIL_NOTRACK, "individual=" + individualID);
            tagMap.put(NotificationMailer.EMAIL_HASH_TAG, StringUtilities.getHashOf(mailTo));
            tagMap.put(NotificationMailer.STANDARD_CONTENT_TAG, message == null ? "" : message);
//            String langCode = ServletUtilities.getLanguageCode(request);
            NotificationMailer mailer = new NotificationMailer(context, null, mailTo, "individualDataUpdate", tagMap);
            es.execute(mailer);
          }
          es.shutdown();
        }
      }
    }
    shep.rollbackDBTransaction();
    shep.closeDBTransaction();
  }

//  public static String getConfigDir(final HttpServletRequest request) {
////      return request.getServletContext().getInitParameter("config.dir");
//      return Global.INST.getInitResources().getString("config.dir", null);
//  }

  //Loads a String of text from a specified file.
  //This is generally used to load an email template for automated emailing
  public static String getText(final String shepherdDataDir, final String fileName, final String langCode) {
    String overrideText=loadOverrideText(shepherdDataDir, fileName, langCode);
    if (overrideText != null) {
      return overrideText;
    }

    try {
        StringBuffer SBreader = new StringBuffer();
        File file = FileUtilities.findResourceOnFileSystem(fileName);

        try (FileReader fileReader = new FileReader(file)) {
            try (BufferedReader buffread = new BufferedReader(fileReader)) {
                String line;
                while ((line = buffread.readLine()) != null) {
                    SBreader.append(line + "\n");
                }
            }
        }

        return SBreader.toString();
    } catch (Exception ex) {
        ex.printStackTrace();
        return "";
  }
  }

  //Logs a new ATOM entry
  public static synchronized void addATOMEntry(final String title, final String link, final String description, final File atomFile, final String context) {
    try {

      if (atomFile.exists()) {

        //System.out.println("ATOM file found!");
        /** Namespace URI for content:encoded elements */
//        String CONTENT_NS = "http://www.w3.org/2005/Atom";

        /** Parses RSS or Atom to instantiate a SyndFeed. */
        SyndFeedInput input = new SyndFeedInput();

        /** Transforms SyndFeed to RSS or Atom XML. */
        SyndFeedOutput output = new SyndFeedOutput();

        // Load the feed, regardless of RSS or Atom type
        SyndFeed feed = input.build(new XmlReader(atomFile));

        // Set the output format of the feed
        feed.setFeedType("atom_1.0");

        @SuppressWarnings("unchecked")
        List<SyndEntry> items = feed.getEntries();
        int numItems = items.size();
        if (numItems > 9) {
          items.remove(0);
          feed.setEntries(items);
        }

        SyndEntry newItem = new SyndEntryImpl();
        newItem.setTitle(title);
        newItem.setLink(link);
        newItem.setUri(link);
        SyndContent desc = new SyndContentImpl();
        desc.setType("text/html");
        desc.setValue(description);
        newItem.setDescription(desc);
        desc.setType("text/html");
        newItem.setPublishedDate(new java.util.Date());

        List<SyndCategory> categories = new ArrayList<SyndCategory>();
        if(CommonConfiguration.getProperty("htmlTitle",context)!=null){
            SyndCategory category2 = new SyndCategoryImpl();
            category2.setName(CommonConfiguration.getProperty("htmlTitle",context));
            categories.add(category2);
        }
        newItem.setCategories(categories);
        if(CommonConfiguration.getProperty("htmlAuthor",context)!=null){
            newItem.setAuthor(CommonConfiguration.getProperty("htmlAuthor",context));
        }
        items.add(newItem);
        feed.setEntries(items);

        feed.setPublishedDate(new java.util.Date());


        FileWriter writer = new FileWriter(atomFile);
        output.output(feed, writer);
        writer.toString();

      }
    } catch (IOException ioe) {
          System.out.println("ERROR: Could not find the ATOM file.");
          ioe.printStackTrace();
    } catch (Exception e) {
          System.out.println("Unknown exception trying to add an entry to the ATOM file.");
          e.printStackTrace();
    }

  }

  //Logs a new entry in the library RSS file
  public static synchronized void addRSSEntry(final String title, final String link, final String description, final File rssFile) {
    //File rssFile=new File("nofile.xml");

    try {
        System.out.println("Looking for RSS file: "+rssFile.getCanonicalPath());
      if (rssFile.exists()) {

        SAXReader reader = new SAXReader();
        Document document = reader.read(rssFile);
        Element root = document.getRootElement();
        Element channel = root.element("channel");
        @SuppressWarnings("rawtypes")
        List items = channel.elements("item");
        int numItems = items.size();
        items = null;
        if (numItems > 9) {
          Element removeThisItem = channel.element("item");
          channel.remove(removeThisItem);
        }

        Element newItem = channel.addElement("item");
        Element newTitle = newItem.addElement("title");
        Element newLink = newItem.addElement("link");
        Element newDescription = newItem.addElement("description");
        newTitle.setText(title);
        newDescription.setText(description);
        newLink.setText(link);

        Element pubDate = channel.element("pubDate");
        pubDate.setText((new java.util.Date()).toString());

        //now save changes
        FileWriter mywriter = new FileWriter(rssFile);
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setLineSeparator(System.getProperty("line.separator"));
        XMLWriter writer = new XMLWriter(mywriter, format);
        writer.write(document);
        writer.close();

      }
    }
    catch (IOException ioe) {
          System.out.println("ERROR: Could not find the RSS file.");
          ioe.printStackTrace();
    }
    catch (DocumentException de) {
          System.out.println("ERROR: Could not read the RSS file.");
          de.printStackTrace();
    } catch (Exception e) {
          System.out.println("Unknown exception trying to add an entry to the RSS file.");
          e.printStackTrace();
    }
  }

  public static boolean isUserAuthorizedForEncounter(final Encounter enc, final HttpServletRequest request) {
    boolean isOwner = false;
    if (request.getUserPrincipal()!=null) {
      isOwner = true;
    }
    return isOwner;
  }

  public static boolean isUserAuthorizedForIndividual(final MarkedIndividual sharky, final HttpServletRequest request) {
    if (request.getUserPrincipal()!=null) {
      return true;
    }
    return false;
  }

  //occurrence
  public static boolean isUserAuthorizedForOccurrence(final Occurrence sharky, final HttpServletRequest request) {
    if (request.getUserPrincipal()!=null) {
      return true;
    }
    return false;
  }


  public static Query setRange(final Query query, final int iterTotal, final int highCount, final int lowCount) {

    if (iterTotal > 10) {

      //handle the normal situation first
      if ((lowCount > 0) && (lowCount <= highCount)) {
        if (highCount - lowCount > 50) {
          query.setRange((lowCount - 1), (lowCount + 50));
        } else {
          query.setRange(lowCount - 1, highCount);
        }
      } else {
        query.setRange(0, 10);
      }


    } else {
      query.setRange(0, iterTotal);
    }
    return query;

  }


  public static String cleanFileName(final String myString){
    return myString.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
  }

  /*public static String cleanFileName(String aTagFragment) {
    final StringBuffer result = new StringBuffer();

    final StringCharacterIterator iterator = new StringCharacterIterator(aTagFragment);
    char character = iterator.current();
    while (character != CharacterIterator.DONE) {
      if (character == '<') {
        result.append("_");
      } else if (character == '>') {
        result.append("_");
      } else if (character == '\"') {
        result.append("_");
      } else if (character == '\'') {
        result.append("_");
      } else if (character == '\\') {
        result.append("_");
      } else if (character == '&') {
        result.append("_");
      } else if (character == ' ') {
        result.append("_");
      } else if (character == '#') {
        result.append("_");
      } else {
        //the char is not a special one
        //add it to the result as is
        result.append(character);
      }
      character = iterator.next();
    }
    return result.toString();
  }
  */

  public static String preventCrossSiteScriptingAttacks(String description) {
    description = description.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    description = description.replaceAll("eval\\((.*)\\)", "");
    description = description.replaceAll("[\\\"\\\'][\\s]*((?i)javascript):(.*)[\\\"\\\']", "\"\"");
    description = description.replaceAll("((?i)script)", "");
    return description;
  }

  public static String getDate() {
    DateTime dt = new DateTime();
    DateTimeFormatter fmt = ISODateTimeFormat.date();
    return (fmt.print(dt));
  }

  public static Connection getConnection() throws SQLException {

    Connection conn = null;
    Properties connectionProps = new Properties();
    connectionProps.put("user", CommonConfiguration.getProperty("datanucleus.ConnectionUserName","context0"));
    connectionProps.put("password", CommonConfiguration.getProperty("datanucleus.ConnectionPassword","context0"));


    conn = DriverManager.getConnection(
           CommonConfiguration.getProperty("datanucleus.ConnectionURL","context0"),
           connectionProps);

    System.out.println("Connected to database for authentication.");
    return conn;
}

public static Shepherd getShepherd(final HttpServletRequest request) {
    return new Shepherd(getContext(request));
}

//
// This calls over to ServletUtils.getContext(), you should use that one.
//
@Deprecated
public static String getContext(final HttpServletRequest request) {
    return ServletUtils.getContext(request);
}

//
//This calls over to ServletUtils.getLanguageCode(), you should use that one.
//
@Deprecated
    public static String getLanguageCode(final HttpServletRequest request) {
        return ServletUtils.getLanguageCode(request);
    }


    public static File dataDir(final String context, final String rootWebappPath)
    {
        File webappsDir = new File(rootWebappPath).getParentFile();
        File shepherdDataDir = new File(webappsDir, CommonConfiguration.getDataDirectoryName(context));
        if (!shepherdDataDir.exists()) {
            shepherdDataDir.mkdirs();
        }
        return shepherdDataDir;
    }

    public static File dataDir(final String context, final String rootWebappPath, final String subdir) {
        return new File(dataDir(context, rootWebappPath), subdir);
    }


//    public static String getText2(final HttpServletRequest request,
//                                  final String fileName) throws IOException {
//        String configDir = getConfigDir(request);
//        String langCode = getLanguageCode(request);
//
//        File file = new File(configDir + "/text/" + langCode + "/" + fileName);
//
//        if (file.exists()) {
//            return OsUtils.readFileToString(file);
//        }
//
//        file = new File(configDir + "/text/" + DEFAULT_LANG_CODE + "/" + fileName);
//        if (file.exists()) {
//            return OsUtils.readFileToString(file);
//        }
//
//        throw new FileNotFoundException(file.getAbsolutePath());
//    }


  private static String loadOverrideText(final String shepherdDataDir, final String fileName, final String langCode) {
      if (logger.isDebugEnabled()) {
          logger.debug("Calling getText with shepherdDataDir [" + shepherdDataDir
                     + "], fileName [" + fileName
                     + "], langCode [" + langCode + "]");
      }

    File configDir = new File("webapps/"+shepherdDataDir+"/WEB-INF/classes/bundles/"+langCode);

    if (logger.isDebugEnabled()) {
        logger.debug("configDir [" + configDir.getAbsolutePath() + "]");
    }

    //
    //sometimes this ends up being the "bin" directory of the J2EE container
    //we need to fix that
    //
    if ((configDir.getAbsolutePath().contains("/bin/"))
         || (configDir.getAbsolutePath().contains("\\bin\\"))) {
      String fixedPath=configDir.getAbsolutePath().replaceAll("/bin", "").replaceAll("\\\\bin", "");

      configDir = new File(fixedPath);

      if (logger.isDebugEnabled()) {
          logger.debug("Fixed configDir to [" + configDir.getAbsolutePath() + "]");
      }
    }

    if (!configDir.exists()) {
        configDir.mkdirs();
    }

    File configFile = new File(configDir, fileName);

    if (logger.isDebugEnabled()) {
        logger.debug("Looking for overriding file [" + configFile.getAbsolutePath() + "]");
    }

    if (!configFile.exists()) {
        if (logger.isDebugEnabled()) {
            logger.debug("File does not exist");
        }
        return null;
    }

    StringBuffer myText = new StringBuffer("");

    FileInputStream fileInputStream = null;
    try {
        fileInputStream = new FileInputStream(configFile);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream))){
            String line;
            while ((line = reader.readLine()) != null) {
                myText.append(line);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    finally {
        if (fileInputStream != null) {
            try {
                fileInputStream.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    return myText.toString();
  }
}
