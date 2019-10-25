package io.jenkins.plugins;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;

import org.kohsuke.stapler.DataBoundConstructor;
import org.w3c.dom.Document;


import javax.sql.rowset.spi.XmlReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class ITMSPostBuildConfiguration extends Notifier {

    private final String projectUrl;

    @DataBoundConstructor
    public ITMSPostBuildConfiguration(final String projectUrl) {
        this.projectUrl = projectUrl;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    @Override
    public boolean perform(final AbstractBuild build, final Launcher launcher, final BuildListener listener) {
        try {
            listener.getLogger().println("Starting Post Build Action");
            listener.getLogger().print("CUSTOM LOG:  " + sendRequest(build, listener));


        } catch (Exception e) {
            listener.getLogger().printf("Error Occurred : %s ", e);
        }
        listener.getLogger().println("Finished Post Build Action");
        return true;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public ITMSGlobalConfiguration getDescriptor() {
        return (ITMSGlobalConfiguration) super.getDescriptor();
    }

    private String sendRequest(AbstractBuild build, BuildListener listener) throws Exception {


        URL url = new URL("https://webhook.site/cb50f24f-8d4a-463b-ba99-39a8b8485817");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/xml");

        File fXmlFile = new File(projectUrl);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
        Document xmlDom = docBuilder.parse(fXmlFile);

        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
            writer.write(xmlDom.toString()); // Write POST query string (if any needed).
        } finally {
            writer.close();
        }

//            TransformerFactory tf = TransformerFactory.newInstance();
//            Transformer transformer = tf.newTransformer();
//        FileReader fileReader = new FileReader(projectUrl);
//        StreamSource source = new StreamSource((File) doc);
//        StreamResult result = new StreamResult(os);
//        transformer.transform(source, result);

//        os.flush();
        InputStream result = connection.getInputStream();
        connection.disconnect();
        return projectUrl + "\n" + xmlDom.toString() + "\n" + result.toString() + "\n" + connection.getResponseCode() + "\n" + result.toString();


//        String readLine;
//        URL urlForGetRequest = new URL(url);
//        HttpURLConnection connection = (HttpURLConnection) urlForGetRequest.openConnection();
//        connection.setInstanceFollowRedirects(false);
//        connection.setRequestMethod("POST");
//        connection.setRequestProperty("userId", "a1bcdef"); // set userId its a sample here
//        OutputStream os = connection.getOutputStream();
//        int responseCode = connection.getResponseCode();
//
//        StringBuilder response;
//        if (responseCode == HttpURLConnection.HTTP_OK) {
//            try (BufferedReader in = new BufferedReader(
//                    new InputStreamReader(connection.getInputStream()))) {
//                response = new StringBuilder();
//                while ((readLine = in.readLine()) != null) {
//                    response.append(readLine);
//                }
//            } finally {
//                connection.disconnect();
//            }
//        } else {
//            connection.disconnect();
//        }

        // form parameters
//        Map<Object, Object> data = new HashMap<>();
//        data.put("baseName", Objects.requireNonNull(build.getWorkspace()).getBaseName());
//        data.put("getName", build.getWorkspace().getName());
//        data.put("getFullDisplayName", build.getFullDisplayName());
//        data.put("getDisplayName", build.getDisplayName());
//        data.put("getUrl", build.getUrl());
//
//        OkHttpClient httpClient = new OkHttpClient();
//
//        // form parameters
//        RequestBody formBody = new FormBody.Builder()
//                .add("baseName", Objects.requireNonNull(build.getWorkspace()))
//                .add("getName", build.getWorkspace().getName())
//                .add("getFullDisplayName", build.getFullDisplayName())
//                .add("getDisplayName", build.getDisplayName())
//                .add("getUrl", build.getUrl())
//                .build();
//
//        Request request = new Request.Builder()
//                .url(url)
//                .addHeader("User-Agent", "OkHttp Bot")
//                .post(formBody)
//                .build();
//
//        try (Response response = httpClient.newCall(request).execute()) {
//
//            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//
//            // Get response body
//            System.out.println(response.body().string());
//        }


    }


}
