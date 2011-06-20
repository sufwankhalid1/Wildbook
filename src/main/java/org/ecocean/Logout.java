package org.ecocean;

import org.apache.wicket.PageParameters;
import org.apache.wicket.authentication.pages.SignOutPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;

public class Logout extends SignOutPage {
  public Logout() {
    this(PageParameters.NULL);
  }
  public Logout(final PageParameters parameters) {
    super(parameters);
    signOut();
    add(new Template("template"));
    add(new Label("bye", new ResourceModel("bye")));
  }

  protected void signOut() {
    ((ShepherdSession)getSession()).signOut();
  }
}
