package org.ecocean.bento;

//import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.UUID;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.TimeZone;

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

    public boolean checkRequiredColumns(String[] headerRow, HashSet<String> reqColumns) {        
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

}