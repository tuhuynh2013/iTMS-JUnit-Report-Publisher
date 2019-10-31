package io.jenkins.plugins;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import io.jenkins.rest.RequestAPI;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.verb.POST;
import hidden.jth.org.apache.http.HttpResponse;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.HttpURLConnection;

@Extension
public final class ITMSGlobalConfiguration extends BuildStepDescriptor<Publisher> {

    /**
     * Global configuration information variables. If you don't want fields
     * to be persisted, use <tt>transient</tt>.
     */
    private String itmsServer;
    private String username;
    private String companyName;
    private String token;
    private AuthenticationInfo authenticationInfo = new AuthenticationInfo();
    /**
     * In order to load the persisted global configuration, you have to call
     * load() in the constructor.
     */
    public ITMSGlobalConfiguration() {
        super(ITMSPostBuildConfiguration.class);
        load();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject formData)
            throws FormException {
        // To persist global configuration information, set that to
        // properties and call save().
        itmsServer = formData.getString("itmsServer");
        username = formData.getString("username");
        companyName = formData.getString("companyName");
        token = formData.getString("token");

        authenticationInfo.setUsername(username);
        authenticationInfo.setCompanyName(companyName);
        authenticationInfo.setToken(token);
        save();
        return super.configure(req, formData);
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
        return true;
    }

    @Nonnull
    @Override
    public String getDisplayName() {
        return ITMSConsts.POST_BUILD_NAME;
    }

    @POST
    public FormValidation doTestConnection(@QueryParameter String itmsServer, @QueryParameter String username,
                                           @QueryParameter String companyName, @QueryParameter String token) throws IOException {

        if (StringUtils.isBlank(itmsServer)) {
            return FormValidation.error("Please enter the iTMS server address");
        }

        if (StringUtils.isBlank(username)) {
            return FormValidation.error("Please enter the username");
        }

        if (StringUtils.isBlank(companyName)) {
            return FormValidation.error("Please enter the company name");
        }

        if (StringUtils.isBlank(token)) {
            return FormValidation.error("Please enter the token");
        }

        JSONObject postData = new JSONObject();
        postData.put("username", username);
        postData.put("company_name", companyName);
        postData.put("service_name", "jenkins");
        postData.put("token", token);

        RequestAPI request = new RequestAPI(itmsServer);
        HttpResponse response = request.createPOSTRequest(postData);

        if (response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
            return FormValidation.error(response.getStatusLine().getStatusCode() + ": " +
                    response.getStatusLine().getReasonPhrase());
        }

        return FormValidation.ok("Connection to iTMS has been validated");
    }

    public String getItmsServer() {
        return itmsServer;
    }

    public String getUsername() {
        return username;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getToken() {
        return token;
    }

    public AuthenticationInfo getAuthenticationInfo() {
        return authenticationInfo;
    }

}
