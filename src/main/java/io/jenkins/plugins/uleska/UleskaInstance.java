package io.jenkins.plugins.uleska;

import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;

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
