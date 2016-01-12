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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    private static final Logger logger = LoggerFactory.getLogger(FileUtilities.class);

     private FileUtilities() {}

     /**
      * Loads the contents of a specified file.
      * @param f File to load
      * @return A byte array containing the contents of the specified {@code File}.
      */
     public static byte[] loadFile(final Path f) throws IOException {
         if (!Files.exists(f)) {
             throw new FileNotFoundException();
         }

         FileInputStream fis = null;
         try {
             ByteArrayOutputStream bao = new ByteArrayOutputStream();
             fis = new FileInputStream(f.toFile());
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

    public static void saveStreamToFile(final InputStream input, final Path path)
        throws IOException
    {
        redirectStream(input, new FileOutputStream(path.toFile()), false);
    }


    public static void saveStreamToFile(final InputStream input, final Path path, final boolean keepOpen)
        throws IOException
    {
        redirectStream(input, new FileOutputStream(path.toFile()), keepOpen);
    }

    /**
     * Deletes a directory and all of it's content, including sub-dirs.
     * @throws IOException
     *
     */
    public static void deleteCascade(final Path path) throws IOException {
        //
        // If it's a directory then we have to first clean it out by deleting
        // all of it's contents.
        //
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path child : stream) {
                    deleteCascade(child);
                }
            }
        }

        Files.delete(path);
    }

    private static boolean deleteDirIfEmpty(final Path path) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            if (! stream.iterator().hasNext()) {
                Files.delete(path);
                return true;
            }
            return false;
        }
    }


    /**
     * Deletes a file or *empty* directory and then deletes all parent directories
     * recursively as long as they are empty.
     *
     * @param path
     * @throws IOException
     */
    public static void deleteAndPrune(final Path path) throws IOException {
        boolean deleted;
        if (Files.isDirectory(path)) {
            deleted = deleteDirIfEmpty(path);
        } else {
            deleted = Files.deleteIfExists(path);
        }

        //
        // prune parents
        //
        if (deleted) {
            deleteAndPrune(path.getParent());
        }
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
    public static void downloadUrlToFile(final URL url, final Path path) throws IOException {
        BufferedInputStream is = null;
        BufferedOutputStream os = null;
        try {
            is = new BufferedInputStream(url.openStream());
            os = new BufferedOutputStream(new FileOutputStream(path.toFile()));
            byte[] b = new byte[4096];
            int len = -1;
            while ((len = is.read(b)) != -1) {
                os.write(b, 0, len);
            }
            os.flush();
        } finally {
            if (os != null) {
                os.close();
            }
            if (is != null) {
                is.close();
            }
        }
    }

    public static Path findResourceOnFileSystem(final String resourceName) {
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

        Path tmp = Paths.get(resourcePath);
        if (Files.exists(tmp)) {
            return tmp;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Resource URL is not found");
        }

        return null;
    }

    public static ImageMeta getImageMetaData(final Path path) throws ImageProcessingException, IOException {
        int index = path.toString().lastIndexOf('.');
        if (index < 0) {
            return null;
        }

        ImageMeta meta = null;

        switch (path.toString().substring(index+1).toLowerCase()) {
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
                if (Files.exists(path)) {
                    meta = new ImageMeta();
                    Metadata metadata = ImageMetadataReader.readMetadata(path.toFile());
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

    private static Path buildPath(final Path root, final Path child) {
        if (root == null) {
            return child;
        } else {
            return Paths.get(root.toString(), child.toString());
        }
    }

    private static void addZipDir(final ZipOutputStream out, final Path root, final Path dir) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path child : stream) {
                Path entry = buildPath(root, child.getFileName());
                if (Files.isDirectory(child)) {
                    addZipDir(out, entry, child);
                } else {
                    out.putNextEntry(new ZipEntry(entry.toString()));
                    Files.copy(child, out);
                    out.closeEntry();
                }
            }
        }
    }

    public static void zipDir(final Path path) throws IOException {
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path must be a directory.");
        }

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path.toString() + ".zip"));

        try (ZipOutputStream out = new ZipOutputStream(bos)) {
            addZipDir(out, path.getFileName(), path);
        }
    }
}
