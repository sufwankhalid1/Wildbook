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

import org.apache.wicket.IClusterable;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: mmcbride
 * Date: 3/20/11
 * Time: 10:34 PM
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class EncounterFormModel implements IClusterable {
  private int day;
  private int month;
  private int year;
  private int hour;
  private int minutes;
  private int length;
  private String units;
  private String guess;
  private String location;
  private String submitterName;
  private String submitterEmail;
  private Vector<String> additionalImageNames;
  
  public int getDay() {
    return day;
  }

  public void setDay(int day) {
    this.day = day;
  }

  public int getMonth() {
    return month;
  }

  public void setMonth(int month) {
    this.month = month;
  }

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public int getHour() {
    return hour;
  }

  public void setHour(int hour) {
    this.hour = hour;
  }

  public int getMinutes() {
    return minutes;
  }

  public void setMinutes(int minutes) {
    this.minutes = minutes;
  }

  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  public String getUnits() {
    return units;
  }

  public void setUnits(String units) {
    this.units = units;
  }

  public String getGuess() {
    return guess;
  }

  public void setGuess(String guess) {
    this.guess = guess;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getSubmitterName() {
    return submitterName;
  }

  public void setSubmitterName(String submitterName) {
    this.submitterName = submitterName;
  }

  public String getSubmitterEmail() {
    return submitterEmail;
  }

  public void setSubmitterEmail(String submitterEmail) {
    this.submitterEmail = submitterEmail;
  }

  public Vector<String> getAdditionalImageNames() {
    return additionalImageNames;
  }

  public void setAdditionalImageNames(Vector<String> additionalImageNames) {
    this.additionalImageNames = additionalImageNames;
  }
}
