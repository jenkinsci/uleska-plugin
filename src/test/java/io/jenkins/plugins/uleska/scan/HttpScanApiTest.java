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
        given(taskListener.getLogger()).willReturn(new PrintStream(NullOutputStream.NULL_OUTPUT_STREAM, false));
    }

    @Test
    public void testScanThrowsNoErrorWhenHttpIsSuccessful() {
        //given
        UUID applicationId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();

        //when
        boolean noErrors = true;
        try {
            scanApi.doScan(applicationId, versionId);
        } catch (Exception e) {
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
    public void testScanThrowsErrorWhenResponseIsServerError() throws ScanException, IOException {
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

    @Test
    public void testScanToolkitThrowsNoErrorWhenHttpIsSuccessful() {
        //given
        UUID applicationId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        UUID toolkitId = UUID.randomUUID();

        //when
        boolean noErrors = true;
        try {
            scanApi.doScan(applicationId, versionId, toolkitId);
        } catch (Exception e) {
            noErrors = false;
        }
        //then
        assertTrue(noErrors);
    }

    @Test
    public void testScanToolkitsHitsTheHttpBackend() throws IOException, ScanException {
        //given
        UUID applicationId = UUID.fromString("7203578c-cdcc-469b-a944-e945eeb7d48d");
        UUID versionId = UUID.fromString("ab81c383-2cb9-4855-bb29-1b94dcf9cde6");
        UUID toolkitId = UUID.fromString("4f730af6-84cb-486b-96ab-bc9f12b708f1");

        String expectedUrl = "/SecureDesigner/api/v1/applications/7203578c-cdcc-469b-a944-e945eeb7d48d/versions/ab81c383-2cb9-4855-bb29-1b94dcf9cde6/scan/4f730af6-84cb-486b-96ab-bc9f12b708f1";

        //when
        scanApi.doScan(applicationId, versionId, toolkitId);

        //then
        verify(httpClient).execute(argThat(this::correctHost));
        verify(httpClient).execute(argThat(request ->
            request.getRequestUri().equals(expectedUrl)
        ));
    }

    @Test(expected = ScanException.class)
    public void testScanToolkitThrowsErrorWhenResponseIsServerError() throws ScanException, IOException {
        //given
        UUID applicationId = UUID.fromString("a59458ca-20ec-4303-9dfa-61a2625df86e");
        UUID versionId = UUID.fromString("d4b6233a-20cd-49c0-b04a-696031e7723d");
        UUID toolkitId = UUID.fromString("95a88d86-4e68-4d7c-890a-24928d217abd");
        String expectedUrl = "/SecureDesigner/api/v1/applications/a59458ca-20ec-4303-9dfa-61a2625df86e/versions/d4b6233a-20cd-49c0-b04a-696031e7723d/scan/95a88d86-4e68-4d7c-890a-24928d217abd";

        given(httpClient.execute(argThat(request -> request.getRequestUri().equals(expectedUrl)))).willReturn(baseHttpResponse);
        given(baseHttpResponse.getCode()).willReturn(HttpStatus.SC_SERVER_ERROR);

        //when - then
        scanApi.doScan(applicationId, versionId, toolkitId);
    }

    @Test(expected = ScanException.class)
    public void testScanToolkitThrowsErrorWhenIoException() throws IOException, ScanException {
        //given
        UUID applicationId = UUID.fromString("a59458ca-20ec-4303-9dfa-61a2625df86e");
        UUID versionId = UUID.fromString("d4b6233a-20cd-49c0-b04a-696031e7723d");
        UUID toolkitId = UUID.fromString("57789c2c-a725-4c9f-8b77-ec3c8698ccb6");
        String expectedUrl = "/SecureDesigner/api/v1/applications/a59458ca-20ec-4303-9dfa-61a2625df86e/versions/d4b6233a-20cd-49c0-b04a-696031e7723d/scan/57789c2c-a725-4c9f-8b77-ec3c8698ccb6";

        given(httpClient.execute(argThat(request -> request.getRequestUri().equals(expectedUrl)))).willThrow(new IOException());

        //when - then
        scanApi.doScan(applicationId, versionId, toolkitId);
    }

    private boolean correctHost(ClassicHttpRequest request) {
        try {
            URI uri = request.getUri();
            return uri.getHost().equals("cloud.uleska.com");
        } catch (URISyntaxException e) {
            return false;
        }
    }

}
