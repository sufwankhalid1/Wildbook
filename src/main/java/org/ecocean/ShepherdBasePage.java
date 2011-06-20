package org.ecocean;

import org.apache.wicket.markup.html.WebPage;

public class ShepherdBasePage extends WebPage {
  public ShepherdBasePage() {
    getSession().bind();
    add(new Template("template"));
  }
}
