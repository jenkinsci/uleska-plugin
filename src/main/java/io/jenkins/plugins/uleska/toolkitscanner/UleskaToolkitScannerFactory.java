package io.jenkins.plugins.uleska.toolkitscanner;

import hudson.model.TaskListener;
import io.jenkins.plugins.uleska.api.HttpFactory;
import io.jenkins.plugins.uleska.scan.HttpScanApi;
import io.jenkins.plugins.uleska.scan.ScanApi;
import io.jenkins.plugins.uleska.toolkit.HttpToolkitApi;
import io.jenkins.plugins.uleska.toolkit.ToolkitApi;
import io.jenkins.plugins.uleska.toolkit.ToolkitLocator;

public class UleskaToolkitScannerFactory {

    public UleskaToolkitScanner build(TaskListener taskListener, String host, char[] apiKey) {
        HttpFactory httpFactory = new HttpFactory();
        ScanApi scanApi = new HttpScanApi(taskListener, httpFactory, host, apiKey);
        ToolkitApi toolkitApi = new HttpToolkitApi(taskListener, httpFactory, host, apiKey);
        ToolkitLocator toolkitLocator = new ToolkitLocator(toolkitApi);
        return new UleskaToolkitScanner(taskListener, toolkitLocator, scanApi);
    }

}
