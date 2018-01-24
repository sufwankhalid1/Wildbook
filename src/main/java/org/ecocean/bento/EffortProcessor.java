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
import java.text.ParseException;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import java.io.File;

import java.util.Iterator;
import java.util.Map;
import org.ecocean.Measurement;
import org.ecocean.Shepherd;

import com.opencsv.*;
import org.ecocean.Survey;



public class EffortProcessor extends BentoProcessor {
    
    private static final String[] REQUIRED_COLUMNS_LIST = {"Date Created","Date Modified","Vessel", "Project", "OnEffort", "OffEffort"};
    public static final HashSet<String> REQUIRED_COLUMNS = new HashSet<>(Arrays.asList(REQUIRED_COLUMNS_LIST));

    public ArrayList<Survey> getSurveysFromFile(File file, Shepherd myShepherd) {

        CSVReader reader = getCSVReader(file);

        //There may be multiple surveys logged.
        Iterator<String[]> rows = reader.iterator();
        String[] columnNameArr = rows.next();
        boolean isValid = checkRequiredColumns(columnNameArr, REQUIRED_COLUMNS);

        if (isValid) {
            ArrayList<Survey> svs = new ArrayList<>();
            // Row zero for column names.
            while (rows.hasNext()) {
                System.out.println("=========== Making a new survey from bento!");
                Survey sv = null;
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
    
    private Survey processColumns(String[] columnNameArr, String[] row ) {

        ArrayList<String> columns = new ArrayList<>(Arrays.asList(columnNameArr));
        HashMap<String,String> obPairs = new HashMap<>();

        Survey sv = null;
        int count = 0;
        for (String column : columns) {
            boolean used = false;
            String name = column.trim().toLowerCase();
            String value = row[count];
            System.out.println("?????? ColumnValue: "+value+" ??????");

            if ("date created".equals(name)) {
                String shortDate = processDate(value, "MMM d, yyyy, k:m","MM-dd-yyyy");
                sv = new Survey(shortDate);
                System.out.println("=============== Made a survey!!!!");
                used = true;
            } 
            
            if (sv==null) {
                return null;
            }

            try {
                if ("project".equals(name)) {
                    sv.setProjectName(value);
                    System.out.println("======= Set project.");
                    used = true;
                }
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }

            try {
                if ("oneffort".equals(name)) {
                    Measurement effort = new Measurement(sv.getID(), "Effort", Double.valueOf(value), "HH:mm", "Measured");
                    sv.setEffort(effort);
                    System.out.println("======= Set effort measurement: "+value);
                    used = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (name.contains("comments")) {
                    sv.addComments(value);
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
            processRemainingColumnsAsObservations(sv, obPairs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sv;
    }
}