package io.jenkins.plugins.uleska.toolkitscanner;

import hudson.model.TaskListener;
import io.jenkins.plugins.uleska.scan.ScanApi;
import io.jenkins.plugins.uleska.scan.ScanException;
import io.jenkins.plugins.uleska.toolkit.Toolkit;
import io.jenkins.plugins.uleska.toolkit.ToolkitLocator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

public class UleskaToolkitScannerTest {

    @InjectMocks
    private UleskaToolkitScanner scanner;

    @Mock
    private ScanApi scanApi;

    @Mock
    private ToolkitLocator toolkitLocator;

    @Spy
    private TaskListener taskListener = TaskListener.NULL;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testPerformScanWhenAppIdIsInvalid() {
        //given
        String appId = "This is not a UUID";
        String versionId = "ea8cfc5b-c5da-45e7-9108-ea1f66164c0c";
        String toolkitName = "Uleska Code Scan";

        //when
        boolean successful = scanner.performScan(appId, versionId, toolkitName);

        //then
        assertFalse(successful);
    }

    @Test
    public void testPerformScanWhenVersionIdIsInvalid() {
        //given
        String appId = "24f071ef-6cfd-4f70-b15a-fb2161c2baa0";
        String versionId = "I don't think I'm a UUID";
        String toolkitName = "Uleska Web Scan";

        //when
        boolean successful = scanner.performScan(appId, versionId, toolkitName);

        //then
        assertFalse(successful);
    }

    @Test
    public void testPerformScanReturnsTrueWhenScanApiIsHit() throws ScanException {
        //given
        UUID appId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        UUID toolkitId = UUID.randomUUID();
        String toolkitName = "Uleska Web Scan";
        Toolkit toolkit = new Toolkit(toolkitId, toolkitName);
        given(toolkitLocator.findByName(toolkitName)).willReturn(Optional.of(toolkit));

        //when
        boolean successful = scanner.performScan(appId.toString(), versionId.toString(), toolkitName);

        //then
        verify(scanApi).doScan(appId, versionId, toolkitId);
        assertTrue(successful);
    }

    @Test
    public void testPerformScanReturnsFalseWhenToolkitCanNotBeFound() {
        //given
        UUID appId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        String toolkitName = "Not a toolkit";
        given(toolkitLocator.findByName(toolkitName)).willReturn(Optional.empty());

        //when
        boolean successful = scanner.performScan(appId.toString(), versionId.toString(), toolkitName);

        //then
        assertFalse(successful);
    }

    @Test
    public void testPerformScanReturnsFalseWhenScanApiThrowsException() throws ScanException {
        //given
        UUID appId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        UUID toolkitId = UUID.randomUUID();
        String toolkitName = "Uleska Web Scan";
        Toolkit toolkit = new Toolkit(toolkitId, toolkitName);
        given(toolkitLocator.findByName(toolkitName)).willReturn(Optional.of(toolkit));
        doThrow(new ScanException(new Exception())).when(scanApi).doScan(appId, versionId, toolkitId);

        //when
        boolean successful = scanner.performScan(appId.toString(), versionId.toString(), toolkitName);

        //then
        assertFalse(successful);
    }

    @Test
    public void testCloseClosesBothToolkitLocatorAndScanApi() throws Exception {
        //when
        scanner.close();

        //then
        verify(toolkitLocator).close();
        verify(scanApi).close();
    }

    @Test(expected = Exception.class)
    public void testCloseClosesBothToolkitLocatorAndScanApiWithToolkitLocatorError() throws Exception {
        //given
        doThrow(new Exception()).when(toolkitLocator).close();
        //when
        scanner.close();

        //then
        verify(toolkitLocator).close();
        verify(scanApi).close();
    }

    @Test(expected = Exception.class)
    public void testCloseClosesBothToolkitLocatorAndScanApiWithScanApiError() throws Exception {
        //given
        doThrow(new Exception()).when(scanApi).close();
        //when
        scanner.close();

        //then
        verify(toolkitLocator).close();
        verify(scanApi).close();
    }

}
