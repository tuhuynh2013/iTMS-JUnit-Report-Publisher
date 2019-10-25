package io.jenkins.plugins;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;


@Extension
public final class ITMSGlobalConfiguration extends BuildStepDescriptor<Publisher> {

    /**
     * Global configuration information variables. If you don't want fields
     * to be persisted, use <tt>transient</tt>.
     */
    private String username;
    private String token;

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }

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
        username = formData.getString("username");
        token = formData.getString("token");
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

}
