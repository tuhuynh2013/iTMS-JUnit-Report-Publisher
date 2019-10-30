package io.jenkins.rest;

import hidden.jth.org.apache.http.HttpResponse;
import hidden.jth.org.apache.http.client.methods.HttpPost;
import hidden.jth.org.apache.http.entity.StringEntity;
import hidden.jth.org.apache.http.impl.client.CloseableHttpClient;
import hidden.jth.org.apache.http.impl.client.HttpClientBuilder;
import net.sf.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;


public class RequestAPI {

    private String baseUrl;
    private CloseableHttpClient httpClient;
    private HttpURLConnection connection;

    public RequestAPI(String baseUrl) {
        this.baseUrl = baseUrl;
        httpClient = HttpClientBuilder.create().build();
    }

    public int createPOSTRequest(JSONObject postData) throws IOException {
        int responseCode = 0;
        try {
            StringEntity params = new StringEntity(postData.toString());
            HttpPost request = new HttpPost(baseUrl);
            request.addHeader("content-type", "application/json");
            request.setEntity(params);

            HttpResponse response = httpClient.execute(request);
            responseCode = response.getStatusLine().getStatusCode();
        } catch (IOException e) {

        } finally {
            httpClient.close();
        }
        return responseCode;
    }

}
