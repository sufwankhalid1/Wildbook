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

package org.ecocean.embedded;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Locale;

import org.apache.wicket.Localizer;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.convert.IConverter;
import org.ecocean.Encounter;
import org.ecocean.Shepherd;

/**
 * Created by IntelliJ IDEA.
 * User: mmcbride
 * Date: 3/19/11
 * Time: 5:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class Submit extends WebPage {
  //private Shepherd myShepherd = new Shepherd();

  public Submit(final PageParameters parameters) {
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
    public EncounterForm(String name) {
      super(name, new CompoundPropertyModel<Encounter>(new Encounter()));

      Localizer l = getLocalizer();
      
      add(new Label("overview", new ResourceModel("overview")));
      add(new Label("yearLabel", new ResourceModel("year")));
      add(new Label("monthLabel", new ResourceModel("month")));
      add(new Label("dayLabel", new ResourceModel("day")));
      add(new Label("hourLabel", new ResourceModel("hour")));
      add(new Label("minuteLabel", new ResourceModel("minute")));
      add(new Label("lengthLabel", new ResourceModel("length")));
      add(new Label("unitsLabel", new ResourceModel("units")));
      add(new Label("measurementMethodLabel", new ResourceModel("measurementMethod")));
      add(new Label("sexLabel", new ResourceModel("sex")));
      add(new Label("locationLabel", new ResourceModel("location")));
      add(new Label("gpsLatitudeLabel", new ResourceModel("gpsLatitude")));
      add(new Label("gpsLongitudeLabel", new ResourceModel("gpsLongitude")));
      add(new Label("seaFloorDepthLabel", new ResourceModel("seaFloorDepth")));
      add(new Label("statusLabel", new ResourceModel("status")));
      add(new Label("scarringLabel", new ResourceModel("scarring")));
      add(new Label("commentsLabel", new ResourceModel("comments")));
      add(new Label("submitterNameLabel", new ResourceModel("submitterName")));
      add(new Label("submitterEmailLabel", new ResourceModel("submitterEmail")));
      add(new Label("submitterAddressLabel", new ResourceModel("submitterAddress")));
      add(new Label("submitterTelephoneLabel", new ResourceModel("submitterTelephone")));
      add(new Label("photographerNameLabel", new ResourceModel("photographerName")));
      add(new Label("photographerEmailLabel", new ResourceModel("photographerEmail")));
      add(new Label("photographerAddressLabel", new ResourceModel("photographerAddress")));
      add(new Label("photographerTelephoneLabel", new ResourceModel("photographerTelephone")));
      add(new Label("otherEmailAddressesLabel", new ResourceModel("otherEmailAddresses")));

      add(new DropDownChoice<Integer>("day", makeList(1, 31)));
      add(new DropDownChoice<Integer>("month", makeList(1, 12)));
      add(new DropDownChoice<Integer>("year", makeList(2010, 2050)));
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
      add(new TextField<Double>("size"));
      add(new RadioChoice<String>("measureUnits", resourcesToList("feet", "meters")));
      add(new RadioChoice<String>("sizeGuess", resourcesToList("personalGuess", "researcherGuess", "directMeasurement")));
      add(new RadioChoice<String>("sex", resourcesToList("male", "female")));
      add(new TextField<String>("location"));
      add(new TextField<Double>("gpsLatitude"));
      add(new TextField<Double>("gpsLongitude"));
      add(new TextField<Double>("maximumDepthInMeters"));
      add(new RadioChoice<String>("livingStatus", resourcesToList("alive", "dead")));
      add(new TextField<String>("distinguishingScar"));
      add(new TextArea<String>("comments"));
      add(new TextField<String>("submitterName"));
      add(new TextField<String>("submitterEmail"));      
      add(new TextField<String>("submitterAddress"));
      add(new TextField<String>("submitterPhone"));      
      add(new TextField<String>("photographerName"));
      add(new TextField<String>("photographerEmail"));      
      add(new TextField<String>("photographerAddress"));
      add(new TextField<String>("photographerPhone"));      
      add(new TextField<List<String>>("interestedResearchers") {
        @Override
        public IConverter getConverter(Class type) {
          return new IConverter() {
            public Object convertToObject(String s, Locale l) {
              return Arrays.asList(s.split(","));
            }
            public String convertToString(Object o, Locale l) {
              StringBuffer sb = new StringBuffer();
              List<String> list = (List<String>)o;
              for (String s : list) {
                sb.append(s).append(", ");
              }
              return sb.toString();
            }
          };
        }
      });      
      add(new FileUploadField("image1"));
      Button submit = new Button("submit");
      add(submit);
    }
    
    protected void onSubmit() {
      try {
        Double length = getModelObject().getSize();
        info("submitting and stuff");
        if (length == null) {
          String errMsg = getLocalizer().getString("no_length", Submit.this);
          error(errMsg);
        } else if (length < 0) {
          String errMsg = getLocalizer().getString("pos_length", Submit.this);
          error(errMsg);
        } else {
          setResponsePage(SubmitSuccess.class);
        }
        //TODO: use a real GUID
        Calendar date = Calendar.getInstance();
        Random ran = new Random();
        String uniqueID = (new Integer(date.get(Calendar.DAY_OF_MONTH))).toString() + (new Integer(date.get(Calendar.MONTH) + 1)).toString() + (new Integer(date.get(Calendar.YEAR))).toString() + (new Integer(date.get(Calendar.HOUR_OF_DAY))).toString() + (new Integer(date.get(Calendar.MINUTE))).toString() + (new Integer(date.get(Calendar.SECOND))).toString() + (new Integer(ran.nextInt(99))).toString();
        //myShepherd.storeNewEncounter(enc, uniqueID);
        //myShepherd.closeDBTransaction();
        info("successfully saved record");
        System.out.println("WOW IT WORKED");
        setResponsePage(SubmitSuccess.class);
      } catch(Exception e) {
        error("caught exception: " + e);
        e.printStackTrace();
      }
    }
  }
}
