package io.jenkins.plugins.uleska;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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

    public static String getErrors(String name, String url, String credentialsId) {
        List<String> errors = new ArrayList<>();

        if (!isNameValid(name)) {
            errors.add(Messages.UleskaInstance_Errors_NameInvalid());
        }

        if (!isUrlValid(url)) {
            errors.add(Messages.UleskaInstance_Errors_UrlInvalid());
        }

        if (!isCredentialsIdValid(credentialsId)) {
            errors.add(Messages.UleskaInstance_Errors_CredentialsInvalid());
        }

        return !errors.isEmpty()
            ? String.join("; ", errors)
            : null;
    }

    public static boolean isNameValid(String name) {
        return !StringUtils.isEmpty(name);
    }

    public static boolean isUrlValid(String url) {
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            return false;
        }

        return url.lastIndexOf('/') < 8;
    }

    public static boolean isCredentialsIdValid(String credentialsId) {
        return !StringUtils.isEmpty(credentialsId);
    }

    public String getErrors() {
        return getErrors(name, url, credentialsId);
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
