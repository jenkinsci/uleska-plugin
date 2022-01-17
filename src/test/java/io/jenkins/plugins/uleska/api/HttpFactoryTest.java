package io.jenkins.plugins.uleska.api;

import org.apache.hc.client5.http.classic.HttpClient;


import org.junit.Test;


import static org.junit.Assert.assertNotNull;

public class HttpFactoryTest {


    @Test
    public void testBuildReturnsHttpClient(){
        //given
        HttpFactory factory = new HttpFactory();

        //when
        HttpClient client = factory.build( "123".toCharArray());

        //then
        assertNotNull(client);
    }



}
