package io.jenkins.plugins.uleska;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.AbortException;
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
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collections;

public class UleskaScanner extends Recorder implements SimpleBuildStep {

    private final static String USER_AGENT = "uleska-jenkins-plugin";
    private final static String SCAN_URL = "/SecureDesigner/api/v1/applications/%s/versions/%s/scan";

    private final String uleskaInstanceName;
    private final String applicationId;
    private final String versionId;
    private final boolean propagateFailure;

    @DataBoundConstructor
    public UleskaScanner(String uleskaInstanceName,
                         String applicationId,
                         String versionId,
                         boolean propagateFailure) {
        this.uleskaInstanceName = uleskaInstanceName;
        this.applicationId = applicationId;
        this.versionId = versionId;
        this.propagateFailure = propagateFailure;
    }

    public String getUleskaInstanceName() {
        return uleskaInstanceName;
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

    private CloseableHttpClient getClient(String apiKey) {
        Header auth = new BasicHeader("Authorization", "Bearer " + apiKey);

        return HttpClientBuilder.create()
            .setDefaultHeaders(Collections.singletonList(auth))
            .setUserAgent(USER_AGENT)
            .build();
    }

    private void failOnPropagation(TaskListener taskListener) throws AbortException {
        if (propagateFailure) {
            taskListener.fatalError("Scan failure propagated");
            throw new AbortException();
        }
    }

    private UleskaInstance getUleskaInstance() {
        return UleskaInstance.get(uleskaInstanceName);
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run,
                        @Nonnull FilePath filePath,
                        @Nonnull Launcher launcher,
                        @Nonnull TaskListener taskListener) throws IOException {
        UleskaInstance uleskaInstance = getUleskaInstance();

        StringCredentials credentials = CredentialsProvider.findCredentialById(uleskaInstance.getCredentialsId(), StringCredentials.class, run);
        String apiKey = credentials.getSecret().getPlainText();

        CloseableHttpClient client = getClient(apiKey);
        String url = uleskaInstance.getUrl() + String.format(SCAN_URL, applicationId, versionId);

        taskListener.getLogger().println("Calling " + url);

        try (CloseableHttpResponse response = client.execute(new HttpGet(url))) {
            if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() > 299) {
                taskListener.error("Non 200 status code returned");
                taskListener.error(response.toString());

                failOnPropagation(taskListener);
            }
        } catch (IOException ioe) {
            // throw if propagateFailure
            taskListener.error("Failed due to exception");

            failOnPropagation(taskListener);
        }
    }

    @Symbol("UleskaScanner")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public UleskaInstance[] getUleskaInstances() {
            return UleskaInstance.all();
        }

    }

}
