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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jdo.Query;
import javax.servlet.ServletContext;

import org.ecocean.servlet.ServletUtilities;
import org.ecocean.util.LogBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;


/**
 * Does actual comparison processing of batch-uploaded images.
 *
 * @author Jon Van Oast
 */
public final class BatchCompareProcessor implements Runnable {
  public static final String SESSION_KEY_PROCESS = "ImportCSVProcess";
  public static final String SESSION_KEY_COMPARE = "ImportCSVCompare";
  private static Logger log = LoggerFactory.getLogger(BatchCompareProcessor.class);

  /** ServletContext for web application, to allow access to resources. */
  private final ServletContext servletContext;
  /** Username of person doing batch upload (for logging in comments). */

    private int countTotal = 0;
    private int countComplete = 0;

  /** Enumeration representing possible status values for the batch processor. */
  public enum Status { WAITING, INIT, RUNNING, FINISHED, ERROR };
  /** Current status of the batch processor. */
  private Status status = Status.WAITING;
  /** Throwable instance produced by the batch processor (if any). */
  private Throwable thrown;

    private String method = null;
    private List<String> args = null;
    private Map<String,List<String>> argsMap = null;
  private String context = "context0";
    private String batchID = null;

  public BatchCompareProcessor(ServletContext servletContext, String context, String method, List<String> args, String batchID) {
        this.servletContext = servletContext;
        this.context = context;
        this.args = args;
        this.method = method;
        this.batchID = batchID;
        if (log.isDebugEnabled()) {
            log.debug("in BatchCompareProcessor()");
        }
    }

            //Map<String,List<String>> fileMap = new HashMap<String,List<String>>();

    //variation using a hashmap for image->[ind,list]
  public BatchCompareProcessor(ServletContext servletContext, String context, String method, Map<String,List<String>> argsMap, String batchID) {
        this.servletContext = servletContext;
        this.context = context;
        this.argsMap = argsMap;
        this.method = method;
        this.batchID = batchID;
        if (log.isDebugEnabled()) {
            log.debug("in BatchCompareProcessor()");
        }
    }

    public int getCountTotal() {
        return this.countTotal;
    }
    public int getCountComplete() {
        return this.countComplete;
    }


//// this.args will contain encounter ids... this (pre)processes the images for those
    public void npmProcess() {
        String rootDir = servletContext.getRealPath("/");
        String baseDir = ServletUtilities.dataDir(context, rootDir).getAbsolutePath();
        List<String> imgs = new ArrayList<String>();
        if (log.isDebugEnabled()) {
            log.debug("start npmProcess()");
        }

        if (this.args != null) {
            imgs = this.args;
        } else if (this.argsMap != null) {
            imgs.addAll(this.argsMap.keySet());
        }

        this.countTotal = imgs.size();

        //note: now actually not encounter ids, but rather paths to individual dirs
        for (String eid : imgs) {
            String epath = baseDir + "/individuals/" + eid;
            if (log.isDebugEnabled()) {
                log.debug(LogBuilder.quickLog("epath", epath));
            }
//~jon/npm_process -contr_thr 0.02 -sigma 1.2 /opt/tomcat7/webapps/cascadia_data_dir/encounterxs 0 0 4 1 2
            //String[] command = new String[]{"/usr/bin/npm_process", "-contr_thr", "0.02", "-sigma", "1.2", epath, "0", "0", "4", "1", "2"};
//home/jon/npm_process -contr_thr 0.02 -sigma 1.2 cascadia_data_dir/ 0 0 4 1 2
            //String[] command = new String[]{"sh", "/opt/tomcat7/bin/run_npm_process.sh", epath};
            String[] command = new String[]{"/usr/local/bin/npm_process_wrapper.sh", epath};

            ProcessBuilder pb = new ProcessBuilder();
            Map<String, String> env = pb.environment();
            env.put("LD_LIBRARY_PATH", "/usr/local/lib/opencv2.4.7");
            pb.command(command);
            if (log.isDebugEnabled()) {
                log.debug("====================== npm_process on " + eid);
            }

            try {
                Process proc = pb.start();
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                String line;
                while ((line = stdInput.readLine()) != null) {
                    if (log.isDebugEnabled()) {
                        log.debug(eid + ">>>> " + line);
                    }
                }
                proc.waitFor();
                if (log.isDebugEnabled()) {
                    log.debug(eid + " DONE?????");
                }
                ////int returnCode = p.exitValue();

            } catch (Exception ioe) {
                log.error("oops", ioe);
            }

            this.countComplete++;

            try {
                PrintWriter statusOut = new PrintWriter(baseDir + "/encounters/importcsv.lock");
                statusOut.println(Integer.toString(this.countComplete) + " " + Integer.toString(this.countTotal));
                statusOut.close();
            } catch (Exception ex) {
                log.error("could not write " + baseDir + "/encounters/importcsv.lock.", ex);
            }
        }

        File ilock = new File(baseDir + "/encounters/importcsv.lock");
        if (ilock.exists()) ilock.delete();
    }


////// does the comparison/match, given a bunch of file paths as
    public void npmCompare() {
        String rootDir = servletContext.getRealPath("/");
        String baseDir = ServletUtilities.dataDir(context, rootDir).getAbsolutePath();
        String batchDir = baseDir + "/match_images/" + this.batchID;
        List<String> imgs = new ArrayList<String>();
        Map<String,List<String>> imgsMap = new HashMap<String,List<String>>();
        if (log.isDebugEnabled()) {
            log.debug("start npmCompare()");
        }

        if (this.args != null) {
            imgs = this.args;  //imgsMap will be empty here, triggering all-individual matching
        } else if (this.argsMap != null) {
            imgsMap = this.argsMap;
            imgs.addAll(this.argsMap.keySet());
        }


        this.countTotal = imgs.size();  //size of images uploaded

        for (String imgpath : imgs) {
            String fullpath = imgpath;
            if (fullpath.indexOf("/") != 0) fullpath = batchDir + "/" + imgpath;
            //String epath = Encounter.dir(baseDir, eid);
//~jon/npm_process -contr_thr 0.02 -sigma 1.2 /opt/tomcat7/webapps/cascadia_data_dir/encounterxs 0 0 4 1 2
//whalematch.exe -sscale 1.1 15.16 "C:\flukefolder" "C:\flukefolder\whale1\whale1fluke1.jpg"  0 0 2 0 -o whaleID_whale1fluke1.xhtml -c whaleID_whale1fluke1.csv
            List<String> pcat = imgsMap.get(imgpath);  //really i think we should only ever have ONE value
            if (log.isDebugEnabled()) {
                LogBuilder builder = new LogBuilder();
                builder.appendVar("fullpath", fullpath);
                builder.appendVar("pcat", pcat);
                log.debug(builder.toString());
            }

            String[] command;
            if (pcat == null) {
                if (log.isDebugEnabled()) {
                    log.debug("using ALL individuals");
                }
                command = new String[]{"/usr/local/bin/npm_both_wrapper.sh", fullpath, baseDir + "/individuals"};
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("using FILTERED individuals, see: " + fullpath + "-in.txt");
                }
                String intxt = fullpath + "\n";
                String filterString = "";
                if (pcat.get(0).equals("1")) {
                    filterString = "(patterningCode.startsWith(\"1\") || patterningCode.startsWith(\"2\"))";
                } else if (pcat.get(0).equals("2")) {
                    filterString = "(patterningCode.startsWith(\"1\") || patterningCode.startsWith(\"2\") || patterningCode.startsWith(\"3\"))";
                } else if (pcat.get(0).equals("3")) {
                    filterString = "(patterningCode.startsWith(\"2\") || patterningCode.startsWith(\"3\") || patterningCode.startsWith(\"4\"))";
                } else if (pcat.get(0).equals("4")) {
                    filterString = "(patterningCode.startsWith(\"3\") || patterningCode.startsWith(\"4\") || patterningCode.startsWith(\"5\"))";
                } else if (pcat.get(0).equals("5")) {
                    filterString = "(patterningCode.startsWith(\"4\") || patterningCode.startsWith(\"5\"))";
                }
                if (log.isDebugEnabled()) {
                    log.debug(LogBuilder.quickLog("filterString", filterString));
                }

                Shepherd myShepherd = new Shepherd(this.context);
                Query query = myShepherd.getPM().newQuery("SELECT FROM org.ecocean.MarkedIndividual WHERE " + filterString);
                Iterator allInds = myShepherd.getAllMarkedIndividuals(query);
                if (allInds == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("NO individuals match query");
                    }
                    return;
                }
                while (allInds.hasNext()) {
                    MarkedIndividual ind = (MarkedIndividual)allInds.next();
                    intxt += baseDir + "/individuals/" + ind.getIndividualID() + "\n";
                }

                try {
                    PrintWriter statusOut = new PrintWriter(fullpath + "-in.txt");
                    statusOut.print(intxt);
                    statusOut.close();
                } catch (Exception ex) {
                    log.error("could not write " + fullpath + "-in.txt", ex);
                }
                command = new String[]{"/usr/local/bin/npm_both_wrapper_filtered.sh", fullpath, fullpath + "-in.txt"};
            }
            //String[] command = new String[]{"/usr/bin/npm_match", "-sscale", "1.1", "15.16", baseDir + "/encounters", imgpath, "0", "0", "2", "0", "-o", "/tmp/out.txt", "-c", "/tmp/out.csv"};
//home/jon/npm_process -contr_thr 0.02 -sigma 1.2 cascadia_data_dir/ 0 0 4 1 2
            //String[] command = new String[]{"sh", "/opt/tomcat7/bin/run_npm_process.sh", epath};

            ProcessBuilder pb = new ProcessBuilder();
            Map<String, String> env = pb.environment();
            env.put("LD_LIBRARY_PATH", "/usr/local/lib/opencv2.4.7");
            pb.command(command);
            if (log.isDebugEnabled()) {
                log.debug(LogBuilder.quickLog("====================== npm_match on", imgpath));
            }

            try {
                Process proc = pb.start();
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String line;
                while ((line = stdInput.readLine()) != null) {
                    if (log.isDebugEnabled()) {
                        log.debug(imgpath + ">>>> " + line);
                    }
                }
                proc.waitFor();
                if (log.isDebugEnabled()) {
                    log.debug(imgpath + " DONE?????");
                }
                ////int returnCode = p.exitValue();

            } catch (Exception ioe) {
                log.error("oops", ioe);
            }
            this.countComplete++;

            Gson gson = new Gson();
            String c = "{ \"filters\": " + gson.toJson(imgsMap) + ", \"countComplete\": " + Integer.toString(this.countComplete) + ", \"countTotal\": " + Integer.toString(this.countTotal);
            if (this.countComplete >= this.countTotal) c += ", \"done\": true ";
            c += " }";
            writeStatusFile(this.servletContext, this.context, this.batchID, c);
        }
    }


    @Override
    public void run()
    {
        if (this.method.equals("npmProcess")) {
            npmProcess();
        } else {
            npmCompare();
        }

        status = Status.INIT;
    }

    public static boolean writeStatusFile(ServletContext servletContext, String context, String batchID, String contents) {
        String rootDir = servletContext.getRealPath("/");
        String baseDir = ServletUtilities.dataDir(context, rootDir).getAbsolutePath();
        String batchDir = baseDir + "/match_images/" + batchID;

        try {
            PrintWriter statusOut = new PrintWriter(batchDir + "/status.json");
            statusOut.println(contents);
            statusOut.close();
        } catch (Exception ex) {
            log.error("could not write " + baseDir + "/status.json", ex);
            return false;
        }

        return true;
    }
}
