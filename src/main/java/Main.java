import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main {

    final static String REMOTE_SERVICE_URL = "https://api.nasa.gov/planetary/apod?api_key=" +
            "sEyL0Lywh9C29V5UtD9066Cg9cg8h5HDjnPWdkqw";
    final static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)
                        .setSocketTimeout(30000)
                        .setRedirectsEnabled(false)
                        .build())
                .build();

        HttpGet request = new HttpGet(REMOTE_SERVICE_URL);
        request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());

        CloseableHttpResponse response = httpClient.execute(request);

        Request nasaFile = mapper.readValue(response.getEntity().getContent(),
                new TypeReference<>() {
                });

        response.close();

        String[] parts = nasaFile.getHdurl().split("/");
        String fileName = parts[parts.length-1];

        request = new HttpGet(nasaFile.getUrl());
        response = httpClient.execute(request);

        try(BufferedInputStream bis = new BufferedInputStream(response.getEntity().getContent());
            FileOutputStream fos = new FileOutputStream(fileName)) {
            int i;
            while ( (i =bis.read()) != -1) {
                fos.write(i);
            }

            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.close();
        httpClient.close();
    }
}
