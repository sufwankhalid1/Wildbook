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

package org.ecocean.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Date;

import org.ecocean.media.ImageMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;

/**
 *
 * @author Giles Winstanley
 */
public class FileUtilities {
  /** SLF4J logger instance for writing log entries. */
  private static final Logger logger = LoggerFactory.getLogger(FileUtilities.class);

  private FileUtilities() {}

  /**
   * Loads the contents of a specified file.
   * @param f File to load
   * @return A byte array containing the contents of the specified {@code File}.
   */
  public static byte[] loadFile(File f) throws IOException {
    if (!f.exists()) {
      throw new FileNotFoundException();
    }
    FileInputStream fis = null;
    try {
      ByteArrayOutputStream bao = new ByteArrayOutputStream();
      fis = new FileInputStream(f);
      byte[] b = new byte[4096];
      int n;
      while ((n = fis.read(b)) != -1) {
        bao.write(b, 0, n);
      }
      return bao.toByteArray();
    } finally {
      if (fis != null) {
        try {
          fis.close();
        } catch (IOException iox) {
          logger.warn(iox.getMessage(), iox);
        }
      }
    }
  }

  /**
   * Copies a file to another location.
   * @param src source file
   * @param dst destination file
   * @throws IOException if there is a problem copying the file
   */
    public static void copyFile(File src, File dst) throws IOException {
        if (src == null) {
            throw new NullPointerException("Invalid source file specified: null");
        }

        if (dst == null) {
            throw new NullPointerException("Invalid destination file specified: null");
        }

        if (!src.exists()) {
            throw new IOException("Invalid source file specified: " + src.getAbsolutePath());
        }

        if (dst.exists()) {
            throw new IOException("Destination file already exists: " + dst.getAbsolutePath());
        }

        saveStreamToFile(new FileInputStream(src), dst);
    }


    public static void saveStreamToFile(final InputStream input, final File file)
        throws IOException
    {
        redirectStream(input, new FileOutputStream(file), false);
    }


    public static void saveStreamToFile(final InputStream input, final File file, final boolean keepOpen)
        throws IOException
    {
        redirectStream(input, new FileOutputStream(file), keepOpen);
    }


    private static void redirectStream(final InputStream input, final OutputStream output, final boolean keepInOpen)
        throws IOException
    {
        InputStream in = new BufferedInputStream(input);
        OutputStream out = new BufferedOutputStream(output);

        try {
            byte[] b = new byte[8192];
            int len = 0;
            while ((len = in.read(b)) != -1) {
                out.write(b, 0, len);
            }
            out.flush();
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                logger.warn(ex.getMessage(), ex);
            }
            if (! keepInOpen) {
                try {
                    in.close();
                } catch (IOException ex) {
                    logger.warn(ex.getMessage(), ex);
                }
            }
        }
    }


  /**
   * Downloads the byte contents of a URL to a specified file.
   * @param url URL to download
   * @param file File to which to write downloaded data
   * @throws IOException
   */
  public static void downloadUrlToFile(URL url, File file) throws IOException {
    BufferedInputStream is = null;
    BufferedOutputStream os = null;
    try {
      is = new BufferedInputStream(url.openStream());
      os = new BufferedOutputStream(new FileOutputStream(file));
      byte[] b = new byte[4096];
      int len = -1;
      while ((len = is.read(b)) != -1)
        os.write(b, 0, len);
      os.flush();
    } finally {
      if (os != null)
        os.close();
      if (is != null)
        is.close();
    }
  }

    public static File findResourceOnFileSystem(final String resourceName) {
        if (logger.isDebugEnabled()) {
            logger.debug("Looking for resource [" + resourceName + "]");
        }

        URL resourceURL = FileUtilities.class.getClassLoader().getResource(resourceName);

        if (resourceURL == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Resource is not found");
            }
            return null;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Looking for resourceURL [" + resourceURL + "]");
        }

        String resourcePath = resourceURL.getPath();
        if (resourcePath == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Resource path is null");
            }
            return null;
        }

        File tmp = new File(resourcePath);
        if (tmp.exists()) {
            return tmp;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Resource URL is not found");
        }

        return null;
    }

    public static ImageMeta getImageMetaData(File file) throws ImageProcessingException, IOException {
        int index = file.getAbsolutePath().lastIndexOf('.');
        if (index < 0) {
            return null;
        }

        ImageMeta meta = null;

        switch (file.getAbsolutePath().substring(index+1).toLowerCase()) {
            case "jpg":
            case "jpeg":
            case "png":
            case "tiff":
            case "arw":
            case "nef":
            case "cr2":
            case "orf":
            case "rw2":
            case "rwl":
            case "srw":
            {
                if (file.exists()) {
                    meta = new ImageMeta();
                    Metadata metadata = ImageMetadataReader.readMetadata(file);
                    // obtain the Exif directory
                    ExifSubIFDDirectory directory = null;
                    directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
                    // query the tag's value
                    Date date = null;
                    if (directory != null) date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                    if (date != null) {
                        meta.setTimestamp(DateUtils.dateToLDT(date));
                    }

                    // See whether it has GPS data
                    Collection<GpsDirectory> gpsDirectories = metadata.getDirectoriesOfType(GpsDirectory.class);
                    if (gpsDirectories != null) {
                        for (GpsDirectory gpsDirectory : gpsDirectories) {
                            // Try to read out the location, making sure it's non-zero
                            GeoLocation geoLocation = gpsDirectory.getGeoLocation();
                            if (geoLocation != null && !geoLocation.isZero()) {
                                meta.setLatitude(geoLocation.getLatitude());
                                meta.setLongitude(geoLocation.getLongitude());
                            }
                        }
                    }

//                    // iterate through metadata directories
//                    List<Tag> list = new ArrayList<Tag>();
//                    for (Directory dir : metadata.getDirectories()) {
//                        if ("exif".equals(dir.getName().toLowerCase()) {
//                            dir.
//                        }
//
//                        for (Tag tag : dir.getTags()) {
//                            if (!tag.getTagName().toUpperCase(Locale.US).startsWith("GPS"))
//                                list.add(tag);
//                        }
//                    }
                }
            }
        }
        return meta;
    }
}
