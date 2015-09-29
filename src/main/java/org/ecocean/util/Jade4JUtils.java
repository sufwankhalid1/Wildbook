package org.ecocean.util;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.ecocean.Global;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.exceptions.JadeCompilerException;
import de.neuland.jade4j.exceptions.JadeException;
import de.neuland.jade4j.template.ClasspathTemplateLoader;
import de.neuland.jade4j.template.JadeTemplate;

public class Jade4JUtils {
    private static Logger logger = LoggerFactory.getLogger(Jade4JUtils.class);

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
            // For debug purposes we disable the cache when developing
            //
            fileConfig.setCaching(! Global.INST.isDevEnv());
        }

        return fileConfig;
    }


    private static String renderTemplate(final JadeConfiguration config,
                                         final String template,
                                         final Map<String, Object> model)
        throws JadeCompilerException, JadeException, IOException
    {
        if (logger.isDebugEnabled()) {
            logger.debug(LogBuilder.quickLog("rendering template", template));
        }

        JadeTemplate jt = config.getTemplate(template);

        if (model != null) {
            return config.renderTemplate(jt, model);
        }

        return config.renderTemplate(jt, Collections.emptyMap());
    }


    public static String renderFile(final String template, final Map<String, Object> model)
            throws JadeCompilerException, JadeException, IOException
    {
        return renderTemplate(getFileConfig(), template, model);
    }


    public static String renderCP(final String template, final Map<String, Object> model)
            throws JadeCompilerException, JadeException, IOException
    {
        return renderTemplate(getCPConfig(), template, model);
    }
}
