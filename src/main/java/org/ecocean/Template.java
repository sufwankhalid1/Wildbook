package org.ecocean;

import org.apache.wicket.markup.html.WebPage;

public class Template extends WebPage {
  public Template() {
    add(new Header("header"));
  }
}
