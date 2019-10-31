package io.jenkins.plugins;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;

import io.jenkins.rest.RequestAPI;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import hidden.jth.org.apache.http.HttpResponse;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;


public class ITMSPostBuildConfiguration extends Notifier {

    private final String reportFolder;

    @DataBoundConstructor
    public ITMSPostBuildConfiguration(final String reportFolder) {
        this.reportFolder = reportFolder;
    }

    public String getReportFolder() {
        return reportFolder;
    }

    @Override
    public boolean perform(final AbstractBuild build, final Launcher launcher, final BuildListener listener) {
        try {
            listener.getLogger().println("Starting Post Build Action1");

            File folder = new File(build.getWorkspace() + reportFolder);
            listener.getLogger().println("Report folder" + folder.getPath());
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles != null) {
                for (File file: listOfFiles) {
                    if (file.getName().endsWith(".xml") || file.getName().endsWith(".XML")) {
                        StringBuilder content = new StringBuilder();
                        Files.lines(Paths.get(file.toString()),
                                StandardCharsets.UTF_8).forEach(content::append);
                        if (content.length() > 0) {
                            HttpResponse response = sendXMLContent(content.toString());
                            listener.getLogger().println("Response: " + response.getStatusLine().getReasonPhrase());
                        } else {
                            listener.getLogger().println("Report file(s) is empty!");
                        }
                    }
                }
            } else {
                listener.getLogger().println("File not found!");
            }

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

    private HttpResponse sendXMLContent (String content) throws IOException {
        JSONObject data = new JSONObject();
        data.put("content", content);
        RequestAPI requestAPI = new RequestAPI("https://webhook.site/cb50f24f-8d4a-463b-ba99-39a8b8485817");
        return requestAPI.createPOSTRequest(data);
    }


}
