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
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.apache.wicket.Localizer;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
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
  private Shepherd myShepherd = new Shepherd();
  
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

  private List<String> makeUnits() {
    ArrayList<String> units = new ArrayList<String>();
    Localizer l = getLocalizer();
    units.add(l.getString("meters",this));
    units.add(l.getString("feet",this));
    return units;
  }

  @SuppressWarnings("serial")
  private class EncounterForm extends Form<EncounterFormModel> {
    public EncounterForm(String name) {
      super(name, new CompoundPropertyModel<EncounterFormModel>(new EncounterFormModel()));
      add(new Label("overview", new ResourceModel("overview")));
      add(new Label("yearLabel", new ResourceModel("year")));
      add(new Label("monthLabel", new ResourceModel("month")));
      add(new Label("dayLabel", new ResourceModel("day")));
      add(new Label("hourLabel", new ResourceModel("hour")));
      add(new Label("minuteLabel", new ResourceModel("minute")));
      add(new Label("lengthLabel", new ResourceModel("length")));
      add(new Label("unitsLabel", new ResourceModel("units")));
      add(new Label("guessLabel", new ResourceModel("guess")));
      add(new Label("locationLabel", new ResourceModel("location")));
      add(new Label("submitterNameLabel", new ResourceModel("submitterName")));
      add(new Label("submitterEmailLabel", new ResourceModel("submitterEmail")));
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
      add(new DropDownChoice<Integer>("minutes", makeList(0, 3, 15), new IChoiceRenderer<Integer>() {
        public String getDisplayValue(Integer i) {
          return ":" + i;
        }
        public String getIdValue(Integer object, int index) {
          return object.toString();
        }
      }));
      add(new TextField<Integer>("length"));
      add(new TextField<Integer>("guess"));
      add(new TextField<Integer>("location"));
      add(new TextField<Integer>("submitterName"));
      add(new TextField<Integer>("submitterEmail"));
      add(new RadioChoice<String>("units", makeUnits()));
      Button submit = new Button("submit");
      add(submit);
    }
    
    protected void onSubmit() {
      try {
        Integer length = getModelObject().getLength();
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
        EncounterFormModel model = getModelObject();
        Encounter enc = new Encounter(model.getDay(),
            model.getMonth(),
            model.getYear(),
            model.getHour(),
            "" + model.getMinutes(),
            model.getGuess(),
            model.getLocation(),
            model.getSubmitterName(),
            model.getSubmitterEmail(),
            model.getAdditionalImageNames());
        //TODO: use a real GUID
        Calendar date = Calendar.getInstance();
        Random ran = new Random();
        String uniqueID = (new Integer(date.get(Calendar.DAY_OF_MONTH))).toString() + (new Integer(date.get(Calendar.MONTH) + 1)).toString() + (new Integer(date.get(Calendar.YEAR))).toString() + (new Integer(date.get(Calendar.HOUR_OF_DAY))).toString() + (new Integer(date.get(Calendar.MINUTE))).toString() + (new Integer(date.get(Calendar.SECOND))).toString() + (new Integer(ran.nextInt(99))).toString();
        myShepherd.storeNewEncounter(enc, uniqueID);
        myShepherd.closeDBTransaction();
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
