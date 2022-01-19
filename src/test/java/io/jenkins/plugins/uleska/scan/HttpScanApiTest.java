package io.jenkins.plugins.uleska.scan;

import hudson.model.TaskListener;
import io.jenkins.plugins.uleska.api.HttpFactory;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

public class HttpScanApiTest {


    private static final String HOST = "https://cloud.uleska.com";
    private static final char[] API_KEY = {'1', '2', '3'};


    private ScanApi scanApi;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpFactory httpFactory;

    @Mock
    private TaskListener taskListener;

    @Mock
    private ClassicHttpResponse baseHttpResponse;


    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        given(httpFactory.build(any())).willReturn(httpClient);
        scanApi = new HttpScanApi(taskListener, httpFactory, HOST, API_KEY);

        given(httpClient.execute(any())).willReturn(baseHttpResponse);
        given(baseHttpResponse.getCode()).willReturn(HttpStatus.SC_OK);
        given(taskListener.getLogger()).willReturn( new PrintStream(NullOutputStream.NULL_OUTPUT_STREAM, false));
    }



    @Test
    public void testScanThrowsNoErrorWhenHttpIsSuccessful(){
        //given
        UUID applicationId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();

        //when
        boolean noErrors = true;
        try {
            scanApi.doScan(applicationId, versionId);
        }catch(Exception e){
            noErrors = false;
        }
        //then
        assertTrue(noErrors);
    }

    @Test
    public void testScanHitsTheHttpBackend() throws IOException, ScanException {
        //given
        UUID applicationId = UUID.fromString("7203578c-cdcc-469b-a944-e945eeb7d48d");
        UUID versionId = UUID.fromString("ab81c383-2cb9-4855-bb29-1b94dcf9cde6");

        String expectedUrl = "/SecureDesigner/api/v1/applications/7203578c-cdcc-469b-a944-e945eeb7d48d/versions/ab81c383-2cb9-4855-bb29-1b94dcf9cde6/scan";

        //when
        scanApi.doScan(applicationId, versionId);

        //then
        verify(httpClient).execute(argThat(this::correctHost));
        verify(httpClient).execute(argThat(request ->
             request.getRequestUri().equals(expectedUrl)
        ));
    }

    @Test(expected = ScanException.class)
    public void testScanThrowsErrorWhenResponseIsServerError() throws IOException, ScanException {
        //given
        UUID applicationId = UUID.fromString("a59458ca-20ec-4303-9dfa-61a2625df86e");
        UUID versionId = UUID.fromString("d4b6233a-20cd-49c0-b04a-696031e7723d");
        String expectedUrl = "/SecureDesigner/api/v1/applications/a59458ca-20ec-4303-9dfa-61a2625df86e/versions/d4b6233a-20cd-49c0-b04a-696031e7723d/scan";

        given(httpClient.execute(argThat(request -> request.getRequestUri().equals(expectedUrl)))).willReturn(baseHttpResponse);
        given(baseHttpResponse.getCode()).willReturn(HttpStatus.SC_SERVER_ERROR);

        //when - then
        scanApi.doScan(applicationId, versionId);
    }

    @Test(expected = ScanException.class)
    public void testScanThrowsErrorWhenIoException() throws IOException, ScanException {
        //given
        UUID applicationId = UUID.fromString("a59458ca-20ec-4303-9dfa-61a2625df86e");
        UUID versionId = UUID.fromString("d4b6233a-20cd-49c0-b04a-696031e7723d");
        String expectedUrl = "/SecureDesigner/api/v1/applications/a59458ca-20ec-4303-9dfa-61a2625df86e/versions/d4b6233a-20cd-49c0-b04a-696031e7723d/scan";

        given(httpClient.execute(argThat(request -> request.getRequestUri().equals(expectedUrl)))).willThrow(new IOException());

        //when - then
        scanApi.doScan(applicationId, versionId);
    }

    private boolean correctHost(ClassicHttpRequest request){
        try {
            URI uri = request.getUri();
            return uri.getHost().equals("cloud.uleska.com");
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
