package org.ecocean.bento;

//import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Iterator;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.ecocean.Shepherd;
import org.ecocean.Util;

import com.opencsv.*;

import org.ecocean.movement.*;
import org.ecocean.Occurrence;



public class SightingsProcessor extends BentoProcessor {
    
    private static final String[] REQUIRED_COLUMNS_LIST = {"Date Created","Date Modified","SurveyArea", "SightingNo.", "Species"};
    public static final HashSet<String> REQUIRED_COLUMNS = new HashSet<>(Arrays.asList(REQUIRED_COLUMNS_LIST));

    public ArrayList<Occurrence> getOccurrencesFromFile(File file, Shepherd myShepherd) {

        CSVReader reader = getCSVReader(file);
        //These are seperate log entries for a single Survey. 
        Iterator<String[]> rows = reader.iterator();
        //Grab the first row for column headers.
        String[] columnNameArr = rows.next();
        System.out.println("Column Names? "+Arrays.toString(columnNameArr));
        boolean isValid = checkRequiredColumns(columnNameArr, REQUIRED_COLUMNS);

        if (isValid) {
            ArrayList<Occurrence> svs = new ArrayList<>();
            // Row zero for column names.
            int count = 0;
            while (rows.hasNext()) {
                count++;
                System.out.println("=========== Making occurrence #"+count+" from bento!");
                Occurrence sv = null;
                String[] dataRow = rows.next();
                sv = processColumns(columnNameArr, dataRow);
                if (sv!=null) {
                    svs.add(sv);
                }
            }
            if (!svs.isEmpty()) {
                return svs;
            }
        }
        return null;
    }

    private Occurrence processColumns(String[] columnNameArr, String[] row ) {

        ArrayList<String> columns = new ArrayList<>(Arrays.asList(columnNameArr));
        HashMap<String,String> obPairs = new HashMap<>();

        Occurrence occ = null;
        int count = 0;
        for (String column : columns) {
            boolean used = false;
            String name = column.trim().toLowerCase();
            String value = row[count];
            System.out.println("?????? ColumnValue: "+value+" ??????");

            if ("date created".equals(name)) {
                //String shortDate = processDate(value, "MMM d, yyyy, k:m","MM-dd-yyyy");
                occ = new Occurrence();
                occ.setPrimaryKeyID(Util.generateUUID());
                System.out.println("=============== Success making a survey!!!!");
                used = true;
            } 
            
            if (occ==null) {
                return null;
            }

            try {
                if ("project".equals(name)) {
                    //occ.setProjectName(value);
                    System.out.println("======= Set project.");
                    used = true;
                }
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }

            try {
                if ("oneffort".equals(name)) {
                    //Measurement effort = new Measurement(sv.getID(), "Effort", Double.valueOf(value), "HH:mm", "Measured");
                    //sv.setEffort(effort);
                    System.out.println("======= Set effort measurement: "+value);
                    used = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (name.contains("comments")) {
                    //sv.addComments(value);
                    System.out.println("======= Set comments: "+value);
                    used = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            if (!used) {
                obPairs.put(name, value);
            }

            count ++;
        }
        try {
            processRemainingColumnsAsObservations(occ, obPairs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return occ;
    }



    public int getNumEncounters() {
        int num = 0;


        return num;
    }


}