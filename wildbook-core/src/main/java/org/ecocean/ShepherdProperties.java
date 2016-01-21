package org.ecocean;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ShepherdProperties {

    public static Properties getProperties(final String fileName) throws IOException {
        return getProperties(fileName, "en");
    }

    public static Properties getProperties(final String fileName, final String langCode) throws IOException {
        return getProperties(fileName, langCode, "context0");
    }

    public static Properties getProperties(final String fileName, String langCode, final String context) throws IOException {
        Properties props=new Properties();

        if (!langCode.equals("")) {
            langCode = langCode + "/";
        }

         try (InputStream inputStream = ShepherdProperties.class.getResourceAsStream("/bundles/" + langCode + fileName)) {
             props.load(inputStream);
         } catch (IOException ioe) {
             ioe.printStackTrace();
         }

        return props;
    }
}
