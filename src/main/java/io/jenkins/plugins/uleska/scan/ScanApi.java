package io.jenkins.plugins.uleska.scan;

import java.util.UUID;

public interface ScanApi extends AutoCloseable {

    void doScan(UUID applicationId, UUID versionId) throws ScanException;

}
