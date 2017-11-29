package org.ecocean.translate;

import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import org.ecocean.CommonConfiguration;
import org.junit.Test;


import static org.junit.Assert.*;

public class DetectTranslateTest {

    @Test
    public void detectLanguageAsEnglish_true() throws Exception {
        String text = "With great power comes great responsibility";
        String context = "context";

        String apiKey= CommonConfiguration.getProperty("translate_key", context);
        Translate translate = TranslateOptions.newBuilder().setApiKey(apiKey).build().getService();

        Detection detection = translate.detect(text);
        String detectedLanguage = detection.getLanguage();

        assert(detectedLanguage.contains("en"));
    }

    @Test
    public void detectLanguageAsSpanish_true() throws Exception {
        String text = "Con un gran poder viene una gran responsabilidad";
        String context = "context";

        String apiKey= CommonConfiguration.getProperty("translate_key", context);
        Translate translate = TranslateOptions.newBuilder().setApiKey(apiKey).build().getService();

        Detection detection = translate.detect(text);
        String detectedLanguage = detection.getLanguage();

        assert(detectedLanguage.contains("es"));
    }

    @Test
    public void detectLanguageAsGerman_true() throws Exception {
        String text = "Mit großer Macht kommt große Verantwortung";
        String context = "context";

        String apiKey= CommonConfiguration.getProperty("translate_key", context);
        Translate translate = TranslateOptions.newBuilder().setApiKey(apiKey).build().getService();

        Detection detection = translate.detect(text);
        String detectedLanguage = detection.getLanguage();

        assert(detectedLanguage.contains("de"));
    }

    @Test
    public void translateLanguageToEnglish() throws Exception {
        String text = "Con un gran poder viene una gran responsabilidad";
        String context = "context";

        String apiKey= CommonConfiguration.getProperty("translate_key", context);
        Translate translate = TranslateOptions.newBuilder().setApiKey(apiKey).build().getService();
        Translation translation = translate.translate(text, Translate.TranslateOption.targetLanguage("en"));

        assert(translation.getTranslatedText().contains("With great power comes a great responsibility"));
    }

    @Test public void translateFromNativeToEnglishToNative() throws Exception {
        String text = "Mit großer Macht kommt große Verantwortung";
        String context = "context";
        System.out.println("Native input: " + text);


        String apiKey= CommonConfiguration.getProperty("translate_key", context);
        Translate translate = TranslateOptions.newBuilder().setApiKey(apiKey).build().getService();

        Detection detection = translate.detect(text);
        String detectedLanguage = detection.getLanguage();

        Translation translation = translate.translate(text, Translate.TranslateOption.targetLanguage("en"));
        System.out.println("Translated to English: " + translation.getTranslatedText());

        Translation translation2 = translate.translate(text, Translate.TranslateOption.targetLanguage(detectedLanguage));
        System.out.println("Translated back to Native: " + translation2.getTranslatedText());
        assert(translation.getTranslatedText().contains("With great power comes great responsibility"));
        assert(translation2.getTranslatedText().contains("Mit großer Macht kommt große Verantwortung"));
    }




}