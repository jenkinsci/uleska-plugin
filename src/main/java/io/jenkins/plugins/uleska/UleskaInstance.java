package io.jenkins.plugins.uleska;

import jenkins.org.apache.commons.validator.routines.UrlValidator;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UleskaInstance implements Serializable {

    private static final UrlValidator urlValidator = new UrlValidator();

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

    public static boolean isNameValid(String name) {
        return !StringUtils.isBlank(name);
    }

    public static boolean isUrlValid(String url) {
        return urlValidator.isValid(url) && url.lastIndexOf('/') < 8;
    }

    public static boolean isCredentialsIdValid(String credentialsId) {
        return !StringUtils.isBlank(credentialsId);
    }

    public String getErrors() {
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
