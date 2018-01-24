package org.ecocean.bento;

//import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Collection;
import java.util.Enumeration;
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
import org.ecocean.Survey;
import org.ecocean.Observation;



public class SurveyLogProcessor extends BentoProcessor {
    
    private static final String[] REQUIRED_COLUMNS_LIST = {"Date Created","Date Modified"};
    public static final HashSet<String> REQUIRED_COLUMNS = new HashSet<>(Arrays.asList(REQUIRED_COLUMNS_LIST));

    public ArrayList<Observation> getLogEntriesFromFile(File file, Shepherd myShepherd) {

        CSVReader reader = getCSVReader(file);
        //These are seperate log entries for a single Survey. 
        Iterator<String[]> rows = reader.iterator();
        //Grab the first row for column headers.
        String[] columnNameArr = rows.next();
        System.out.println("Column Names? "+Arrays.toString(columnNameArr));
        boolean isValid = checkRequiredColumns(columnNameArr, REQUIRED_COLUMNS);

        if (isValid) {
            ArrayList<Observation> svs = new ArrayList<>();
            // Row zero for column names.
            int count = 0;
            while (rows.hasNext()) {
                count++;
                System.out.println("=========== Making occurrence #"+count+" from bento!");
                Observation ob = null;
                String[] dataRow = rows.next();
                ob = processColumns(columnNameArr, dataRow);
                if (ob!=null) {
                    svs.add(ob);
                }
            }
            if (!svs.isEmpty()) {
                return svs;
            }
        }
        return null;
    }

    private Observation processColumns(String[] columnNameArr, String[] row ) {

        ArrayList<String> columns = new ArrayList<>(Arrays.asList(columnNameArr));
        HashMap<String,String> obPairs = new HashMap<>();    
        String obValue = "";
        String time = "";
        
        int count = 0;
        for (String column : columns) {
            boolean used = false;
            String name = column.trim().toLowerCase();
            String value = row[count];
            System.out.println("?????? ColumnValue: "+value+" ??????");
            
            if ("date created".equals(name)) {
                String shortDate = processDate(value, "MMM d, yyyy, k:m","MM-dd-yyyy");
                time = processTime(value, "MMM d, yyyy, k:m","HH:mm");        
                used = true;
            } 
        }
        Observation ob = new Observation("Log Entry "+time,obValue, );


        return ob;
    }


}