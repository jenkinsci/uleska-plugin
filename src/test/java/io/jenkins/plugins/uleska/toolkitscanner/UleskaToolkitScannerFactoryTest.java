package io.jenkins.plugins.uleska.toolkitscanner;

import hudson.model.TaskListener;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class UleskaToolkitScannerFactoryTest {


    @Test
    public void testBuildReturnsInstanceOfScanner() throws Exception {
        char[] apiKey = {'a', 'b', 'c'};
        UleskaToolkitScannerFactory factory = new UleskaToolkitScannerFactory();
        try(UleskaToolkitScanner scanner = factory.build(TaskListener.NULL, "https://cloud.uleska.com", apiKey)){
            assertNotNull(scanner);
        }

    }

}
