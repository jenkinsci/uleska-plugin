package io.jenkins.plugins.uleska;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import hudson.CopyOnWrite;
import hudson.Extension;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Extension
public class UleskaGlobalConfiguration extends GlobalConfiguration {

    private final Supplier<Jenkins> jenkinsSupplier;

    @CopyOnWrite
    private volatile UleskaInstance[] uleskaInstances = new UleskaInstance[0];

    public UleskaGlobalConfiguration() {
        load();

        this.jenkinsSupplier = () -> Optional.ofNullable(Jenkins.getInstanceOrNull())
            .orElseThrow(() -> new IllegalStateException(Messages.UleskaGlobalConfiguration_Errors_NoJenkinsInstance()));
    }

    public static UleskaGlobalConfiguration get() {
        return GlobalConfiguration.all().get(UleskaGlobalConfiguration.class);
    }

    public static UleskaInstance[] getAllUleskaInstances() {
        return get().getUleskaInstances();
    }

    public static UleskaInstance getInstance(String name) {
        UleskaInstance[] uleskaInstances = getAllUleskaInstances();

        if (StringUtils.isEmpty(name) && uleskaInstances.length > 0) {
            return uleskaInstances[0];
        }

        return Arrays.stream(uleskaInstances)
            .filter(instance -> instance.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    public UleskaInstance[] getUleskaInstances() {
        return uleskaInstances.clone();
    }

    public void setUleskaInstances(UleskaInstance[] uleskaInstances) {
        this.uleskaInstances = uleskaInstances.clone();
        save();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) {
        List<UleskaInstance> instanceList = req.bindJSONToList(UleskaInstance.class, json.get("uleskaInstance"));
        //noinspection ToArrayCallWithZeroLengthArrayArgument
        setUleskaInstances(instanceList.toArray(new UleskaInstance[instanceList.size()]));

        return true;
    }

    public ListBoxModel doFillCredentialsIdItems(@QueryParameter String credentialsId) {
        return !jenkinsSupplier.get().hasPermission(Jenkins.ADMINISTER)
            ? new StandardListBoxModel().includeCurrentValue(credentialsId)
            : new StandardListBoxModel()
            .includeEmptyValue()
            .includeMatchingAs(
                ACL.SYSTEM,
                jenkinsSupplier.get(),
                StringCredentials.class,
                Collections.emptyList(),
                CredentialsMatchers.always()
            );
    }

    public FormValidation doCheckInstanceName(@QueryParameter String value) {
        return UleskaInstance.isNameValid(value)
            ? FormValidation.ok()
            : FormValidation.error(Messages.UleskaGlobalConfiguration_Errors_NameInvalid());
    }

    public FormValidation doCheckInstanceUrl(@QueryParameter String value) {
        return UleskaInstance.isUrlValid(value)
            ? FormValidation.ok()
            : FormValidation.error(Messages.UleskaGlobalConfiguration_Errors_UrlInvalid());
    }

}
