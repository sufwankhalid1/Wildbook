package org.ecocean.bento;

//import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.UUID;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.TimeZone;

import java.util.MissingResourceException;
import java.text.ParseException;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;


//EXIF-related imports
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.ecocean.media.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.DataFormatter;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.io.IOUtils;

import org.ecocean.Shepherd;
import org.ecocean.Encounter;
import org.ecocean.genetics.TissueSample;
import org.ecocean.Observation;
import org.ecocean.Occurrence;
import org.ecocean.Survey;

import com.opencsv.*;


public abstract class BentoProcessor {

    // Put all the date crunching, field sanitizing, content cleaning helper stuff here.

    protected CSVReader getCSVReader(File file) {
        CSVReader reader = null;
        try {
          reader = new CSVReader(new FileReader(file));
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        }
        return reader;
    }

    protected boolean checkRequiredColumns(String[] headerRow, HashSet<String> reqColumns) {        
        int found = 0;
        for (String header : headerRow) {
            if (reqColumns.contains(header)) {
                found++;
            }
        }
        if (found==reqColumns.size()) {
            return true;
        }
        return false;
    }

    protected String processDate(String rawDate, String inputFormat, String outputFormat) {
        try  {
            DateTimeFormatter in = DateTimeFormat.forPattern(inputFormat); 
            DateTimeFormatter out = DateTimeFormat.forPattern(outputFormat);  
    
            DateTime dt = in.parseDateTime(rawDate); 
            String date = out.print(dt.getMillis());
            return date;

        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        }
        return null;
    }

    //Maybe not need. It's a bit gross and hardcoded. Does the job (most of the time) if people can't be consistent though. 
    protected String formatDate(String rawDate) {
        String date = null;
        DateTime dt = null;
        //out.println("Raw Date Created : "+rawDate);
        if (rawDate!=null&&rawDate.length()>16) {
          try {
            if (rawDate.endsWith("AM")||(rawDate.endsWith("PM"))){
              dt = dateStringToDateTime(rawDate,"MMM d, yyyy, h:m a");
            } else if (String.valueOf(rawDate.charAt(3)).equals(" ")&&rawDate.contains(",")) {
              dt = dateStringToDateTime(rawDate,"MMM dd, yyyy, h:m");          
            } else if (String.valueOf(rawDate.charAt(4)).equals("-")) {
              dt = dateStringToDateTime(rawDate,"yyyy-MM-dd'T'kk:mm:ss"); 
            }
            date = dt.toString().substring(0,10);        
          } catch (Exception e) {
          }
        } 
        return date;
      }  

      protected DateTime dateStringToDateTime(String verbatim, String format) {
        DateFormat fm = new SimpleDateFormat(format);
        Date d = null;
        try {
          d = (Date)fm.parse(verbatim);    
        } catch (ParseException pe) {
          pe.printStackTrace();
        }
        DateTime dt = new DateTime(d);
        return dt;
      }

      private void processRemainingColumnsAsObservations(Object obj, ArrayList<String> names, ArrayList<String> values) {

        Encounter enc = null;
        Occurrence occ = null;
        TissueSample ts = null;
        Survey sv = null;
        String id = null;
        if (obj.getClass().getSimpleName().equals("Encounter")) {
          enc = (Encounter) obj;
          id = ((Encounter) obj).getPrimaryKeyID();
        } 
        if (obj.getClass().getSimpleName().equals("Occurrence")) {
          occ = (Occurrence) obj;
          id = ((Occurrence) obj).getPrimaryKeyID();
        }
        if (obj.getClass().getSimpleName().equals("TissueSample")) {
          ts = (TissueSample) obj;
          id = ((TissueSample) obj).getSampleID();
        }
        if (obj.getClass().getSimpleName().equals("Survey")) {
          sv = (Survey) obj;
          id = ((Survey) obj).getID();
        }
        
        ArrayList<Observation> newObs = new ArrayList<Observation>();
        int count = 0;
        for (String name : names) {
          String value = null;
          try {
            if (values.get(count)!=null) {
              value = values.get(column.trim()).toString().trim();
              if (value.length() > 0) {
                Observation ob = new Observation(column.toString(), value, obj, id);
                newObs.add(ob);           
              }
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
          count++;
        }
        if (newObs.size() > 0) {
          try {
            if (enc != null) {
              enc.addBaseObservationArrayList(newObs);
            }
            if (occ != null) {
              occ.addBaseObservationArrayList(newObs); 
            }
            if (ts != null) {
              ts.addBaseObservationArrayList(newObs); 
            }
            if (sv != null) {
              sv.addBaseObservationArrayList(newObs); 
            }
          } catch (Exception e) {
            e.printStackTrace();
          }        
        }
      }

}