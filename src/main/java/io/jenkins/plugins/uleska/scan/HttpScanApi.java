package io.jenkins.plugins.uleska.scan;

import hudson.model.TaskListener;
import io.jenkins.plugins.uleska.api.BaseHttpApi;
import io.jenkins.plugins.uleska.api.HttpException;
import io.jenkins.plugins.uleska.api.HttpFactory;
import java.util.UUID;

public class HttpScanApi extends BaseHttpApi implements ScanApi {

    private static final String SCAN_ADDRESS = "%s/SecureDesigner/api/v1/applications/%s/versions/%s/scan";

    public HttpScanApi(TaskListener taskListener, HttpFactory httpFactory, String host, char[] apiKey) {
       super(taskListener, httpFactory, host, apiKey);
    }

    @Override
    public void doScan(UUID applicationId, UUID versionId) throws ScanException {
        String address = String.format(SCAN_ADDRESS, host, applicationId, versionId);
        try {
            doHttpGet(address);
        }catch (HttpException e){
            throw new ScanException(e);
        }
    }


}
