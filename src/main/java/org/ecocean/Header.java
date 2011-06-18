package org.ecocean;

import org.apache.wicket.Localizer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.protocol.http.WebApplication;

public class Header extends Panel {
  private static final long serialVersionUID = 6293159062294318676L;
  
  public Header(String id) {
    super(id);
    //TODO: parameterize masthead image
    add(new Label("home", new ResourceModel("home")));
    add(new Label("learn", new ResourceModel("learn")));
    add(new Label("intro", new ResourceModel("intro")));
    add(new Label("participate", new ResourceModel("participate")));
    add(new Label("report", new ResourceModel("report")));
    add(new Label("individuals", new ResourceModel("individuals")));
    add(new Label("viewAll", new ResourceModel("viewAll")));
    add(new Label("viewEncounters", new ResourceModel("viewEncounters")));
    add(new Label("viewImages", new ResourceModel("viewImages")));
    add(new Label("encounterCalendar", new ResourceModel("encounterCalendar")));
    add(new Label("search", new ResourceModel("search")));
    add(new Label("encounters", new ResourceModel("encounters")));
    add(new Label("encounterSearch", new ResourceModel("encounterSearch")));
    add(new Label("individualSearch", new ResourceModel("individualSearch")));
    add(new Label("googleSearch", new ResourceModel("googleSearch")));
    add(new Label("administer", new ResourceModel("administer")));
    add(new Label("contactUs", new ResourceModel("contactUs")));
    WebMarkupContainer adminEncountersFragment = new WebMarkupContainer("adminEncountersFragment"); 
    adminEncountersFragment.add(new Label("viewUnapproved", new ResourceModel("viewUnapproved")));
    adminEncountersFragment.add(new Label("viewMySubmissions", new ResourceModel("viewMySubmissions")));
    adminEncountersFragment.add(new Label("viewUnidentifiable", new ResourceModel("viewUnidentifiable")));
    WebMarkupContainer wikiLinksFragment = new WebMarkupContainer("wikiLinksFragment");
    wikiLinksFragment.add(new Label("accessPolicy", new ResourceModel("accessPolicy")));
    wikiLinksFragment.add(new Label("userWiki", new ResourceModel("userWiki")));
    WebMarkupContainer appAdminFragment = new WebMarkupContainer("appAdminFragment");
    appAdminFragment.add(new Label("general", new ResourceModel("general")));
    WebMarkupContainer scanTaskAdminFragment = new WebMarkupContainer("scanTaskAdminFragment");
    scanTaskAdminFragment.add(new Label("gridAdmin", new ResourceModel("grid")));
    WebMarkupContainer welcomeFragment = new WebMarkupContainer("welcomeFragment");
    welcomeFragment.add(new Label("login", new ResourceModel("login")));
    WebMarkupContainer logoutFragment = new WebMarkupContainer("logoutFragment");
    logoutFragment.add(new Label("logout", new ResourceModel("logout")));
    logoutFragment.setVisible(false);
    
    add(adminEncountersFragment);
    add(wikiLinksFragment);
    add(appAdminFragment);
    add(scanTaskAdminFragment);
    add(welcomeFragment);
    add(logoutFragment);
 }

}
