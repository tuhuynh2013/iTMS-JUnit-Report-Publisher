package io.jenkins.plugins;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;

import io.jenkins.plugins.model.AuthenticationInfo;
import io.jenkins.plugins.rest.RequestAPI;
import io.jenkins.plugins.rest.StandardResponse;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static io.jenkins.plugins.model.ITMSConsts.SERVICE_NAME;


public class JUnitPostBuild extends Notifier {

    private final String itmsAddress;
    private final String reportFolder;
    private final String projectId;
    private final String ticketKey;
    private final String cycleName;

    @DataBoundConstructor
    public JUnitPostBuild(final String itmsAddress, final String reportFolder,
                          final String projectId, final String ticketKey, final String cycleName) {
        this.itmsAddress = itmsAddress.trim();
        this.reportFolder = reportFolder.trim();
        this.projectId = projectId.trim();
        this.ticketKey = ticketKey.trim();
        this.cycleName = cycleName.trim();
    }

    @Override
    public boolean perform(final AbstractBuild build, final Launcher launcher, final BuildListener listener) {
        int counter = 0;
        try {
            listener.getLogger().println("Starting Post Build Action");

            File folder = new File(build.getWorkspace() + reportFolder);
            listener.getLogger().println("Report folder: " + folder.getPath());
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    String content;
                    if (file.getName().toLowerCase().endsWith(".xml")) {
                        counter++;
                        content = readFileContent(file);
                        listener.getLogger().println(sendReportContent(content, build));
                    }
                }

                if (counter < 1) {
                    listener.getLogger().println("Report file not found! Check your report folder and format type");
                }

            } else {
                listener.getLogger().println("Folder is empty!");
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
    public JUnitGlobalConfiguration getDescriptor() {
        return (JUnitGlobalConfiguration) super.getDescriptor();
    }

    private StandardResponse sendXMLContent(String content, AbstractBuild build) {
        AuthenticationInfo authenticationInfo = getDescriptor().getAuthenticationInfo();

        Cause cause = (Cause) build.getCauses().get(0);
        String userCause = ((Cause.UserIdCause) cause).getUserId();

        JSONArray jenkinsAttributes = new JSONArray();
        JSONObject jenkinsAttr = new JSONObject();
        jenkinsAttr.put("build_number", build.number);
        jenkinsAttr.put("build_status", Objects.requireNonNull(build.getResult()).toString().toLowerCase());
        jenkinsAttr.put("user", userCause);
        jenkinsAttr.put("report_type", "junit");
        jenkinsAttributes.add(jenkinsAttr);

        JSONObject data = new JSONObject();
        data.put("username", authenticationInfo.getUsername());
        data.put("service_name", SERVICE_NAME);
        data.put("token", authenticationInfo.getToken());
        data.put("project_id", projectId);
        data.put("jenkins_auto_executions_attributes", jenkinsAttributes);
        data.put("ticket_key", ticketKey);
        data.put("cycle_name", cycleName);
        data.put("is_json", Boolean.FALSE);
        data.put("report_content", content);
        RequestAPI requestAPI = new RequestAPI(itmsAddress);
        return requestAPI.createPOSTRequest(data);
    }

    private String readFileContent(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        Files.lines(Paths.get(file.toString()),
                StandardCharsets.UTF_8).forEach(content::append);
        return content.toString();
    }

    private String sendReportContent(String content, AbstractBuild build) {
        if (content.length() > 0) {
            StandardResponse response = sendXMLContent(content, build);
            return "JUnit plugin response: " + response.toString();
        }
        return "Report file(s) is empty!";
    }

    public String getItmsAddress() {
        return itmsAddress;
    }

    public String getReportFolder() {
        return reportFolder;
    }

    public String getTicketKey() {
        return ticketKey;
    }

    public String getCycleName() {
        return cycleName;
    }

    public String getProjectId() {
        return projectId;
    }

}
