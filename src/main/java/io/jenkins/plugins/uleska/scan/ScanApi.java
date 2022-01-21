package io.jenkins.plugins.uleska.scan;


import java.util.UUID;

public interface ScanApi extends AutoCloseable {

    /**
     * Scans an application/version with the tools in that application/version
     * @deprecated Under newer versions of Uleska you will only be able to scan with a toolkit
     * @param applicationId The id of the application being scanned
     * @param versionId The id of the version being scanned
     * @throws ScanException if something goes wrong making a scanning request
     */
    @Deprecated
    void doScan(UUID applicationId, UUID versionId) throws ScanException;

    /**
     * Scans an application/version with the tools in the toolkit
     * @param applicationId The id of the application being scanned
     * @param versionId The id of the version being scanned
     * @param  toolkitId The id of the toolkit
     * @throws ScanException if something goes wrong making a scanning request
     */
    void doScan(UUID applicationId, UUID versionId, UUID toolkitId) throws ScanException;

}
