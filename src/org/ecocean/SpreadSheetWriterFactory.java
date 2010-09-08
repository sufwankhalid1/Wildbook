package org.ecocean;

import javax.jdo.*;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Vector;
import java.util.ArrayList;
//import jxl.*;
//import jxl.write.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.*;
import org.ecocean.*;
//import javax.jdo.*;
//import java.lang.StringBuffer;
import java.lang.Integer;
//import java.lang.NumberFormatException;
import java.io.*;
import java.util.Vector;
//import java.util.Iterator;
//import java.util.StringTokenizer;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;


public class SpreadSheetWriterFactory {
  
private static SpreadSheetWriter pmf;
  
  public synchronized static SpreadSheetWriter getSpreadSheetWriter(File fileExport, HttpServletRequest request, Properties props) {
      if(pmf!=null){return pmf;}
      else{
        pmf=new SpreadSheetWriter(fileExport, request, props);
        return pmf;
      }
  }
  
}
