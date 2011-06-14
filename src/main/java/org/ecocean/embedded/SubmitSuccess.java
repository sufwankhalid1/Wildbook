package org.ecocean.embedded;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;

public class SubmitSuccess extends WebPage {
  public SubmitSuccess(final PageParameters parameters) {
    add(new Label("thanks", new ResourceModel("thanks")));
  }
}
