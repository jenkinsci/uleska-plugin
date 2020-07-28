package io.jenkins.plugins.uleska;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import hudson.CopyOnWrite;
import hudson.Extension;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

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
            .orElseThrow(() -> new IllegalStateException("Jenkins instance is not available"));
    }

    public static UleskaGlobalConfiguration get() {
        return GlobalConfiguration.all().get(UleskaGlobalConfiguration.class);
    }

    public UleskaInstance[] getUleskaInstances() {
        return uleskaInstances;
    }

    public void setUleskaInstances(UleskaInstance[] uleskaInstances) {
        this.uleskaInstances = uleskaInstances;
        save();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) {
        List<UleskaInstance> instanceList = req.bindJSONToList(UleskaInstance.class, json.get("uleskaInstance"));
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

}
