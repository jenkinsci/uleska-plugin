package io.jenkins.plugins.uleska;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import jenkins.tasks.SimpleBuildStep;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collections;

public class UleskaScanner extends Recorder implements SimpleBuildStep {

    private final static String USER_AGENT = "uleska-jenkins-plugin";
    private final static String SCAN_URL = "/SecureDesigner/api/v1/applications/%s/versions/%s/scan";

    private final String instanceUrl;
    private final String apiToken;
    private final String applicationId;
    private final String versionId;
    private final boolean propagateFailure;

    @DataBoundConstructor
    public UleskaScanner(String instanceUrl, String apiToken,
                         String applicationId,
                         String versionId,
                         boolean propagateFailure) {
        this.instanceUrl = instanceUrl;
        this.apiToken = apiToken;
        this.applicationId = applicationId;
        this.versionId = versionId;
        this.propagateFailure = propagateFailure;
    }

    public String getInstanceUrl() {
        return instanceUrl;
    }

    public String getApiToken() {
        return apiToken;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getVersionId() {
        return versionId;
    }

    public boolean isPropagateFailure() {
        return propagateFailure;
    }

    private CloseableHttpClient getClient() {
        return HttpClientBuilder.create()
            .setDefaultHeaders(Collections.singletonList(new BasicHeader("Authorization", "Bearer " + apiToken)))
            .setUserAgent(USER_AGENT)
            .build();
    }

    private void failOnPropagation(TaskListener taskListener, Exception... exceptions) {
        if (propagateFailure) {
            taskListener.fatalError("Scan failure propagated");
        }
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run,
                        @Nonnull FilePath filePath,
                        @Nonnull Launcher launcher,
                        @Nonnull TaskListener taskListener) throws InterruptedException, IOException {
        CloseableHttpClient client = getClient();
        String url = instanceUrl + String.format(SCAN_URL, applicationId, versionId);

        taskListener.getLogger().println("Calling " + url);

        try (CloseableHttpResponse response = client.execute(new HttpGet(url))) {
            if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() > 299) {
                taskListener.error("Non 200 status code returned");
                taskListener.error(response.toString());

                if (propagateFailure) {
                    taskListener.fatalError("Scan failure propagated");
                    // TODO: Throw
                }
            }
        } catch (IOException ioe) {
            // throw if propagateFailure
            taskListener.error("Failed due to exception", ioe);

            if (propagateFailure) {
                taskListener.fatalError("Scan failure propagated");
                throw ioe;
            }
        }
    }

    @Symbol("UleskaScanner")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

    }

}
