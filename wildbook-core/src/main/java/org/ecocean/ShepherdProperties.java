package org.ecocean;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

        String shepherdDataDir="shepherd_data_dir";

        if (!langCode.equals("")) {
            langCode=langCode+"/";
        }

        Properties contextsProps = getContextsProperties();
        if (contextsProps.getProperty(context + "DataDir") != null){
            shepherdDataDir=contextsProps.getProperty(context+"DataDir");
        }

        Properties overrideProps = loadOverrideProps(shepherdDataDir, fileName, langCode);

        if (overrideProps.size()>0) {
            props=overrideProps;
         } else {
             try (InputStream inputStream = ShepherdProperties.class.getResourceAsStream("/bundles/"+langCode+fileName)) {
                 props.load(inputStream);
             } catch (IOException ioe) {
                 ioe.printStackTrace();
             }
         }

        return props;
    }

    public static Properties getContextsProperties() {
        Properties props=new Properties();
        try (InputStream inputStream = ShepherdProperties.class.getResourceAsStream("/bundles/contexts.properties")) {
            props.load(inputStream);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return props;
    }

    private static Properties loadOverrideProps(final String shepherdDataDir,
                                                final String fileName,
                                                final String langCode) throws IOException {
        Properties myProps=new Properties();
        Path configDir = Paths.get("webapps", shepherdDataDir, "WEB-INF", "classes", "bundles", langCode);

        //sometimes this ends up being the "bin" directory of the J2EE container
        //we need to fix that
        if (configDir.toString().contains("/bin/") || configDir.toString().contains("\\bin\\")){
            String fixedPath = configDir.toString().replaceAll("/bin", "").replaceAll("\\\\bin", "");
            configDir = Paths.get(fixedPath);
        }
        if ((configDir.toString().contains("/logs/")) || (configDir.toString().contains("\\logs\\"))){
            String fixedPath = configDir.toString().replaceAll("/logs", "").replaceAll("\\\\logs", "");
            configDir = Paths.get(fixedPath);
        }

        if (!Files.exists(configDir)) {
            Files.createDirectories(configDir);
        }

        Path configFile = Paths.get(configDir.toString(), fileName);
        if (Files.exists(configFile)) {
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(configFile.toFile());
                myProps.load(fileInputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }

            finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }

        return myProps;
    }
}
