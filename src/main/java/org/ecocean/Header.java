package org.ecocean;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;

public class Header extends Panel {
  private static final long serialVersionUID = 6293159062294318676L;
  
  public Header(String id) {
    super(id);
    //TODO: parameterize masthead image
    BookmarkablePageLink<Index> indexLink = new BookmarkablePageLink<Index>("linkToIndex", Index.class); 
    indexLink.add(new Label("home", new ResourceModel("home")));
    add(indexLink);

    BookmarkablePageLink<Index> learnLink = new BookmarkablePageLink<Index>("linkToLearn", Index.class); 
    learnLink.add(new Label("learn", new ResourceModel("learn")));
    add(learnLink);

    BookmarkablePageLink<Index> introLink = new BookmarkablePageLink<Index>("linkToIntro", Index.class); 
    introLink.add(new Label("intro", new ResourceModel("intro")));
    add(introLink);
    
    BookmarkablePageLink<Submit> submitLink = new BookmarkablePageLink<Submit>("linkToSubmit", Submit.class); 
    submitLink.add(new Label("participate", new ResourceModel("participate")));
    add(submitLink);

    BookmarkablePageLink<Submit> reportLink = new BookmarkablePageLink<Submit>("linkToReport", Submit.class); 
    reportLink.add(new Label("report", new ResourceModel("report")));
    add(reportLink);

    BookmarkablePageLink<Index> individualsLink = new BookmarkablePageLink<Index>("linkToIndividuals", Index.class); 
    individualsLink.add(new Label("individuals", new ResourceModel("individuals")));
    add(individualsLink);

    BookmarkablePageLink<Index> allIndividualsLink = new BookmarkablePageLink<Index>("linkToAllIndividuals", Index.class); 
    allIndividualsLink.add(new Label("viewAll", new ResourceModel("viewAll")));
    add(allIndividualsLink);

    BookmarkablePageLink<Index> encountersLink = new BookmarkablePageLink<Index>("linkToEncounters", Index.class); 
    encountersLink.add(new Label("encounters", new ResourceModel("encounters")));
    add(encountersLink);

    BookmarkablePageLink<Index> allEncountersLink = new BookmarkablePageLink<Index>("linkToAllEncounters", Index.class); 
    allEncountersLink.add(new Label("viewEncounters", new ResourceModel("viewEncounters")));
    add(allEncountersLink);

    BookmarkablePageLink<Index> viewImagesLink = new BookmarkablePageLink<Index>("linkToViewImages", Index.class); 
    viewImagesLink.add(new Label("viewImages", new ResourceModel("viewImages")));
    add(viewImagesLink);

    BookmarkablePageLink<Index> calendarLink = new BookmarkablePageLink<Index>("linkToCalendar", Index.class); 
    calendarLink.add(new Label("encounterCalendar", new ResourceModel("encounterCalendar")));
    add(calendarLink);

    WebMarkupContainer adminEncountersFragment = new WebMarkupContainer("adminEncountersFragment"); 
    BookmarkablePageLink<Index> unapprovedLink = new BookmarkablePageLink<Index>("linkToUnapproved", Index.class); 
    unapprovedLink.add(new Label("viewUnapproved", new ResourceModel("viewUnapproved")));
    adminEncountersFragment.add(unapprovedLink);

    BookmarkablePageLink<Index> mySubmissionsLink = new BookmarkablePageLink<Index>("linkToMySubmissions", Index.class); 
    mySubmissionsLink.add(new Label("viewMySubmissions", new ResourceModel("viewMySubmissions")));
    adminEncountersFragment.add(mySubmissionsLink);

    BookmarkablePageLink<Index> unidentifiableLink = new BookmarkablePageLink<Index>("linkToUnidentifiable", Index.class); 
    unidentifiableLink.add(new Label("viewUnidentifiable", new ResourceModel("viewUnidentifiable")));
    adminEncountersFragment.add(unidentifiableLink);

    BookmarkablePageLink<Index> searchLink = new BookmarkablePageLink<Index>("linkToSearch", Index.class); 
    searchLink.add(new Label("search", new ResourceModel("search")));
    add(searchLink);

    BookmarkablePageLink<Index> encounterSearchLink = new BookmarkablePageLink<Index>("linkToEncounterSearch", Index.class); 
    encounterSearchLink.add(new Label("encounterSearch", new ResourceModel("encounterSearch")));
    add(encounterSearchLink);

    BookmarkablePageLink<Index> individualSearchLink = new BookmarkablePageLink<Index>("linkToIndividualSearch", Index.class); 
    individualSearchLink.add(new Label("individualSearch", new ResourceModel("individualSearch")));
    add(individualSearchLink);

    BookmarkablePageLink<Index> googleSearchLink = new BookmarkablePageLink<Index>("linkToGoogleSearch", Index.class); 
    googleSearchLink.add(new Label("googleSearch", new ResourceModel("googleSearch")));
    add(googleSearchLink);

    BookmarkablePageLink<Index> adminLink = new BookmarkablePageLink<Index>("linkToAdmin", Index.class); 
    adminLink.add(new Label("administer", new ResourceModel("administer")));
    add(adminLink);

    WebMarkupContainer wikiLinksFragment = new WebMarkupContainer("wikiLinksFragment");
    
    BookmarkablePageLink<Index> accessPolicyLink = new BookmarkablePageLink<Index>("linkToAccessPolicy", Index.class); 
    accessPolicyLink.add(new Label("accessPolicy", new ResourceModel("accessPolicy")));
    wikiLinksFragment.add(accessPolicyLink);

    BookmarkablePageLink<Index> userWikiLink = new BookmarkablePageLink<Index>("linkToUserWiki", Index.class); 
    userWikiLink.add(new Label("userWiki", new ResourceModel("userWiki")));
    wikiLinksFragment.add(userWikiLink);

    WebMarkupContainer appAdminFragment = new WebMarkupContainer("appAdminFragment");
    BookmarkablePageLink<Index> generalAdminLink = new BookmarkablePageLink<Index>("linkToGeneralAdmin", Index.class); 
    generalAdminLink.add(new Label("general", new ResourceModel("general")));
    appAdminFragment.add(generalAdminLink);

    WebMarkupContainer gridAdminFragment = new WebMarkupContainer("gridAdminFragment");
    BookmarkablePageLink<Index> gridAdminLink = new BookmarkablePageLink<Index>("linkToGridAdmin", Index.class); 
    gridAdminLink.add(new Label("gridAdmin", new ResourceModel("grid")));
    gridAdminFragment.add(gridAdminLink);

    BookmarkablePageLink<Index> contactLink = new BookmarkablePageLink<Index>("linkToContact", Index.class); 
    contactLink.add(new Label("contactUs", new ResourceModel("contactUs")));
    add(contactLink);

    WebMarkupContainer welcomeFragment = new WebMarkupContainer("welcomeFragment");
    BookmarkablePageLink<Index> welcomeLink = new BookmarkablePageLink<Index>("linkToWelcome", Index.class); 
    welcomeLink.add(new Label("login", new ResourceModel("login")));
    welcomeFragment.add(welcomeLink);

    WebMarkupContainer logoutFragment = new WebMarkupContainer("logoutFragment");
    BookmarkablePageLink<Index> logoutLink = new BookmarkablePageLink<Index>("linkToLogout", Index.class); 
    logoutLink.add(new Label("logout", new ResourceModel("logout")));
    logoutFragment.add(logoutLink);
    //TODO: based on logged in state
    logoutFragment.setVisible(false);
    
    add(adminEncountersFragment);
    add(wikiLinksFragment);
    add(appAdminFragment);
    add(gridAdminFragment);
    add(welcomeFragment);
    add(logoutFragment);
 }

}
