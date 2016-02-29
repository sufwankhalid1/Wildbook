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

package org.ecocean.mmutil;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.ecocean.Global;
import org.ecocean.ImageProcessor;
import org.ecocean.media.AssetStore;
import org.ecocean.media.ImageMeta;
import org.ecocean.media.MediaAsset;
import org.ecocean.util.FileUtilities;
import org.ecocean.util.LogBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.samsix.util.OsUtils;
import com.samsix.util.io.ResourceReader;

/**
 * Class providing centralized image-related services, such as image rescaling,
 * JPEG saving, thumbnail generation, etc.
 */
public final class MediaUtilities {
    /** SLF4J logger instance for writing log entries. */
    private static Logger logger = LoggerFactory.getLogger(MediaUtilities.class);
    /** ColorSpace for sRGB images (used for conversions). */
    private static final ICC_ColorSpace CS_sRGB = new ICC_ColorSpace(ICC_Profile.getInstance(ColorSpace.CS_sRGB));
    /** Regex pattern string suffix for matching image filenames (case-insensitive, capturing group). */
    public static final String REGEX_SUFFIX_FOR_OTHER_IMAGES = "(?i:(tiff?|bmp))$";
    /** Regex pattern string suffix for matching image filenames (case-insensitive, capturing group). */
    public static final String REGEX_SUFFIX_FOR_WEB_IMAGES = "(?i:(jpe?g?|png|gif))$";
    /** Regex pattern string suffix for matching video filenames (case-insensitive, capturing group). */
    public static final String REGEX_SUFFIX_FOR_MOVIES = "(?i:(mp4|mpg|mov|wmv|avi|flv))$";

    public static final String IMAGE_TYPE_THUMB = "thumb";
    public static final String IMAGE_TYPE_MID = "mid";

    private final static ExecutorService executor = Executors.newFixedThreadPool(5);


    /**
     * Gets a JPEG {@code ImageWriter} instance.
     */
    private static ImageWriter getImageWriter_JPEG() throws IOException {
        // Obtain ImageWriter instance for JPEG images.
        for (Iterator<ImageWriter> iter = ImageIO.getImageWritersByMIMEType("image/jpeg"); iter.hasNext();) {
            ImageWriter iw = iter.next();
            ImageWriteParam iwp = iw.getDefaultWriteParam();
            if (iwp.canWriteCompressed() && iwp.canWriteProgressive())
                return iw;
        }
        throw new IOException("No JPEG ImageWriter available");
    }

    private MediaUtilities() {
        // prevent instantiation
    }

    /**
     * Checks filename extension for supported image type.
     * This method had been recreated here to allow static and centralized access;
     * original version requires a {@code Shepherd} instance.
     * @param filename filename of file to check
     * @return true if filename is supported, false otherwise
     */
    public static boolean isWebImageFile(final String filename) {
        return (filename == null) ? false : filename.matches("^.+\\." + REGEX_SUFFIX_FOR_WEB_IMAGES);
    }

    public static boolean isImageFile(final String filename) {
        if (isWebImageFile(filename)) {
            return true;
        }

        return (filename == null) ? false : filename.matches("^.+\\." + REGEX_SUFFIX_FOR_OTHER_IMAGES);
    }

    /**
     * Checks filename extension for supported video type.
     * This method had been recreated here to allow static and centralized access;
     * original version requires a {@code Shepherd} instance.
     * @param filename filename of file to check
     * @return true if filename is supported, false otherwise
     */
    public static boolean isVideoFile(final String filename) {
        return (filename == null) ? false : filename.matches("^.+\\." + REGEX_SUFFIX_FOR_MOVIES);
    }

    public static boolean isGpsFile(final String fileName) {
        if ((fileName.toLowerCase().indexOf(".kmz") != -1)
             || (fileName.toLowerCase().indexOf(".kml") != -1)
             || (fileName.toLowerCase().indexOf(".gpx") != -1)) {
            return true;
        }
        return false;
    }

    /**
     * Checks filename extension for supported image type.
     * @param file file to check
     * @return true if filename is supported, false otherwise
     */
    public static boolean isWebImageFile(final Path path) {
        return (path == null) ? false : isWebImageFile(path.getFileName());
    }

    public static boolean isImageFile(final Path path) {
        return (path == null) ? false : isImageFile(path.getFileName());
    }

    /**
     * Checks filename extension for supported video type.
     * @param file file to check
     * @return true if filename is supported, false otherwise
     */
    public static boolean isVideoFile(final Path path) {
        return (path == null) ? false : isVideoFile(path.getFileName());
    }

    /**
     * Checks filename extension for JPEG image type.
     * @param file file to check
     * @return true if filename is the correct format, false otherwise
     */
    public static boolean isFileType_JPEG(final Path path) {
        return path.getFileName().toString().matches("(?i:\\.(jpe?g))$");
    }

    /**
     * Checks filename extension for PNG image type.
     * @param file file to check
     * @return true if filename is the correct format, false otherwise
     */
    public static boolean isFileType_PNG(final Path path) {
        return path.getFileName().toString().matches("(?i:\\.(png))$");
    }

    /**
     * Checks filename extension for PNG image type.
     * @param file file to check
     * @return true if filename is the correct format, false otherwise
     */
    public static boolean isFileType_TIFF(final Path path) {
        return path.getFileName().toString().matches("(?i:\\.(tiff?))$");
    }

    /**
     * Checks filename extension for PNG image type.
     * @param file file to check
     * @return true if filename is the correct format, false otherwise
     */
    public static boolean isFileType_BMP(final Path path) {
        return path.getFileName().toString().matches("(?i:\\.(bmp))$");
    }

//  /**
//   * Lists all web-compatible image files in a folder matching the specified base name.
//   * @param dir folder in which to search
//   * @param baseName base name of web-compatible images for which to search
//   * @return list of files
//   */
//  public static List<File> listWebImageFiles(final File dir, final String baseName) {
//    RegexFilenameFilter ff = WebImageFilenameFilter.instance();
//    List<File> list = new LinkedList<File>();
//    File[] fileList = dir.listFiles(ff);
//    if (fileList != null) {
//      list.addAll(Arrays.asList(fileList));
//      for (ListIterator<File> it = list.listIterator(); it.hasNext();) {
//        File f = it.next();
//        String s = f.getName();
//        if (!s.substring(0, s.lastIndexOf(".")).equals(baseName))
//          it.remove();
//      }
//    }
//    return list;
//  }

//  /**
//   * Lists all web-compatible image files in a folder matching the specified base name regex.
//   * @param dir folder in which to search
//   * @param baseNameRegex base name regex of web-compatible images for which to search
//   * @return list of files
//   */
//  public static List<File> listWebImageFilesRegex(final File dir, final String baseNameRegex) {
//    RegexFilenameFilter ff = WebImageFilenameFilter.instance();
//    List<File> list = new LinkedList<File>(Arrays.asList(dir.listFiles(ff)));
//    for (ListIterator<File> it = list.listIterator(); it.hasNext();) {
//      File f = it.next();
//      String s = f.getName();
//      if (!s.substring(0, s.lastIndexOf(".")).matches(baseNameRegex))
//        it.remove();
//    }
//    return list;
//  }

    /**
     * Loads the specified image from the specified file.
     * @param f file to which to save image
     * @return {@code BufferedImage} instance
     * @throws IOException if an I/O problem occurs
     */
    public static BufferedImage loadImage(final Path path) throws IOException {
        if (path == null) {
            throw new NullPointerException("Invalid (null) file specified");
        } else if (!Files.exists(path)) {
            throw new FileNotFoundException(String.format("File %s doesn't exist", path));
        } else if (Files.isDirectory(path)) {
            throw new FileNotFoundException(String.format("%s is a folder", path));
        }

        // Read image file.
        BufferedImage img = ImageIO.read(path.toFile());

        // Search metadata for orientation change to apply.
        try {
            Metadata md = ImageMetadataReader.readMetadata(path.toFile());
            Directory dir = md.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (dir == null) {
                return img;
            }
            // Check orientation & define transform.
            if (dir.containsTag(ExifIFD0Directory.TAG_ORIENTATION))
            {
                int orientation = dir.getInt(ExifIFD0Directory.TAG_ORIENTATION);
                AffineTransform tx = getOrientationTransform(img, orientation);
                // Transform image.
                if (tx != null) {
                    BufferedImage tmp = transformImage(img, tx);
                    img.flush();
                    img = tmp;
                }
            }
        } catch (ImageProcessingException ex) {
            // Warn, and return original.
            logger.warn("Unable to read metadata for image: " + path, ex);
        } catch (MetadataException ex) {
            // Warn, and return original.
            logger.warn("Unable to read metadata for image: " + path, ex);
        }
        // Return image.
        return img;
    }

    /**
     * Loads the specified image from the specified file, converting it to
     * the sRGB color profile if necessary.
     * @param f file to which to save image
     * @return {@code BufferedImage} instance (converted to sRGB color profile)
     * @throws IOException if an I/O problem occurs
     */
    public static BufferedImage loadImageAsSRGB(final Path path) throws IOException {
        BufferedImage bi = loadImage(path);
        BufferedImage img = convertToSRGB(bi);
        if (img != bi) {
            bi.flush();
        }
        return img;
    }

    /**
     * Creates an appropriate affine transform to correct the EXIF orientation
     * specified. The orientation number should be obtained from the EXIF IDFD0
     * Orientation metadata field.
     * @param img image to be transformed
     * @param orientation EXIF orientation identifier
     * @return {@code AffineTransform} instance
     */
    private static AffineTransform getOrientationTransform(final BufferedImage img, final int orientation) {
        AffineTransform tx = null;
        int w = img.getWidth();
        int h = img.getHeight();
        switch (orientation) {
        case 1: // "top, left side (normal)"
            break;
        case 2: // "top, right side (mirror horizontal)";
            tx = AffineTransform.getScaleInstance(-1, 1);
            tx.translate(-w, 0);
            break;
        case 3: // "bottom, right side (mirror horizontal/vertical)";
            tx = AffineTransform.getScaleInstance(-1, -1);
            tx.translate(-w, -h);
            break;
        case 4: // "bottom, left side (mirror vertical)";
            tx = AffineTransform.getScaleInstance(1, -1);
            tx.translate(0, -h);
            break;
        case 5: // "left side, top (mirror horizontal, rotate 90)";
            tx = AffineTransform.getScaleInstance(-1, 1);
            tx.translate(-h / 2, w / 2);
            tx.quadrantRotate(1);
            tx.translate(-w / 2, -h / 2);
            break;
        case 6: // "right side, top (rotate 90)";
            tx = AffineTransform.getTranslateInstance(h / 2, w / 2);
            tx.quadrantRotate(1);
            tx.translate(-w / 2, -h / 2);
            break;
        case 7: // "right side, bottom (mirror horizontal, rotate 90)";
            tx = AffineTransform.getScaleInstance(-1, 1);
            tx.translate(-h / 2, w / 2);
            tx.quadrantRotate(3);
            tx.translate(-w / 2, -h / 2);
            break;
        case 8: // "Left side, bottom (rotate 270)";
            tx = AffineTransform.getTranslateInstance(h / 2, w / 2);
            tx.quadrantRotate(3);
            tx.translate(-w / 2, -h / 2);
            break;
        }
        return tx;
    }

    /**
     * Transforms an image according to the specified affine transform.
     * @param image image to transform
     * @param transform affine transform to apply
     * @return {@code BufferedImage} instance
     */
    private static BufferedImage transformImage(final BufferedImage image, final AffineTransform transform) {
        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);
        BufferedImage img = op.createCompatibleDestImage(image, (image.getType() == BufferedImage.TYPE_BYTE_GRAY) ? image.getColorModel() : null);
        img = op.filter(image, img);
        return img;
    }

    /**
     * Saves the specified image to the specified file.
     * @param img image to save in JPEG format
     * @param f file to which to save image
     * @param overwrite whether to overwrite file if it already exists
     * @param quality JPEG quality (0-1)
     * @param progressive whether to save as a progressive JPEG
     */
    public static void saveImageJPEG(final BufferedImage img, final Path path, final boolean overwrite, final float quality, final boolean progressive) throws IOException {
        if (img == null) {
            throw new NullPointerException("Invalid (null) image specified");
        }

        if (path == null) {
            throw new NullPointerException("Invalid (null) file specified");
        }

        if (quality < 0f || quality > 1f) {
            throw new IllegalArgumentException("Invalid JPEG quality specified (0 <= quality <= 1)");
        }

        if (Files.exists(path)) {
            if (overwrite) {
                Files.delete(path);
            } else {
                throw new IOException(String.format("File %s already exists", path));
            }
        }

        ImageWriter iw = getImageWriter_JPEG();
        ImageWriteParam iwp = iw.getDefaultWriteParam();
        if (iwp.canWriteCompressed()) {
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwp.setCompressionQuality(quality);
        }

        if (iwp.canWriteProgressive()) {
            iwp.setProgressiveMode(progressive ? ImageWriteParam.MODE_DEFAULT : ImageWriteParam.MODE_DISABLED);
        }

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(path.toFile())) {
            iw.setOutput(ios);
            iw.write(null, new IIOImage(img, null, null), iwp);
            iw.reset();
        } finally {
            iw.dispose();
        }
    }

    /**
     * Convenience method to convert a {@code BufferedImage} to the sRGB
     * color space if not already compatible.
     * @param image image to convert (if necessary)
     * @return A {@code BufferedImage} instance in sRGB color space
     */
    public static BufferedImage convertToSRGB(final BufferedImage image) {
        if (image == null) {
            throw new NullPointerException("Invalid (null) image specified");
        }
        final ColorModel cm = image.getColorModel();
        // If not using sRGB colorspace, convert to sRGB before processing.
        if (cm != null && !cm.getColorSpace().isCS_sRGB()) {
            BufferedImage img = null;
            ColorConvertOp cco = new ColorConvertOp(CS_sRGB, null);
            img = cco.filter(image, null);
            image.flush();
            return img;
        }
        return image;
    }

    /**
     * Convenience method that returns a scaled instance of the
     * provided {@code BufferedImage}.
     * Any transparency/alpha of the original image is not preserved.
     *
     * @param img the original image to be scaled
     * @param targetWidth the desired width of the scaled instance, in pixels
     * @param targetHeight the desired height of the scaled instance, in pixels
     * @param hint one of the rendering hints that corresponds to
     *    {@link java.awt.RenderingHints.KEY_INTERPOLATION} (e.g.
     *    {@link java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
     *    {@link java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR},
     *    {@link java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @return a scaled version of the original {@code BufferedImage}
     */
    public static BufferedImage rescaleImage(final BufferedImage img, final int targetWidth, final int targetHeight, final Object hint) {
        if (img == null) {
            throw new NullPointerException("Invalid (null) image specified");
        }
        BufferedImage tmp = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = tmp.createGraphics();
        if (hint != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
        }
        g2.drawImage(img, 0, 0, targetWidth, targetHeight, null);
        g2.dispose();
        return tmp;
    }

    /**
     * Convenience method that returns a scaled instance of the
     * provided {@code BufferedImage} with a text overlay.
     * Any transparency/alpha of the original image is not preserved.
     * This is a simple implementation which was created solely for the purpose
     * of reproducing thumbnail images with a copyright message overlay,
     * as done by the deprecated Sunwest Technologies Dynamic Images library.
     */
    public static BufferedImage rescaleImageWithTextOverlay(final BufferedImage img, final int tw, final int th, final String text) {
        if (img == null) {
            throw new NullPointerException("Invalid (null) image specified");
        }
        BufferedImage tmp = new BufferedImage(tw, th, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = tmp.createGraphics();
        int yPos = th / 3;
        // RenderingHints for determining image quality.
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // White translucent overlay over whole image.
        Rectangle2D rectWash = new Rectangle2D.Float(0, 0, tw, th);
        Paint paintWash = new Color(0xFF, 0xFF, 0xFF, 0x4D);

        // Text overlay.
        Font font = new Font("Arial", Font.BOLD, 11);
        FontMetrics fm = g2.getFontMetrics(font);
        Paint paintText = Color.black;
        // Strip behind text.
        Paint paintStrip = new Color(0x99, 0xCC, 0xFF, 0x4D);
        Rectangle2D rectStrip = new Rectangle2D.Float(0, yPos, tw, 13);
        Rectangle2D textBounds = fm.getStringBounds(text, g2);
        if (textBounds.getWidth() > tw) {
            logger.warn("Text overlay too long for image width.");
        }

        g2.drawImage(img, 0, 0, tw, th, null);
        g2.setPaint(paintWash);
        g2.fill(rectWash);
        g2.setPaint(paintStrip);
        g2.fill(rectStrip);
        g2.setPaint(paintText);
        g2.setFont(font);
        g2.drawString(text, 4, yPos + fm.getHeight() - fm.getMaxDescent());
        g2.dispose();
        return tmp;
    }

    /**
     * Extracts all the EXIF tags from the specified Metadata instance.
     * This method ignores all GPS-related tags for data security reasons
     * (identifiers starting with &quot;GPS&quot;).
     * @param md {@code Metadata} instance from image file
     * @return list of {@code Tag} instances
     */
    public static List<Tag> extractMetadataTags(final Metadata md) {
        List<Tag> list = new ArrayList<Tag>();
        for (Directory dir : md.getDirectories()) {
            for (Tag tag : dir.getTags()) {
                if (!tag.getTagName().toUpperCase(Locale.US).startsWith("GPS"))
                    list.add(tag);
            }
        }
        return list;
    }

    private static Path getOutputFile(final AssetStore store, final Path relFile, final String altOutputDir) {
        if (StringUtils.isBlank(altOutputDir)) {
            return store.getFullPath(relFile);
        }

        return Paths.get(altOutputDir, relFile.toString());
    }

    public static Path writeMedia(final AssetStore store,
                                  final Path relFile,
                                  final InputStream content,
                                  final boolean keepStreamOpen,
                                  final String altOutputDir) throws IOException
    {
        Path file = getOutputFile(store, relFile, altOutputDir);

        Files.createDirectories(file.getParent());

        if (logger.isDebugEnabled()) {
            logger.debug(LogBuilder.quickLog("fullPath", file.toString()));
        }

        FileUtilities.saveStreamToFile(content, file, keepStreamOpen);

        if (logger.isDebugEnabled()) {
            logger.debug("Done saving stream.");
        }

        return file;
    }

    public static MediaAsset createMediaAsset(final AssetStore store,
                                              final Path relFile,
                                              final String origFilename,
                                              final Path file,
                                              final Integer submitterId) throws IOException {
        MediaAsset ma = new MediaAsset(store, relFile);

        try {
            ImageMeta meta = FileUtilities.getImageMetaData(file);
            if (meta != null) {
                ma.setMetaTimestamp(meta.getTimestamp());
                ma.setMetaLatitude(meta.getLatitude());
                ma.setMetaLongitude(meta.getLongitude());
                ma.addMeta("origFilename", origFilename);
            }
        } catch (ImageProcessingException ex) {
            logger.error("Problem reading metadata from [" + file + "]", ex);
        }
        ma.setSubmitterId(submitterId);
        ma.setSubmittedOn(LocalDateTime.now());

        setMediaResizes(ma, store);

        return ma;
    }

    public static MediaAsset saveMedia(final AssetStore store,
                                       final Path relFile,
                                       final String origFilename,
                                       final InputStream content,
                                       final Integer submitterId,
                                       final boolean keepStreamOpen,
                                       final String altOutputDir) throws IOException
    {
        Path file = writeMedia(store, relFile, content, keepStreamOpen, altOutputDir);
        return createMediaAsset(store, relFile, origFilename, file, submitterId);
    }


    private static void setMediaResizes(final MediaAsset ma, final AssetStore store) {
        switch (ma.getType()) {
        case IMAGE: {
            Path thumbRel = getRelResizePath(IMAGE_TYPE_THUMB, ma.getPath());
            ma.setThumb(store, thumbRel);
            Path midRel = getRelResizePath(IMAGE_TYPE_MID, ma.getPath());
            ma.setMid(midRel);
        }
        default:
            break;
        }
    }

    public static void processMedia(final MediaAsset ma,
                                    final AssetStore store,
                                    final Path relFile,
                                    final String altOutputDir) throws IOException {
        Path file = getOutputFile(store, relFile, altOutputDir);
        //
        // Make Thumbnail and midsize
        //
        switch (ma.getType()) {
        case IMAGE: {
            //
            // TODO: For now I'm assuming thumbstore will be same as the main file store. Feel
            // free to change later but allow for a "null" thumbstore to default to main store.
            //
            Path thumbRel = getRelResizePath(IMAGE_TYPE_THUMB, ma.getPath());
            createThumbnail(file, store, thumbRel, altOutputDir);

            Path midRel = getRelResizePath(IMAGE_TYPE_MID, ma.getPath());
            createMidSize(file, store, midRel, altOutputDir);
            break;
        }
        default:
            break;
        }
    }

    /**
     *
     * @param store
     * @param fileName
     * @param content
     * @param submitterId
     * @param keepStreamOpen
     * @param altOutputDir just for importing on a server that is not the final server. Allows
     *                     you to redirect the file output to another place where you can then tar
     *                     it up and copy up to the main server and untar in the correct
     *                     place as pointed to by the AssetStore
     *
     * @return the resulting MediaAsset
     * @throws IOException
     */
    public static MediaAsset importMedia(final AssetStore store,
                                         final Path relFile,
                                         final String origFilename,
                                         final InputStream content,
                                         final Integer submitterId,
                                         final boolean keepStreamOpen,
                                         final String altOutputDir) throws IOException
    {
        MediaAsset ma;
        ma = saveMedia(store, relFile, origFilename, content, submitterId, keepStreamOpen, altOutputDir);
        processMedia(ma, store, relFile, altOutputDir);
        return ma;
    }

    public static void createThumbnail(final Path file,
                                       final AssetStore store,
                                       final Path relResized,
                                       final String altOutputDir) throws IOException {
        createResized(IMAGE_TYPE_THUMB,
                      file,
                      store,
                      relResized,
                      altOutputDir,
                      100,
                      75);
    }

    public static void createMidSize(final Path file,
                                     final AssetStore store,
                                     final Path relResized,
                                     final String altOutputDir) throws IOException {
        createResized(IMAGE_TYPE_MID,
                      file,
                      store,
                      relResized,
                      altOutputDir,
                      800,
                      600);
    }

    private static String getResizeTypeKey(final String type) {
        return "images." + type + ".";
    }

    public static Path getRelResizePath(final String type, final Path relFile) {
        String ext = Global.INST.getAppResources().getString(getResizeTypeKey(type) + "ext", "jpg");
        String resizedFileName = OsUtils.getFileRoot(relFile.getFileName().toString()) + "." + ext;
        return Paths.get(relFile.getParent().toString(), type, resizedFileName);
    }

    private static void createResized(final String type,
                                      final Path file,
                                      final AssetStore store,
                                      final Path relResized,
                                      final String altOutputDir,
                                      final int defaultWidth,
                                      final int defaultHeight) throws IOException {
        ResourceReader resources = Global.INST.getAppResources();
        String keyBase = getResizeTypeKey(type);
        int width = resources.getInt(keyBase + "width", defaultWidth);
        int height = resources.getInt(keyBase + "height", defaultHeight);

        Path resized = getOutputFile(store, relResized, altOutputDir);


        Files.createDirectories(resized.getParent());

        ImageProcessor iproc;
        iproc = new ImageProcessor("resize",
                                   width,
                                   height,
                                   file.toString(),
                                   resized.toString(),
                                   null);
        iproc.run();

        if (iproc.getException() != null) {
            throw iproc.getException();
        }
    }

    public static void processMediaBackground(final PrintStream reporter,
                                              final MediaAsset ma,
                                              final AssetStore store,
                                              final Path relFile,
                                              final String altOutputDir) {
        executor.execute(new ProcessMedia(reporter, ma, store, relFile, altOutputDir));
    }


    //==============================
    // ProcessMedia class
    //==============================

    private static class ProcessMedia
        implements Runnable
    {
        private final MediaAsset ma;
        private final PrintStream reporter;
        private final AssetStore store;
        private final Path relFile;
        private final String altOutputDir;

        public ProcessMedia(final PrintStream reporter,
                            final MediaAsset ma,
                            final AssetStore store,
                            final Path relFile,
                            final String altOutputDIr) {
            this.reporter = reporter;
            this.ma = ma;
            this.store = store;
            this.relFile = relFile;
            this.altOutputDir = altOutputDIr;
        }

        @Override
        public void run() {
            try {
                if (reporter != null) {
                    reporter.println("Processing [" + relFile + "]...");
                }
                processMedia(ma, store, relFile, altOutputDir);
            } catch (Exception ex) {
                logger.error("Trouble processing media [" + ma.getID() + "]", ex);
            }
        }
    }
}
