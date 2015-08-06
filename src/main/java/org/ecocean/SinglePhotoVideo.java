package org.ecocean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.ecocean.servlet.ServletUtilities;


public class SinglePhotoVideo extends DataCollectionEvent {

  private static final long serialVersionUID = 7999349137348568641L;
  private PatterningPassport patterningPassport;
  private String filename;
  private String fullFileSystemPath;

  //use for User objects
  String correspondingUsername;

  //Use for Story objects
  String correspondingStoryID;

  /*
  private String thumbnailFilename;
  private String thumbnailFullFileSystemPath;
  */

  private static String type = "SinglePhotoVideo";
  private String copyrightOwner;
  private String copyrightStatement;
  private List<Keyword> keywords;

  //
  // username of person who submitted the photo
  //
  private String submitter;

  /**
   * Empty constructor required for JDO persistence
   */
  public SinglePhotoVideo(){}

  /*
   * Required constructor for instance creation
   */
  public SinglePhotoVideo(final String correspondingEncounterNumber, final String filename, final String fullFileSystemPath) {
    super(correspondingEncounterNumber, type);
    this.filename = filename;
    this.fullFileSystemPath = fullFileSystemPath;
  }

  public SinglePhotoVideo(final String correspondingEncounterNumber, final File file) {
    super(correspondingEncounterNumber, type);
    this.filename = file.getName();
    this.fullFileSystemPath = file.getAbsolutePath();
  }

    public SinglePhotoVideo(final Encounter enc, final FileItem formFile, final String context, final String dataDir) throws Exception {
//TODO FUTURE: should use context to find out METHOD of storage (e.g. remote, amazon, etc) and switch accordingly?
    super(enc.getEncounterNumber(), type);

        String encID = enc.getEncounterNumber();
        if ((encID == null) || encID.equals("")) {
            throw new Exception("called SinglePhotoVideo(enc) with Encounter missing an ID");
        }

        //TODO generalize this when we encorporate METHOD?
        //File dir = new File(dataDir + File.separator + correspondingEncounterNumber.charAt(0) + File.separator + correspondingEncounterNumber.charAt(1), correspondingEncounterNumber);
        File dir = new File(enc.dir(dataDir));
        if (!dir.exists()) { dir.mkdirs(); }

        //String origFilename = new File(formFile.getName()).getName();
        this.filename = ServletUtilities.cleanFileName(new File(formFile.getName()).getName());

        File file = new File(dir, this.filename);
    this.fullFileSystemPath = file.getAbsolutePath();
        formFile.write(file);  //TODO catch errors and return them, duh
System.out.println("full path??? = " + this.fullFileSystemPath + " WRITTEN!");
    }

  /**
   * Returns the photo or video represented by this object as a java.io.File
   * This is a convenience method.
   * @return java.io.File
   */
  public File getFile() {
    if(fullFileSystemPath!=null){
        return (new File(fullFileSystemPath));
    }
    else{return null;}
  }


    public String asUrl(final String context) {
        return getUrl(context, this.fullFileSystemPath, this.filename);
    }

    //old way, relied on being encounter-based  USE ABOVE!
    public String asUrl(final Encounter enc, final String baseDir) {
    System.out.println("*** OLD SinglePhotoVideo.asUrl(enc, baseDir) being called! please update to .asUrl(context)");
        return "/" + enc.dir(baseDir) + "/" + this.filename;
    }


/*
    public String asUrl(String context) {
        String baseDir = CommonConfiguration.getDataDirectoryName(context);
        return this.fullDir().toString() + ":::baseDir=("+baseDir+")";
    }
*/

    public static String getUrl(final String context, final String fullFileSystemPath, final String filename)
    {
        return getUrlDir(context, fullFileSystemPath) + "/" + filename;
    }


    private static String getUrlDir(final String context, final String fullFileSystemPath)
    {
        if (fullFileSystemPath == null) {
            return null;
        }

        File dir = new File(fullFileSystemPath).getParentFile();

        String baseDir = CommonConfiguration.getDataDirectoryName(context);
        int index = dir.toString().indexOf(baseDir);
        if (index < 0) {
            System.out.println("weird, SinglePhotoVideo.urlDir() could not find baseDir=" + baseDir + " in fullDir=" + dir.toString());
            return dir.toString();
        }
        if (index == 0) {
            index = 1;  //"should never happen", but meh
        }
        return dir.toString().substring(index - 1);
    }

    public String urlDir(final String context) {
        return getUrlDir(context, this.fullFileSystemPath);
    }

    public File fullDir() {
        if (this.fullFileSystemPath == null) {
            return null;
        }
        return new File(this.fullFileSystemPath).getParentFile();
    }

  /*
  public File getThumbnailFile(){
    if(thumbnailFullFileSystemPath!=null){
        return (new File(thumbnailFullFileSystemPath));
    }
    else{return null;}
  }
  */

  public String getFilename(){return filename;}
  public void setFilename(final String newName){this.filename=newName;}

  public String getFullFileSystemPath(){return fullFileSystemPath;}
  public void setFullFileSystemPath(final String newPath){this.fullFileSystemPath=newPath;}

  public String getCopyrightOwner(){return copyrightOwner;}
  public void setCopyrightOwner(final String owner){copyrightOwner=owner;}

  public String getCopyrightStatement(){return copyrightStatement;}
  public void setCopyrightStatement(final String statement){copyrightStatement=statement;}

   //public String getThumbnailFilename(){return (this.getDataCollectionEventID()+".jpg");}

  /*
  public void setThumbnailFilename(String newName){this.thumbnailFilename=newName;}

  public String getThumbnailFullFileSystemPath(){return thumbnailFullFileSystemPath;}
  public void setThumbnailFullFileSystemPath(String newPath){this.thumbnailFullFileSystemPath=newPath;}
  */

  public void addKeyword(final Keyword dce){
    if(keywords==null){keywords=new ArrayList<Keyword>();}
    if(!keywords.contains(dce)){keywords.add(dce);}
  }
  public void removeKeyword(final int num){keywords.remove(num);}
  public List<Keyword> getKeywords(){return keywords;}
  public void removeKeyword(final Keyword num){keywords.remove(num);}

  public PatterningPassport getPatterningPassport() {
    if (patterningPassport == null) {
      patterningPassport = new PatterningPassport();
    }
    return patterningPassport;
  }

  public File getPatterningPassportFile() {
    File f = this.getFile();
    String xmlPath;
    String dirPath;
    if (f != null) {
      dirPath = f.getParent();
      xmlPath = dirPath + "/" + this.filename.substring(0,this.filename.indexOf(".")) + "_pp.xml";
    } else {
      return null; // no xml if no image!
    }

    File xmlFile = new File(xmlPath);
    if (xmlFile.isFile() == Boolean.FALSE) {
      return null;
    }

    return xmlFile;
  }

  /**
   * @param patterningPassport the patterningPassport to set
   */
  public void setPatterningPassport(final PatterningPassport patterningPassport) {
    this.patterningPassport = patterningPassport;
  }

  public String getCorrespondingUsername(){return correspondingUsername;}
  public void setCorrespondingUsername(final String username){this.correspondingUsername=username;}

  public String getCorrespondingStoryID(){return correspondingStoryID;}
  public void setCorrespondingStoryID(final String userID){this.correspondingStoryID=userID;}


    //background scaling of the image to some target path
    // true = doing it (background); false = cannot do it (no external command support; not image)
    public boolean scaleTo(final String context, final int width, final int height, final String targetPath) {
        String cmd = CommonConfiguration.getProperty("imageResizeCommand", context);

        if ((cmd == null) || cmd.equals("")) {
            return false;
        }

        String sourcePath = this.getFullFileSystemPath();
        if (!Shepherd.isAcceptableImageFile(sourcePath)) {
            return false;
        }

        ImageProcessor iproc = new ImageProcessor(context, "resize", width, height, sourcePath, targetPath, null);
        Thread t = new Thread(iproc);
        t.start();
        return true;
    }

    public boolean scaleToWatermark(final String context, final int width, final int height, final String targetPath, final String watermark) {
        String cmd = CommonConfiguration.getProperty("imageWatermarkCommand", context);
        if ((cmd == null) || cmd.equals("")) {
            return false;
        }

        String sourcePath = this.getFullFileSystemPath();
        if (!Shepherd.isAcceptableImageFile(sourcePath)) {
            return false;
        }

        ImageProcessor iproc = new ImageProcessor(context, "watermark", width, height, sourcePath, targetPath, watermark);
        Thread t = new Thread(iproc);
        t.start();
        return true;
    }

    @Override
    public boolean equals(final Object that) {
        if (that == null) return false;
        if (this == that) return true;
        if (!(that instanceof SinglePhotoVideo)) return false;
        SinglePhotoVideo spv = (SinglePhotoVideo)that;
        if (this.getDataCollectionEventID() == null) return false;
        return this.getDataCollectionEventID().equals(spv.getDataCollectionEventID());
    }

    @Override
    public int hashCode() {
        if (this.getDataCollectionEventID() == null) return -1;
        return this.getDataCollectionEventID().hashCode();
    }

    public String getSubmitter() {
        return submitter;
    }

    public void setSubmitter(final String submitter) {
        this.submitter = submitter;
    }

}
