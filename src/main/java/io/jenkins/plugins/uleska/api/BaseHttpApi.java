package io.jenkins.plugins.uleska.api;

import com.google.gson.Gson;
import hudson.model.TaskListener;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class BaseHttpApi implements AutoCloseable {

    protected final TaskListener taskListener;
    protected final HttpFactory httpFactory;
    protected final String host;
    protected final char[] apiKey;

    public BaseHttpApi(TaskListener taskListener, HttpFactory httpFactory, String host, char[] apiKey) {
        this.httpFactory = httpFactory;
        this.host = host;
        this.apiKey = apiKey.clone();
        this.taskListener = taskListener;
    }

    @Override
    public void close() {
        Arrays.fill(this.apiKey, '*');
    }

    protected <T> T doHttpGet(String address, Type type) throws HttpException {
        HttpClient client = null;
        try {
            client = httpFactory.build(this.apiKey);
            HttpResponse response = null;
            try {
                taskListener.getLogger().println("GET request to " + address);
                response = client.execute(new HttpGet(address));
                if (!isSuccessful(response)) {
                    throw new HttpException(response.getReasonPhrase());
                }
                if (type == Void.TYPE) {
                    return null;
                }

                return convertJsonToObject(response, type);
            } catch (IOException e) {
                throw new HttpException(e);
            } finally {
                closeResource(response);
            }
        } finally {
            closeResource(client);
        }
    }

    public <T> T convertJsonToObject(HttpResponse response, Type type) throws IOException {
        HttpEntity entity = ((ClassicHttpResponse) response).getEntity();
        Reader json = new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8);
        Gson gson = new Gson();
        return gson.fromJson(json, type);
    }

    private boolean isSuccessful(HttpResponse response) {
        return response.getCode() >= 200 && response.getCode() < 300;
    }

    private void closeResource(Object resource) {
        if (resource instanceof AutoCloseable) {
            try {
                ((AutoCloseable) resource).close();
            } catch (Exception e) {
                taskListener.error("Unable to close " + resource.getClass().getSimpleName() + " because of " + e.getMessage());
            }
        }
    }

}
