package io.jenkins.plugins.uleska.api;

import hudson.model.TaskListener;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.HttpResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

public class HttpScanApi implements ScanApi {

    private final TaskListener taskListener;
    private final HttpFactory httpFactory;
    private final String host;
    private final char[] apiKey;

    public HttpScanApi(TaskListener taskListener, HttpFactory httpFactory, String host, char[] apiKey) {
        this.httpFactory = httpFactory;
        this.host = host;
        this.apiKey = apiKey;
        this.taskListener = taskListener;
    }

    @Override
    public void doScan(UUID applicationId, UUID versionId) throws ScanException {
        String address = String.format("%s/SecureDesigner/api/v1/applications/%s/versions/%s/scan", host, applicationId, versionId);
        doHttpGet(address);
    }

    private void doHttpGet(String address) throws ScanException {
        HttpClient client = null;
        try {
            client = httpFactory.build(this.apiKey);
            HttpResponse response = null;
            try {
                taskListener.getLogger().println("GET request to " + address);
                response = client.execute(new HttpGet(address));
                if (!isSuccessful(response)) {
                    throw new ScanException(response.getReasonPhrase());
                }
            } catch (IOException e) {
                throw new ScanException(e);
            } finally {
                closeResource(response);
            }
        } finally {
            closeResource(client);
        }
    }

    private boolean isSuccessful(HttpResponse response) {
        return response.getCode() >= 200 && response.getCode() < 300;
    }

    private void closeResource(Object resource) {
        if (resource instanceof AutoCloseable) {
            try {
                ((AutoCloseable) resource).close();
            } catch (Exception e) {
                taskListener.error("Unable to close " + resource.getClass().getSimpleName()  + " because of " + e.getMessage());
            }
        }
    }

    @Override
    public void close() throws Exception {
        Arrays.fill(this.apiKey, '*');
    }

}
