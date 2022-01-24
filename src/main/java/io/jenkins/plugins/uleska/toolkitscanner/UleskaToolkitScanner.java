package io.jenkins.plugins.uleska.toolkitscanner;

import hudson.model.TaskListener;
import io.jenkins.plugins.uleska.scan.ScanApi;
import io.jenkins.plugins.uleska.scan.ScanException;
import io.jenkins.plugins.uleska.toolkit.Toolkit;
import io.jenkins.plugins.uleska.toolkit.ToolkitLocator;

import java.util.Optional;
import java.util.UUID;

public class UleskaToolkitScanner implements AutoCloseable {

    private final TaskListener taskListener;
    private final ToolkitLocator toolkitLocator;
    private final ScanApi scanApi;

    UleskaToolkitScanner(TaskListener taskListener, ToolkitLocator toolkitLocator, ScanApi scanApi) {
        this.taskListener = taskListener;
        this.toolkitLocator = toolkitLocator;
        this.scanApi = scanApi;
    }

    public boolean performScan(String applicationId, String versionId, String toolkitName) {
        UUID realApplicationId;
        try {
            realApplicationId = UUID.fromString(applicationId);
        } catch (IllegalArgumentException e) {
            this.taskListener.error("%s is not a valid UUID.  It's needed for Application Id", applicationId);
            return false;
        }

        UUID realVersionId;
        try {
            realVersionId = UUID.fromString(versionId);
        } catch (IllegalArgumentException e) {
            this.taskListener.error("%s is not a valid UUID.  It's needed for Version Id", applicationId);
            return false;
        }

        Optional<UUID> maybeToolkitId = toolkitLocator.findByName(toolkitName).map(Toolkit::getId);
        if (maybeToolkitId.isPresent()) {
            return scan(realApplicationId, realVersionId, maybeToolkitId.get());
        } else {
            this.taskListener.error("Could not find toolkit with name %s", toolkitName);
            return false;
        }
    }

    @Override
    public void close() throws Exception {
        MultiException exceptions = new MultiException();

        try {
            this.scanApi.close();
        } catch (Exception e) {
            exceptions.add(e);
        }

        try {
            this.toolkitLocator.close();
        } catch (Exception e) {
            exceptions.add(e);
        }

        if (exceptions.isNotEmpty()) {
            throw exceptions;
        }
    }

    private boolean scan(UUID applicationId, UUID versionId, UUID toolkitId) {
        try {
            scanApi.doScan(applicationId, versionId, toolkitId);
            return true;
        } catch (ScanException e) {
            this.taskListener.error("Unable to complete scan. Because of %s", e.getMessage());
            return false;
        }
    }

}
