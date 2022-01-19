package io.jenkins.plugins.uleska.toolkit;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hudson.model.TaskListener;
import io.jenkins.plugins.uleska.api.BaseHttpApi;
import io.jenkins.plugins.uleska.api.HttpException;
import io.jenkins.plugins.uleska.api.HttpFactory;
import org.apache.hc.core5.http.ClassicHttpResponse;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class HttpToolkitApi extends BaseHttpApi implements ToolkitApi {

    private final static Type TOOLKIT_COLLECTION_TYPE = new TypeToken<Collection<ToolkitWrapper>>() {

    }.getType();
    private final static String TOOLKIT_ADDRESS = "/SecureDesigner/api/v1/toolkits";

    public HttpToolkitApi(TaskListener taskListener,
                          HttpFactory httpFactory,
                          String host, char[] apiKey) {
        super(taskListener, httpFactory, host, apiKey);
    }

    @Override
    public Collection<Toolkit> fetchToolkits() {
        try {
            ClassicHttpResponse response = doHttpGet(TOOLKIT_ADDRESS);
            Gson gson = new Gson();
            Reader entityReader = new InputStreamReader(response.getEntity().getContent());
            Collection<ToolkitWrapper> toolkitWrappers = gson.fromJson(entityReader, TOOLKIT_COLLECTION_TYPE);
            return toolkitWrappers.stream().map(ToolkitWrapper::getToolkit).collect(Collectors.toList());
        } catch (HttpException | IOException e) {
            logError(e);
        }
        return Collections.emptyList();
    }

    private void logError(Exception e) {
        String errorMessage = String.format("Unable to get toolkits because %s exception thrown with message %s.",
            e.getClass().getSimpleName(),
            e.getMessage());
        taskListener.error(errorMessage);
    }

}
