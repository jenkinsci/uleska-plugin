package io.jenkins.plugins.uleska;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;
import java.util.Arrays;

public class UleskaInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;

    private final String url;

    private final String credentialsId;

    @DataBoundConstructor
    public UleskaInstance(String name, String url, String credentialsId) {
        this.name = name;
        this.url = url;
        this.credentialsId = credentialsId;
    }

    public static UleskaInstance[] all() {
        return UleskaGlobalConfiguration.get().getUleskaInstances();
    }

    public static UleskaInstance get(String name) {
        UleskaInstance[] uleskaInstances = all();

        if (StringUtils.isEmpty(name) && uleskaInstances.length > 0) {
            return uleskaInstances[0];
        }

        return Arrays.stream(uleskaInstances)
            .filter(instance -> instance.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

}
