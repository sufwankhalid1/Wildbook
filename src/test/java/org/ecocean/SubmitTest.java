package org.ecocean;

import junit.framework.TestCase;

import org.apache.wicket.util.file.File;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;

public class SubmitTest extends TestCase {
  public void testRender() {
    WicketTester tester = new WicketTester(new ShepherdApplication());
    tester.startPage(Submit.class);
    FormTester ft = tester.newFormTester("encounterForm");
    ft.setValue("day", "1");
    ft.setValue("month", "1");
    ft.setValue("year", "1");
    ft.setValue("size", "10");
    ft.setValue("measureUnits", "feet");
    ft.setValue("location", "the WORLD");
    ft.setValue("submitterName", "mark");
    ft.setValue("submitterEmail", "mark@gmail.com");
    File image1 = new File(new java.io.File("src/test/resources/org/ecocean/masthead.jpg"));
    ft.setFile("image1", image1, "image/jpg");
    ft.submit();
    tester.assertRenderedPage(SubmitSuccess.class);
  }
}
