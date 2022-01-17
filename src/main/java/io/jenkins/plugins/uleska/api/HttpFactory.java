package io.jenkins.plugins.uleska.api;


import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.message.BufferedHeader;
import org.apache.hc.core5.util.CharArrayBuffer;

import java.util.Collections;

public class HttpFactory {


    public HttpClient build(char[] apiToken){
        CharArrayBuffer charBuffer = buildAuthHeaderValue(apiToken);
        Header authHeader = BufferedHeader.create(charBuffer);
        return HttpClientBuilder.create().setDefaultHeaders(Collections.singletonList(authHeader)).build();
    }

    private CharArrayBuffer buildAuthHeaderValue(char[] apiKey){
        String colonAndBearer = ":Bearer ";
        int headerLength = HttpHeaders.AUTHORIZATION.length() + colonAndBearer.length() + apiKey.length;
        CharArrayBuffer charBuffer = new CharArrayBuffer(headerLength);
        charBuffer.append(HttpHeaders.AUTHORIZATION);
        charBuffer.append(colonAndBearer);
        charBuffer.append(apiKey, 0, apiKey.length);
        return charBuffer;
    }

}
