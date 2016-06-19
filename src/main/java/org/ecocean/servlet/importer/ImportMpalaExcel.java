package org.ecocean.servlet.importer;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import java.net.*;
import org.ecocean.grid.*;
import java.io.*;
import java.util.*;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import org.ecocean.*;
import org.ecocean.servlet.*;
import org.ecocean.media.*;
import javax.jdo.*;
import java.lang.StringBuffer;
import java.util.Vector;
import java.util.Iterator;
import java.lang.NumberFormatException;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;


import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class
ImportMpalaExcel extends HttpServlet {

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  public void doGet(HttpServletRequest request,  HttpServletResponse response) throws ServletException,  IOException {
    doPost(request,  response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,  IOException {

    String context="context0";
    context=ServletUtilities.getContext(request);
    Shepherd myShepherd = new Shepherd(context);
    PrintWriter out = response.getWriter();

    String filename = "/home/ubuntu/documents/Wildbook/mpala_data_sample.xls";
    if (request.getParameter("filename") != null) filename = request.getParameter("filename");
    File dataFile = new File(filename);
    boolean dataFound = dataFile.exists();

    out.println("</br><p>File found="+String.valueOf(dataFound)+" at "+dataFile.getAbsolutePath()+"</p>");
    FileInputStream dataFIStream = new FileInputStream(dataFile);
    //Create Workbook instance holding reference to .xlsx file
    //XSSFWorkbook workbook = new XSSFWorkbook(dataFIStream);

    POIFSFileSystem fs = new POIFSFileSystem(dataFIStream);
    HSSFWorkbook wb = new HSSFWorkbook(fs);
    HSSFSheet sheet = wb.getSheetAt(0);
    HSSFRow row;
    HSSFCell cell;

    int numSheets = wb.getNumberOfSheets();
    out.println("<p>Num Sheets = "+numSheets+"</p>");

    int physicalNumberOfRows = sheet.getPhysicalNumberOfRows();
    out.println("<p>Num Rows = "+physicalNumberOfRows+"</p>");

    int rows = sheet.getPhysicalNumberOfRows();; // No of rows
    int cols = sheet.getRow(0).getPhysicalNumberOfCells(); // No of columns
    int tmp = 0;
    out.println("<p>Num Cols = "+cols+"</p>");

    Occurrence occ = null;
    myShepherd.beginDBTransaction();
    out.println("<h2>BEGINNING THE EXCEL LOOP</h2>");
    for (int i=1; i<rows; i++) {
      try {
        myShepherd.beginDBTransaction();
        row = sheet.getRow(i);
        String rowOccID = getInteger(row, 2).toString();

        occ = getCurrentOccurrence(rowOccID, occ, myShepherd, row);
        if (!myShepherd.isOccurrence(occ.getOccurrenceID())) myShepherd.storeNewOccurrence(occ);
        Encounter enc = parseEncounter(occ, row);
        String indID = enc.getIndividualID();
        MarkedIndividual ind = myShepherd.getMarkedIndividual(indID);
        boolean needToAddEncToInd = false;
        if (ind==null) {
          ind = new MarkedIndividual(indID,enc);
        } else {
          needToAddEncToInd = true;
        }
        myShepherd.storeNewEncounter(enc, Util.generateUUID());
        occ.addEncounter(enc);
        if (needToAddEncToInd) ind.addEncounter(enc, context);
        if (!myShepherd.isMarkedIndividual(indID)) myShepherd.storeNewMarkedIndividual(ind);
        // TODO: why is it not persisted?? could fix with a fixSomeFields
        myShepherd.commitDBTransaction();
        out.println("<p>Parsed row ("+i+"), containing Occ "+occ.getOccurrenceID()+" and Enc "+enc.getEncounterNumber()+"</p>");
      }
      catch (Exception e) {
        out.println("Encountered an error while importing the file.");
        e.printStackTrace(out);
        myShepherd.rollbackDBTransaction();
      }
    }
  }

  public Occurrence getCurrentOccurrence(String id, Occurrence oldOcc, Shepherd myShepherd, HSSFRow row) {
    if (needToParse(id, myShepherd)) return parseOccurrence(row);
    if (oldOcc!=null && oldOcc.getOccurrenceID()==id) return oldOcc;
    return myShepherd.getOccurrence(id);
  }

  private boolean needToParse(String rowOccID, Shepherd myShepherd) {
    return (!myShepherd.isOccurrence(rowOccID));
  }

  public Occurrence parseOccurrence(HSSFRow row) {

    String id = getInteger(row, 2).toString();
    if (id==null || id.equals("")) return null;

    Occurrence occ = new Occurrence(id);
    occ.setDateDay(getInteger(row, 4));
    occ.setDateMonth(getInteger(row, 5));
    occ.setDateYear(getInteger(row,6));
    occ.setDateTime(getDateTime(row));
    occ.setGroupHabitatActivityTableRemark(getString(row, 8));
    occ.setZebraSpecies(getString(row, 9));
    occ.setRanch(getString(row, 10));
    occ.setLocalName(getString(row, 11));
    occ.setStartGpsX(getInteger(row, 12));
    occ.setStartGpsY(getInteger(row, 13));
    occ.setDistanceToGroupCentre(getInteger(row, 14));
    occ.setDirectionToGroupCentre(getInteger(row, 15));
    occ.setGroupSpread(getString(row, 16));
    occ.setTotalIndividualsCounted(getBooleanFromString(row,17));
    occ.setAllMaleId(getBooleanFromString(row, 18));
    occ.setAllIndId(getBooleanFromString(row, 19));
    occ.setAllAgeStructureOp(getBooleanFromString(row, 20));
    occ.setMonth(getString(row,21));
    occ.setSeason(getString(row,22));
    occ.setInfs01female(getInteger(row,23));
    occ.setInfs03female(getInteger(row,24));
    occ.setInfs13female(getInteger(row,25));
    occ.setInfs36female(getInteger(row,26));
    occ.setInfs612female(getInteger(row,27));
    occ.setInfs01male(getInteger(row,28));
    occ.setInfs03male(getInteger(row,29));
    occ.setInfs13male(getInteger(row,30));
    occ.setInfs36male(getInteger(row,31));
    occ.setInfs612Male(getInteger(row,32));
    occ.setInfs01sexukn(getInteger(row,33));
    occ.setInfs03sexukn(getInteger(row,34));
    occ.setInfs13sexukn(getInteger(row,35));
    occ.setInfs36sexukn(getInteger(row,36));
    occ.setInfs612sexukn(getInteger(row,37));
    occ.setYearlingFemale(getInteger(row,38));
    occ.setYearlingMale(getInteger(row,39));
    occ.setYearlingSexukn(getInteger(row,40));
    occ.setTwoYearFemale(getInteger(row,41));
    occ.setTwoYearMale(getInteger(row,42));
    occ.setTwoYearSexukn(getInteger(row,43));
    occ.setThreeYearMale(getInteger(row,44));
    occ.setThreeYearFemale(getInteger(row,45));
    occ.setThreeYearSexukn(getInteger(row,46));
    occ.setAdFemaleReprostatusukn(getInteger(row,47));
    occ.setAdFemalePreg(getInteger(row,48));
    occ.setAdFemaleLact(getInteger(row,49));
    occ.setAdFemaleNonlact(getInteger(row,50));
    occ.setAdultMaleStallion(getInteger(row,51));
    occ.setAdultMaleBachelor(getInteger(row,52));
    occ.setYearlingMaleBachelor(getInteger(row,53));
    occ.setTwoYearOldMaleBachelor(getInteger(row,54));
    occ.setAdultMaleStatusUkn(getInteger(row,55));
    occ.setTerritorialMale(getInteger(row,56));
    occ.setAdultSexUkn(getInteger(row,57));
    occ.setAgeSexUkn(getInteger(row,58));
    occ.setInfs03HybridFemale(getInteger(row,59));
    occ.setInfs03HybridMale(getInteger(row,60));
    occ.setInfs03HybridUkn(getInteger(row,61));
    occ.setInfs36HybridFemale(getInteger(row,62));
    occ.setInfs36HybridMale(getInteger(row,63));
    occ.setInfs36HybridUkn(getInteger(row,64));
    occ.setInfs612HybridFemale(getInteger(row,65));
    occ.setInfs612HybridMale(getInteger(row,66));
    occ.setInfs612HybridUkn(getInteger(row,67));
    occ.setYearlingHybridFemale(getInteger(row,68));
    occ.setYearlingHybridMale(getInteger(row,69));
    occ.setYearlingHybridUkn(getInteger(row,70));
    occ.setTwoYearHybridFemale(getInteger(row,71));
    occ.setTwoYearHybridMale(getInteger(row,72));
    occ.setTwoYearHybridUkn(getInteger(row,73));
    occ.setAdFemaleHybridReproStatusUkn(getInteger(row,74));
    occ.setAdFemaleHybridPreg(getInteger(row,75));
    occ.setAdFemaleHybridLact(getInteger(row,76));
    occ.setAdFemaleHybridNonLact(getInteger(row,77));
    occ.setAdultMaleHybridStallion(getInteger(row,78));
    occ.setAdultMaleHybridBachelor(getInteger(row,79));
    occ.setYearlingMaleHybridBachelor(getInteger(row,80));
    occ.setTwoYearOldMaleHybridBachelor(getInteger(row,81));
    occ.setAdultMaleHybridStatusUkn(getInteger(row,82));
    occ.setAdultHybridSexUkn(getInteger(row,83));
    occ.setHybridAgeAndSexUnk(getInteger(row,84));
    occ.setTotalIndividualsCalculated(getInteger(row,85));
    occ.setTotalIndividuals(getInteger(row,86));
    occ.setN1_OtherSpecies(getString(row,87));
    occ.setNumber1stSp(getInteger(row,88));
    occ.setN2_OtherSpecies(getString(row,89));
    occ.setNumber2ndSp(getInteger(row,90));
    occ.setN3_OtherSpecies(getString(row,91));
    occ.setNumber3rdSp(getInteger(row,92));
    occ.setSun(getString(row,93));
    occ.setWind(getString(row,94));
    occ.setSoil(getString(row,95));
    occ.setRain(getString(row,96));
    occ.setCloudPercentage(getInteger(row,97));
    occ.setHabitatObscurityBitNumber(getInteger(row,98));
    occ.setHabitatObscurityCategory(getString(row,99));
    occ.setDominantBushType(getString(row,100));
    occ.setGrassColor(getString(row,101));
    occ.setGrassHeight(getString(row,102));
    occ.setDominantGrassSpecies1(getString(row,103));
    occ.setDominantGrassSpecies2(getString(row,104));
    occ.setDominantGrassSpecies3(getString(row,105));
    occ.setOnRoad(getString(row,106));
    occ.setUnusualEnvironment(getString(row,107));
    occ.setNumberGrazing(getInteger(row,108));
    occ.setNumberVigilant(getInteger(row,109));
    occ.setNumberStanding(getInteger(row,110));
    occ.setNumberWalking(getInteger(row,111));
    occ.setNumberSocialising(getInteger(row,112));
    occ.setNumberAgonism(getInteger(row,113));
    occ.setNumberHealthMaintenance(getInteger(row,114));
    occ.setNumberSexualBeh(getInteger(row,115));
    occ.setNumberPlay(getInteger(row,116));
    occ.setNumberNurseSuckle(getInteger(row,117));
    occ.setNumberLying(getInteger(row,118));
    occ.setNumberSalting(getInteger(row,119));
    occ.setNumberMutualGrooming(getInteger(row,120));
    occ.setNumberRunning(getInteger(row,121));
    occ.setNumberBehaviorNotVisible(getInteger(row,122));
    occ.setNumberDrinking(getInteger(row,123));
    occ.setDirectionOfWalking(getString(row,124));
    occ.setTotalIndividualsActivity(getInteger(row,125));
    occ.setLoopNumber(getInteger(row,126));
    return occ;
  }

  public Encounter parseEncounter(Occurrence occ, HSSFRow row) {
    String id = getString(row, 127);
    Encounter enc = new Encounter(occ, id);
    enc.setAgeClass(getString(row,128));
    enc.setReproductiveStatus(getString(row,129));
    enc.setInjury(getString(row,130));
    enc.setWoundType(getString(row,131));
    enc.setIndividualSightingsRemark(getString(row,132));
    enc.setNearestNeighbourId(getString(row,133));
    enc.setNearestNeighbourIdSecond(getString(row,134));
    enc.setOrder(getInteger(row,135));
    enc.setHeading(getInteger(row,136));
    enc.setStallionId(getString(row,137));
    enc.setMother(getString(row,138));
    enc.setHaremNumber(getInteger(row,139));
    enc.setLactatingFemale(getBooleanFromString(row,140));
    enc.setPregnantFemale(getBooleanFromString(row,141));
    enc.setIs03Foal(getBooleanFromString(row,142));
    enc.setIs36Foal(getBooleanFromString(row,143));
    enc.setIs612Foal(getBooleanFromString(row,144));
    enc.setNumberOfFoals(getInteger(row,145));
    enc.setLionWound(getString(row,146));
    return enc;
  }

  // following 'get' functions swallow errors
  public Integer getInteger(HSSFRow row, int i) {
    try {
      double val = row.getCell(i).getNumericCellValue();
      return new Integer( (int) val );
    }
    catch (Exception e){}
    return null;
  }

  public String getString(HSSFRow row, int i) {
    try {
      String str = row.getCell(i).getStringCellValue();
      if (str==null || str.equals("")) return null;
      else return str;
    }
    catch (Exception e) {}
    return null;
  }

  public Boolean getBooleanFromString(HSSFRow row, int i) {
    try {
      String boolStr = getString(row, i).trim().toLowerCase();
      if (boolStr==null || boolStr.equals("")) return null;
      else if (boolStr.equals("yes")) return new Boolean(true);
      else if (boolStr.equals("no")) return new Boolean(false);
    }
    catch (Exception e) {}
    return null;
  }

  public DateTime getDateTime(HSSFRow row) {
    int year = 0;
    int month = 0;
    int day = 0;
    int hour = 0;
    int minute = 0;
    int second = 0;
    try {
      Date dateWithTimeOnly = row.getCell(7).getDateCellValue();
      hour = dateWithTimeOnly.getHours();
      minute = dateWithTimeOnly.getMinutes();
      second = dateWithTimeOnly.getSeconds();
    }
    catch (Exception e) {}
    /*try {
      Date dateWithDateOnly = row.getCell(3).getDateCellValue();
      year = dateWithDateOnly.getYear();
      month = dateWithDateOnly.getMonth();
      day = dateWithDateOnly.getDay();
    }
    catch (Exception e) {
    */
      try {
        year = getInteger(row, 6).intValue();
        month = getInteger(row, 5).intValue();
        day = getInteger(row, 4).intValue();
    }
      catch (Exception ex) {}  /*
    }*/
    return new DateTime(year, month, day, hour, minute, second, DateTimeZone.forID("Africa/Nairobi"));

  }
}
