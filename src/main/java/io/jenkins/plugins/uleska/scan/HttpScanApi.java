package io.jenkins.plugins.uleska.scan;

import hudson.model.TaskListener;
import io.jenkins.plugins.uleska.api.BaseHttpApi;
import io.jenkins.plugins.uleska.api.HttpException;
import io.jenkins.plugins.uleska.api.HttpFactory;
import java.util.UUID;

public class HttpScanApi extends BaseHttpApi implements ScanApi {

    private static final String SCAN_ADDRESS = "%s/SecureDesigner/api/v1/applications/%s/versions/%s/scan";
    private static final String SCAN_TOOLKITS_ADDRESS = "%s/SecureDesigner/api/v1/applications/%s/versions/%s/scan/%s";

    public HttpScanApi(TaskListener taskListener, HttpFactory httpFactory, String host, char[] apiKey) {
       super(taskListener, httpFactory, host, apiKey);
    }

    @Override
    public void doScan(UUID applicationId, UUID versionId) throws ScanException {
        String address = String.format(SCAN_ADDRESS, host, applicationId, versionId);
        doScan(address);
    }

    @Override
    public void doScan(UUID applicationId, UUID versionId, UUID toolkitId) throws ScanException {
        String address = String.format(SCAN_TOOLKITS_ADDRESS, host, applicationId, versionId, toolkitId);
        doScan(address);
    }

    private void doScan(String address) throws ScanException{
        try {
            doHttpGet(address, Void.TYPE);
        }catch (HttpException e){
            throw new ScanException(e);
        }
    }


}
