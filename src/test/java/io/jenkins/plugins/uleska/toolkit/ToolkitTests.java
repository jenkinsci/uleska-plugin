package io.jenkins.plugins.uleska.toolkit;

import com.google.gson.Gson;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ToolkitTests {


    @Test
    public void testCreateToolkitViaJsonHasCorrectData(){
        //given
        UUID expectedId = UUID.fromString("de26f7b4-d2cc-405c-a459-127d730d65d6");
        String expectedName = "tom";
        String json = "{ toolkit: { id: 'de26f7b4-d2cc-405c-a459-127d730d65d6', name : 'tom', uleskaApproved : true }, tools : [] }";
        Gson gson = new Gson();

        //when
        ToolkitWrapper toolkitWrapper = gson.fromJson(json, ToolkitWrapper.class);
        Toolkit toolkit = toolkitWrapper.getToolkit();

        //then
        assertEquals(expectedId, toolkit.getId());
        assertEquals(expectedName, toolkit.getName());
        assertTrue(toolkit.isUleskaApproved());
    }
}
