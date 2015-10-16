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
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samsix.util.OsUtils;
import com.samsix.util.string.StringUtilities;

/**
 * Does actual comparison processing of batch-uploaded images.
 *
 * @author Jon Van Oast
 */
public final class ImageProcessor implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(ImageProcessor.class);

    private String command = null;
    private String imageSourcePath = null;
    private String imageTargetPath = null;
    private String arg = null;
    private int width = 0;
    private int height = 0;
    private IOException exception;

    public ImageProcessor(final String action,
                          final int width,
                          final int height,
                          final String imageSourcePath,
                          final String imageTargetPath,
                          final String arg) {
        this.width = width;
        this.height = height;
        this.imageSourcePath = imageSourcePath;
        this.imageTargetPath = imageTargetPath;
        this.arg = arg;

        String propKey = "command.image." + action;
        String ext = OsUtils.getFileExtension(imageTargetPath);
        //
        // look for any special commands for this type of file. Otherwise fallback to standard one.
        //
        if (! StringUtils.isBlank(ext)) {
            String propKey2 = propKey + "." + ext.toLowerCase();
            if (logger.isDebugEnabled()) {
                logger.debug("Checking for property [" + propKey2 + "]");
            }
            this.command = Global.INST.getAppResources().getString(propKey2, null);
        }

        if (this.command == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Checking for property [" + propKey + "]");
            }
            this.command = Global.INST.getAppResources().getString(propKey, null);
        }
    }

    public IOException getException() {
        return exception;
    }


    @Override
    public void run()
    {
        if (StringUtils.isBlank(this.command)) {
            logger.warn("Can't run processor due to empty command");
            return;
        }

        if (StringUtils.isBlank(this.imageSourcePath)) {
            logger.warn("Can't run processor due to empty source path");
            return;
        }

        if (StringUtils.isBlank(this.imageTargetPath)) {
            logger.warn("Can't run processor due to empty target path");
            return;
        }

        String fullCommand;
        fullCommand = this.command.replaceAll("%width", Integer.toString(this.width))
                                  .replaceAll("%height", Integer.toString(this.height))
                                  //.replaceAll("%imagesource", this.imageSourcePath)
                                  //.replaceAll("%imagetarget", this.imageTargetPath)
                                  .replaceAll("%arg", this.arg);
        String[] command = fullCommand.split("\\s+");

        //we have to do this *after* the split-on-space cuz files may have spaces!
        for (int i = 0 ; i < command.length ; i++) {
            if (command[i].equals("%imagesource")) command[i] = this.imageSourcePath;
            if (command[i].equals("%imagetarget")) command[i] = this.imageTargetPath;
        }

        if (logger.isDebugEnabled()) {
            logger.debug(StringUtilities.arrayToString(command, " "));
        }

        ProcessBuilder pb = new ProcessBuilder();
        pb.command(command);
        try {
            Process proc = pb.start();
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String line;
            while ((line = stdInput.readLine()) != null) {
                logger.info(line);
            }
            while ((line = stdError.readLine()) != null) {
                //
                // I am going to make the error stream go to warn because one of the things I was seeing here
                // was the following example...
                //     convert: profile 'icc': 'RGB ': RGB color space not permitted on grayscale PNG `/tmp/media/import/cascadia/mid/PStap110626fr5661_Mn_ID_S#_W4.png' @ warning/png.c/MagickPNGWarningHandler/1656.
                // ...which is just a warning actually. The resulting PNG is fine.
                //
                logger.warn(line);
            }
            proc.waitFor();
        } catch (IOException|InterruptedException ex) {
            String msg = "Trouble running image processor command [" + StringUtilities.arrayToString(command, " ") + "]";
            exception = new IOException(msg, ex);
            logger.error(msg, ex);
        }
    }
}
