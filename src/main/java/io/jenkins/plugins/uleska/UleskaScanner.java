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
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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

    public static String getErrors(String uleskaInstanceName, String applicationId, String versionId) {
        List<String> errors = new ArrayList<>();

        if (!isUleskaInstanceNameValid(uleskaInstanceName)) {
            errors.add(Messages.UleskaScanner_Errors_InstanceNameInvalid());
        }

        if (!isUUIDValid(applicationId)) {
            errors.add(Messages.UleskaScanner_Errors_ApplicationIdInvalid());
        }

        if (!isUUIDValid(versionId)) {
            errors.add(Messages.UleskaScanner_Errors_VersionIdInvalid());
        }

        return !errors.isEmpty()
            ? String.join("; ", errors)
            : null;
    }

    public static boolean isUleskaInstanceNameValid(String name) {
        return !StringUtils.isEmpty(name);
    }

    public static boolean isUUIDValid(String uuid) {
        if (uuid.trim().length() < 36) {
            return false;
        }

        try {
            //noinspection ResultOfMethodCallIgnored
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException iae) {
            return false;
        }
    }

    public String getErrors() {
        return getErrors(uleskaInstanceName, applicationId, versionId);
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

    private void checkScanner() throws AbortException {
        String errors = getErrors();

        if (errors != null) {
            throw new AbortException(Messages.UleskaScanner_Errors_ScannerErrors(errors));
        }
    }

    private UleskaInstance getUleskaInstance() throws AbortException {
        UleskaInstance uleskaInstance = UleskaGlobalConfiguration.getInstance(uleskaInstanceName);
        String errors = uleskaInstance.getErrors();

        if (errors != null) {
            throw new AbortException(Messages.UleskaScanner_Errors_InstanceErrors(uleskaInstanceName, errors));
        }

        return uleskaInstance;
    }

    private CloseableHttpClient getClientWithApiKey(Run<?, ?> run,
                                                    UleskaInstance uleskaInstance) throws AbortException {
        String credentialsId = uleskaInstance.getCredentialsId();
        StringCredentials credentials = CredentialsProvider.findCredentialById(credentialsId, StringCredentials.class, run);

        if (credentials == null) {
            throw new AbortException(Messages.UleskaScanner_Errors_NoCredentials(credentialsId));
        }

        String apiKey = credentials.getSecret().getPlainText();
        Header auth = new BasicHeader("Authorization", "Bearer " + apiKey);

        return HttpClientBuilder.create()
            .setDefaultHeaders(Collections.singletonList(auth))
            .setUserAgent(USER_AGENT)
            .build();
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run,
                        @Nonnull FilePath filePath,
                        @Nonnull Launcher launcher,
                        @Nonnull TaskListener taskListener) throws IOException {
        try {
            checkScanner();

            UleskaInstance uleskaInstance = getUleskaInstance();
            CloseableHttpClient client = getClientWithApiKey(run, uleskaInstance);

            String url = uleskaInstance.getUrl() + String.format(SCAN_URL, applicationId, versionId);

            taskListener.getLogger().println(Messages.UleskaScanner_Info_CallingEndpoint(url));

            try (CloseableHttpResponse response = client.execute(new HttpGet(url))) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode < 200 || statusCode > 299) {
                    throw new AbortException(Messages.UleskaScanner_Errors_Non200Status(statusCode));
                }
            }
        } catch (Exception e) {
            if (propagateFailure) {
                taskListener.fatalError(Messages.UleskaScanner_Errors_PropagatedFailure());
                throw e;
            } else {
                taskListener.error(Messages.UleskaScanner_Errors_UnpropagatedFailure(e));
            }
        }
    }

    @Symbol("UleskaScanner")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> clazz) {
            return true;
        }

        public UleskaInstance[] getUleskaInstances() {
            return UleskaGlobalConfiguration.getAllUleskaInstances();
        }

        public FormValidation doCheckUUID(@QueryParameter String value) {
            return isUUIDValid(value)
                ? FormValidation.ok()
                : FormValidation.error(Messages.UleskaScanner_Errors_UUIDInvalid());
        }

    }

}
