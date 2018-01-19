package org.ecocean.bento;

//import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
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



public class EffortProcessor extends BentoProcessor {
    
    private static final String[] REQUIRED_COLUMNS_LIST = {"Date Created","Date Modified","Vessel", "Project", "OnEffort", "OffEffort"};
    public static final HashSet<String> REQUIRED_COLUMNS = new HashSet<>(Arrays.asList(REQUIRED_COLUMNS_LIST));

    public Survey[] getSurveysFromFile(File file, Shepherd myShepherd) {

        CSVReader reader = getCSVReader(file);

        //These are seperate log entries for a single Survey. 
        Iterator<String[]> rows = reader.iterator();

        String[] columnNameArr = rows.next();
        boolean isValid = checkRequiredColumns(columnNameArr, REQUIRED_COLUMNS);

        if (isValid) {
            // Row zero for column names.
            while (rows.hasNext()) {
                Survey sv = null;
                String[] dataRow = rows.next();
                sv = processRow(columnNameArr, dataRow);
            }
        }

        return null;
    }
    
    private Survey processRow(String[] columnNameArr, String[] row ) {
        Survey sv = new Survey();
        
        for (int i=0;i<columnNameArr.length;i++) {
            String name = columnNameArr[i];
            String value = row[i];

            

        }
        return sv;
    }
}