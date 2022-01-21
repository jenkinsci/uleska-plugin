package io.jenkins.plugins.uleska.api;

import hudson.model.TaskListener;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.util.ReflectionUtils;



import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class BaseHttpApiTest {


    private static final String HOST = "https://cloud.uleska.com";
    private static final char[] API_KEY = {'1', '2', '3'};


    private BaseHttpApi api;

    @Mock
    private HttpFactory httpFactory;

    @Mock
    private TaskListener taskListener;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        api = new BaseHttpApi(taskListener, httpFactory, HOST, API_KEY);
    }

    @Test
    public void testCloseClearsApiKeyFromMemory() {
        //when
        api.close();

        //then
        Optional<Character[]> mayApiKey =  getApiKeyViaReflection(api);


        assertTrue(mayApiKey.isPresent());
        assertEquals(3, mayApiKey.get().length);
        for(char letter : mayApiKey.get()){
            assertEquals('*', letter);
        }
    }

    private Optional<Character[]> getApiKeyViaReflection(Object obj){
        Field apiKeyField = ReflectionUtils.findField(obj.getClass(), "apiKey");
        if(apiKeyField == null){
            return Optional.empty();
        }
        ReflectionUtils.makeAccessible(apiKeyField);
        char[] apiKey = (char[])ReflectionUtils.getField(apiKeyField, obj);
        if(apiKey == null){
            return Optional.empty();
        }
        Character[] apiKeyWrapper = new Character[apiKey.length];
        for(int i =0 ; i < apiKey.length; i++){
            apiKeyWrapper[i] = apiKey[i];
        }
        return Optional.of(apiKeyWrapper);
    }
}

