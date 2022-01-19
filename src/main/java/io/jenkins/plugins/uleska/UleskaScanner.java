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
import io.jenkins.plugins.uleska.api.HttpFactory;
import io.jenkins.plugins.uleska.scan.HttpScanApi;
import io.jenkins.plugins.uleska.scan.ScanApi;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class UleskaScanner extends Recorder implements SimpleBuildStep {


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



    @Override
    public void perform(@Nonnull Run<?, ?> run,
                        @Nonnull FilePath filePath,
                        @Nonnull Launcher launcher,
                        @Nonnull TaskListener taskListener) throws IOException {
        checkScanner();

        UleskaInstance uleskaInstance = getUleskaInstance();

        char[] apiKey = fetchApiKey(run, uleskaInstance);
        if (apiKey.length == 0) {
            throw new AbortException(Messages.UleskaScanner_Errors_NoCredentials(""));
        }

        try(ScanApi scanApi = new HttpScanApi(taskListener, new HttpFactory(), uleskaInstance.getUrl(), apiKey)){
            scanApi.doScan(UUID.fromString(applicationId), UUID.fromString(versionId));
            taskListener.getLogger().println("Scan Started");
        } catch (Exception e) {
            if (propagateFailure) {
                taskListener.fatalError(Messages.UleskaScanner_Errors_PropagatedFailure());
                throw new AbortException(e.getMessage());
            } else {
                taskListener.error(Messages.UleskaScanner_Errors_UnpropagatedFailure(e));
            }
        }finally {
           Arrays.fill(apiKey, '*');
        }
    }

    private char[] fetchApiKey(Run<?, ?> run, UleskaInstance uleskaInstance){
        String credentialsId = uleskaInstance.getCredentialsId();
        StringCredentials credentials = CredentialsProvider.findCredentialById(credentialsId, StringCredentials.class, run);
        if(credentials == null){
            return new char[] {};
        }
        return credentials.getSecret().getPlainText().toCharArray();
    }

    @Symbol("uleskaScanner")
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
