package io.jenkins.plugins.uleska.toolkit;

import hudson.model.TaskListener;
import io.jenkins.plugins.uleska.api.HttpFactory;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class HttpToolkitApiTest {

    private static final String HOST = "https://cloud.uleska.com";
    private static final char[] API_KEY = {'1', '2', '3'};
    private static final String ADDRESS = "/SecureDesigner/api/v1/toolkits";

    private ToolkitApi toolkitApi;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpFactory httpFactory;

    @Mock
    private TaskListener taskListener;


    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        given(httpFactory.build(any())).willReturn(httpClient);
        toolkitApi = new HttpToolkitApi(taskListener, httpFactory, HOST, API_KEY);
        given(taskListener.getLogger()).willReturn( new PrintStream(NullOutputStream.NULL_OUTPUT_STREAM, false));
    }

    @Test
    public void testFetchToolkitsReturnsCollectionOfToolkits() throws IOException {
        //given
        HttpResponse response = mockHttpResponse(HttpStatus.SC_OK,  stringInputStream("[]"));
        given(httpClient.execute(argThat(request -> request.getRequestUri().equals(ADDRESS)))).willReturn(response);

        //when
        Collection<Toolkit> toolkits = toolkitApi.fetchToolkits();

        //then
        assertNotNull(toolkits);
    }

    @Test
    public void testFetchToolkitsReturnsCorrectData() throws IOException {
        //given
        InputStream json = this.getClass().getResourceAsStream("/toolkits.json");
        HttpResponse response = mockHttpResponse(HttpStatus.SC_OK, json);
        given(httpClient.execute(argThat(request -> request.getRequestUri().equals(ADDRESS)))).willReturn(response);

        //when
        Collection<Toolkit> toolkits = toolkitApi.fetchToolkits();

        //then
        assertEquals(5, toolkits.size());
        List<String> expectedNames = new ArrayList<>(5);
        expectedNames.addAll(Arrays.asList("first", "second", "third", "fourth", "fifth"));
        toolkits.forEach(toolkit -> {
            assertTrue(expectedNames.contains(toolkit.getName()));
            expectedNames.removeIf(name -> name.equals(toolkit.getName()));
        });
        assertTrue(expectedNames.isEmpty());
    }

    @Test
    public void testFetchToolkitsReturnsEmptyCollectionWhenServerFails() throws IOException {
        //given
        HttpResponse response = mockHttpResponse(HttpStatus.SC_SERVER_ERROR,  stringInputStream("Server Error"));
        given(httpClient.execute(argThat(request -> request.getRequestUri().equals(ADDRESS)))).willReturn(response);

        //when
        Collection<Toolkit> toolkits = toolkitApi.fetchToolkits();

        //then
        assertTrue(toolkits.isEmpty());
    }

    private BasicClassicHttpResponse mockHttpResponse(int status, InputStream body) throws IOException {
        BasicClassicHttpResponse response = mock(BasicClassicHttpResponse.class);
        HttpEntity entity = mock(HttpEntity.class);

        given(response.getCode()).willReturn(status);
        given(response.getEntity()).willReturn(entity);
        given(entity.getContent()).willReturn(body);
        return response;
    }

    private InputStream stringInputStream(String data){
        return new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
    }




}
