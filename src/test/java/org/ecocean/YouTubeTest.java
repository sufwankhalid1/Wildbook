package org.ecocean;

import javax.servlet.http.HttpServletRequest;
import org.ecocean.servlet.ServletUtilities;

import java.io.IOException;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.junit.Test;

public class YouTubeTest {
    private static String apiKey = null;
    private static String refreshToken = null;
    private static com.google.api.services.youtube.YouTube youtube;
    private static com.google.api.services.youtube.YouTube youtube2;
    private static HttpServletRequest request;
    public static final double EXTRACT_FPS = 0.5;  //note: this *must* be synced with value in config/youtube_extract.sh

    @Test
    public void detectInitRequestWorks() throws Exception {
        String URL = "https://www.youtube.com/oembed?url=http://www.youtube.com/watch?v=lRbu7ESK7bQ&format=json";
//        String context = ServletUtilities.getContext(request);
        String context = "context";
//        String request = URL;
//        System.out.println("Checking for a context: "+request.getParameter("context"));
        apiKey = CommonConfiguration.getProperty("youtube_api_key", context);
        youtube = new com.google.api.services.youtube.YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
            public void initialize(com.google.api.client.http.HttpRequest request) throws IOException {
            }
        }).setApplicationName("wildbook-youtube").build();

        String CLIENT_ID = CommonConfiguration.getProperty("youtube_client_id", context);
        String CLIENT_SECRET = CommonConfiguration.getProperty("youtube_client_secret", context);
        String HTML_DESC = CommonConfiguration.getProperty("htmlDescription",context);
        String HTML_KEYS = CommonConfiguration.getProperty("htmlKeywords",context);
        String HTML_TITLE = CommonConfiguration.getProperty("htmlTitle",context);
        String HTML_AUTHOR = CommonConfiguration.getProperty("htmlAuthor",context);


//        System.out.println("URL sample: " + URL);
        System.out.println("CLIENT_ID: " + CLIENT_ID);
        System.out.println("HTML_DESC: " + HTML_DESC);
        System.out.println("HTML_KEYS: " + HTML_KEYS);
        System.out.println("HTML_TITLE: " + HTML_TITLE);
        System.out.println("HTML_AUTHOR: " + HTML_AUTHOR);
        System.out.println("API_KEY: " + apiKey);

        System.out.println("CLIENT_SECRET: " + CLIENT_SECRET);
        refreshToken = CommonConfiguration.getProperty("refresh_token", context);
        System.out.println("refreshToken: " + refreshToken);
        HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
        JsonFactory JSON_FACTORY = new JacksonFactory();

        Credential credential = new GoogleCredential.Builder()
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .setClientSecrets(CLIENT_ID, CLIENT_SECRET)
                .build();
        credential.setRefreshToken(refreshToken);

        youtube2 = new com.google.api.services.youtube.YouTube.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName("wildbook-youtube")
                .build();

        System.out.println("HTTP_TRANSPORT: " + HTTP_TRANSPORT);
        System.out.println("JSON_FACTORY: " + JSON_FACTORY);
        System.out.println("youtube: " + youtube);
        System.out.println("youtube2: " + youtube2);


    }
}