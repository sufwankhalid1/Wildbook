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
import java.io.InputStreamReader;

//import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jon Van Oast
 */
public final class GeoFileProcessor implements Runnable {
    private static Logger log = LoggerFactory.getLogger(GeoFileProcessor.class);

  //private String context = "context0";
	private String geoFilePath = null;

  public GeoFileProcessor(String geoFilePath) {
		//this.context = context;
		this.geoFilePath = geoFilePath;
	}


	public void run() {
/*
		if (this.geoFilePath == null) {
			log.warn("geoFilePath is null");
			return;
		}
*/

		String[] command = new String[] {"/usr/local/bin/geoFileProcess.sh", this.geoFilePath};

//System.out.println("done run()");

//if (this.geoFilePath != null) return;
		ProcessBuilder pb = new ProcessBuilder();
		pb.command(command);
/*
		Map<String, String> env = pb.environment();
		env.put("LD_LIBRARY_PATH", "/home/jon/opencv2.4.7");
*/
//System.out.println("before!");

		try {
			Process proc = pb.start();
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			String line;
			while ((line = stdInput.readLine()) != null) {
				System.out.println(">>>> " + line);
			}
			while ((line = stdError.readLine()) != null) {
				System.out.println("!!!! " + line);
			}
			proc.waitFor();
//System.out.println("DONE?????");
			////int returnCode = p.exitValue();
		} catch (Exception ioe) {
		    log.error("Trouble running processor [" + command + "]", ioe);
		}
//System.out.println("RETURN");
	}
}
