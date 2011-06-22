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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.apache.wicket.Localizer;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.MinimumValidator;
import org.apache.wicket.validation.validator.RangeValidator;
import org.ecocean.model.Encounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: mmcbride
 * Date: 3/19/11
 * Time: 5:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class Submit extends ShepherdBasePage {
  private Shepherd myShepherd = new Shepherd();
  private Logger log = LoggerFactory.getLogger(Submit.class);

  public Submit(final PageParameters parameters) {
    super();
    add(new Label("submit_report", new ResourceModel("submit_report")));
    add(new FeedbackPanel("feedback"));
    add(new EncounterForm("encounterForm"));
  }

  private List<Integer> makeList(int start, int end) {
    return makeList(start, end, 1);
  }
  
  private List<Integer> makeList(int start, int end, int interval) {
    ArrayList<Integer> days = new ArrayList<Integer>();
    for (int i = start; i <= end*interval; i += interval) {
      days.add(i);
    }
    return days;
  }

  private List<String> makeStringList(int start, int end, int interval) {
    ArrayList<String> days = new ArrayList<String>();
    for (int i = start; i <= end*interval; i += interval) {
      days.add("" + i);
    }
    return days;
  }

  private List<String> resourcesToList(String... args) {
    ArrayList<String> s = new ArrayList<String>();
    Localizer l = getLocalizer();
    for (String resource : args) {
      s.add(l.getString(resource, this));
    }
    return s;
  }
  @SuppressWarnings("serial")
  private class EncounterForm extends Form<Encounter> {
    private FileUploadField image1Field;
    private FileUploadField image2Field;
    private FileUploadField image3Field;
    private FileUploadField image4Field;

    public EncounterForm(String name) {
      super(name, new CompoundPropertyModel<Encounter>(new Encounter()));

      setMultiPart(true);

      add(new Label("overview", new ResourceModel("overview")));
      add(new Label("yearLabel", new ResourceModel("year")));
      add(new Label("monthLabel", new ResourceModel("month")));
      add(new Label("dateLabel", new ResourceModel("date")));
      add(new Label("dayLabel", new ResourceModel("day")));
      add(new Label("hourLabel", new ResourceModel("hour")));
      add(new Label("minuteLabel", new ResourceModel("minute")));
      add(new Label("lengthLabel", new ResourceModel("length")));
      add(new Label("unitsLabel", new ResourceModel("units")));
      add(new Label("useSameUnitsLabel", new ResourceModel("useSameUnits")));
      add(new Label("measurementMethodLabel", new ResourceModel("measurementMethod")));
      add(new Label("sexLabel", new ResourceModel("sex")));
      add(new Label("locationLabel", new ResourceModel("location")));
      add(new Label("gpsLatitudeLabel", new ResourceModel("gpsLatitude")));
      add(new Label("gpsLongitudeLabel", new ResourceModel("gpsLongitude")));
      add(new Label("gpsConversionLabel", new ResourceModel("gpsConversion")));
      add(new Label("gpsClickTextLabel", new ResourceModel("gpsClickText")));
      add(new Label("seaFloorDepthLabel", new ResourceModel("seaFloorDepth")));
      add(new Label("statusLabel", new ResourceModel("status")));
      add(new Label("scarringLabel", new ResourceModel("scarring")));
      add(new Label("commentsLabel", new ResourceModel("comments")));
      add(new Label("yourContactLabel", new ResourceModel("yourContact")));
      add(new Label("submitterNameLabel", new ResourceModel("submitterName")));
      add(new Label("submitterEmailLabel", new ResourceModel("submitterEmail")));
      add(new Label("submitterAddressLabel", new ResourceModel("submitterAddress")));
      add(new Label("submitterTelephoneLabel", new ResourceModel("submitterTelephone")));
      add(new Label("photoContactLabel", new ResourceModel("photoContact")));
      add(new Label("photoContactSubnoteLabel", new ResourceModel("photoContactSubnote")));
      add(new Label("photographerNameLabel", new ResourceModel("photographerName")));
      add(new Label("photographerEmailLabel", new ResourceModel("photographerEmail")));
      add(new Label("photographerAddressLabel", new ResourceModel("photographerAddress")));
      add(new Label("photographerTelephoneLabel", new ResourceModel("photographerTelephone")));
      add(new Label("otherEmailAddressesLabel", new ResourceModel("otherEmailAddresses")));
      add(new Label("emailSeparatorNoteLabel", new ResourceModel("emailSeparatorNote")));
      add(new Label("image1Label", new ResourceModel("image1")));
      add(new Label("image2Label", new ResourceModel("image2")));
      add(new Label("image3Label", new ResourceModel("image3")));
      add(new Label("image4Label", new ResourceModel("image4")));

      DropDownChoice<Integer> dayChoice = new DropDownChoice<Integer>("day", makeList(1, 31));
      dayChoice.setRequired(true);
      add(dayChoice);
      DropDownChoice<Integer> monthChoice = new DropDownChoice<Integer>("month", makeList(1, 12));
      monthChoice.setRequired(true);
      add(monthChoice);
      DropDownChoice<Integer> yearChoice = new DropDownChoice<Integer>("year", makeList(2010, 2050));
      yearChoice.setRequired(true);
      add(yearChoice);
      add(new DropDownChoice<Integer>("hour", makeList(0, 23), new IChoiceRenderer<Integer>() {
        public String getDisplayValue(Integer i) {
          boolean isPm = i > 13;
          int displayHour = isPm ? i - 12 : i;
          String amPm = isPm ? "pm" : "am";
          if (i == 0) {
            return "12 am";
          } else {
            return displayHour + " " + amPm;
          }
        }
        public String getIdValue(Integer object, int index) {
          return object.toString();
        }
      }));
      add(new DropDownChoice<String>("minutes", makeStringList(0, 3, 15), new IChoiceRenderer<String>() {
        public String getDisplayValue(String i) {
          return ":" + i;
        }
        public String getIdValue(String object, int index) {
          return object.toString();
        }
      }));
      TextField<Double> sizeField = new TextField<Double>("size");
      sizeField.add(new MinimumValidator<Double>(0.0));
      add(sizeField);
      add(new RadioChoice<String>("measureUnits", resourcesToList("feet", "meters")));
      add(new RadioChoice<String>("sizeGuess", resourcesToList("personalGuess", "researcherGuess", "directMeasurement")));
      add(new RadioChoice<String>("sex", resourcesToList("male", "female")));
      TextField<String> locationField = new TextField<String>("location");
      locationField.setRequired(true);
      add(locationField);
      TextField<Double> gpsLatitudeField = new TextField<Double>("gpsLatitude");
      gpsLatitudeField.add(new RangeValidator<Double>(-90.0, 90.0));
      add(gpsLatitudeField);
      TextField<Double> gpsLongitudeField = new TextField<Double>("gpsLongitude");
      gpsLongitudeField.add(new RangeValidator<Double>(-180.0, 180.0));
      add(gpsLongitudeField);
      TextField<Double> maximumDepthField = new TextField<Double>("maximumDepthInMeters");
      maximumDepthField.add(new MinimumValidator<Double>(0.0));
      add(maximumDepthField);
      add(new RadioChoice<String>("livingStatus", resourcesToList("alive", "dead")));
      add(new TextField<String>("distinguishingScar"));
      add(new TextArea<String>("comments"));
      TextField<String> submitterNameField = new TextField<String>("submitterName");
      submitterNameField.setRequired(true);
      add(submitterNameField);
      TextField<String> submitterEmailField = new TextField<String>("submitterEmail");
      submitterEmailField.setRequired(true);
      submitterEmailField.add(EmailAddressValidator.getInstance());
      add(submitterEmailField);      
      add(new TextField<String>("submitterAddress"));
      add(new TextField<String>("submitterPhone"));      
      add(new TextField<String>("photographerName"));
      TextField<String> photographerEmailField = new TextField<String>("photographerEmail");
      photographerEmailField.add(EmailAddressValidator.getInstance());
      add(photographerEmailField);      
      add(new TextField<String>("photographerAddress"));
      add(new TextField<String>("photographerPhone"));      
      add(new TextField<List<String>>("interestedResearchers") {
        @Override
        public IConverter getConverter(Class<?> type) {
          return new IConverter() {
            public Object convertToObject(String s, Locale l) {
              return Arrays.asList(s.split(","));
            }
            public String convertToString(Object o, Locale l) {
              StringBuffer sb = new StringBuffer();
              @SuppressWarnings("unchecked")
              List<String> list = (List<String>)o;
              for (String s : list) {
                sb.append(s).append(", ");
              }
              return sb.toString();
            }
          };
        }
      });      
      add(image1Field = new FileUploadField("image1"));
      add(image2Field = new FileUploadField("image2"));
      add(image3Field = new FileUploadField("image3"));
      add(image4Field = new FileUploadField("image4"));
      Button submit = new Button("submit");
      add(submit);
    }
    
    protected boolean validate(Encounter encounter) {
      log.info("Starting data submission...");
      if (encounter.getSize() == null) {
        String errMsg = getLocalizer().getString("no_length", Submit.this);
        error(errMsg);
        return false;
      } else if (encounter.getSize() < 0) {
        String errMsg = getLocalizer().getString("pos_length", Submit.this);
        error(errMsg);
        return false;
      }
      return true;
    }

    protected boolean checkSpamBot(Encounter encounter) {
      StringBuffer spamFields = new StringBuffer();
      boolean spamBot = false;
      spamFields.append(encounter.getSubmitterPhone());
      spamFields.append(encounter.getSubmitterName());
      spamFields.append(encounter.getPhotographerPhone());
      spamFields.append(encounter.getPhotographerName());
      spamFields.append(encounter.getLocation());
      if (spamFields.toString().toLowerCase().indexOf("porn") != -1) {
        spamBot = true;
      }
      spamFields.append(encounter.getComments());
      if (spamFields.toString().toLowerCase().indexOf("href") != -1) {
        spamBot = true;
      }
      return spamBot;
    }

    protected void onSubmit() {
      try {
        Encounter model = getModelObject();
        if (validate(model) && !checkSpamBot(model)) {
          setResponsePage(SubmitSuccess.class);
          //TODO: location translation
          //TODO: use a real GUID
          Calendar date = Calendar.getInstance();
          Random ran = new Random();
          String uniqueID = (new Integer(date.get(Calendar.DAY_OF_MONTH))).toString() + (new Integer(date.get(Calendar.MONTH) + 1)).toString() + (new Integer(date.get(Calendar.YEAR))).toString() + (new Integer(date.get(Calendar.HOUR_OF_DAY))).toString() + (new Integer(date.get(Calendar.MINUTE))).toString() + (new Integer(date.get(Calendar.SECOND))).toString() + (new Integer(ran.nextInt(99))).toString();
          String encountersDirName = ((ShepherdApplication)getApplication()).getEncounterStorageDir();
          File encountersDir = new File(encountersDirName);
          File thisEncounterDir = new File(encountersDir, uniqueID);
          List<FileUploadField> uploadFields = Arrays.asList(new FileUploadField[]{image1Field, image2Field, image3Field, image4Field});
          for (FileUploadField uploadField : uploadFields) {
            FileUpload upload = uploadField.getFileUpload();
            if (!thisEncounterDir.exists()) {
              thisEncounterDir.mkdirs();
            }
            if (upload != null) {
              File targetFile = new File(thisEncounterDir, upload.getClientFileName());
              model.addAdditionalImageName(targetFile.getCanonicalPath());
              upload.writeTo(targetFile);
            }
          }
          myShepherd.storeNewEncounter(model, uniqueID);
          myShepherd.closeDBTransaction();
          log.info("successfully saved record");
          info("successfully saved record");
          setResponsePage(SubmitSuccess.class);
        }
      } catch(Exception e) {
        error("caught exception: " + e);
        e.printStackTrace();
      }
    }
  }
}
