package org.ecocean.util;

import java.io.IOException;
import java.util.Map;

import org.ecocean.Global;

import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.exceptions.JadeCompilerException;
import de.neuland.jade4j.exceptions.JadeException;
import de.neuland.jade4j.template.ClasspathTemplateLoader;

public class Jade4JUtils {
    private static JadeConfiguration fileConfig;
    private static JadeConfiguration cpConfig;

    private Jade4JUtils() {
        // prevent instantiation
    }

    private static JadeConfiguration getCPConfig() {
        //
        // If you want to clear the cache you can call --> config.clearCache();
        //
        if (cpConfig == null) {
            cpConfig = new JadeConfiguration();
            //
            // For debug purposes you can disable the cache.
            //
            cpConfig.setCaching(Global.INST.getAppResources().getBoolean("email.jade.cache.enabled", true));
            cpConfig.setTemplateLoader(new ClasspathTemplateLoader());
        }

        return cpConfig;
    }

    private static JadeConfiguration getFileConfig() {
        //
        // If you want to clear the cache you can call --> config.clearCache();
        //
        if (fileConfig == null) {
            fileConfig = new JadeConfiguration();
            //
            // For debug purposes you can disable the cache.
            //
            fileConfig.setCaching(Global.INST.getAppResources().getBoolean("email.jade.cache.enabled", true));
        }

        return fileConfig;
    }


    public static String renderFile(final String template, final Map<String, Object> model)
            throws JadeCompilerException, JadeException, IOException
    {
        JadeConfiguration config = getFileConfig();
        return config.renderTemplate(config.getTemplate(template), model);
    }


    public static String renderCP(final String template, final Map<String, Object> model)
            throws JadeCompilerException, JadeException, IOException
    {
        JadeConfiguration config = getCPConfig();
        return config.renderTemplate(config.getTemplate(template), model);
    }
}
