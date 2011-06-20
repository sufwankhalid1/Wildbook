package org.ecocean;

import org.apache.wicket.PageParameters;
import org.apache.wicket.authentication.pages.SignInPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;

public class Login extends SignInPage {
  public Login() {
    super();
    add(new Template("template"));
    add(new Label("databaseLogin", new ResourceModel("databaseLogin")));
    add(new Label("requested", new ResourceModel("requested")));
  }
  public Login(final PageParameters parameters) {
    super(parameters);
    add(new Template("template"));
    add(new Label("databaseLogin", new ResourceModel("databaseLogin")));
    add(new Label("requested", new ResourceModel("requested")));
  }
}
