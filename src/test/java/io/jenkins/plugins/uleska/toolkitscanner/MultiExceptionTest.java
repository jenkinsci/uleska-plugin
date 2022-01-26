package io.jenkins.plugins.uleska.toolkitscanner;


import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MultiExceptionTest {

    @Test
    public void testAddMakesItNotEmpty(){
        //given
        MultiException multiException = new MultiException();

        //when
        multiException.add(new Exception());

        //then
        assertTrue(multiException.isNotEmpty());
    }

    @Test
    public void testIsNotEmptyIsFalseWhenEmpty(){
        //when
        MultiException multiException = new MultiException();
        //then
        assertFalse(multiException.isNotEmpty());
    }

    @Test
    public void testGetCauseReturnsCauseOfFirstException(){
        //given
        Exception rootCause = new Exception();

        MultiException multiException = new MultiException();
        multiException.add(new Exception(rootCause));
        multiException.add(new Exception());

        //when
        Throwable cause = multiException.getCause();

        //then
        assertEquals(rootCause, cause);
    }

    @Test
    public void testGetCauseReturnsNullWhenNoSubExceptions(){
        //given
        MultiException multiException = new MultiException();

        //when
        Throwable cause = multiException.getCause();

        //then
        assertNull(cause);
    }

    @Test
    public void testGetMessageReturnsMessagesOfAllExceptions(){
        //given
        String expectedMessage = "Null Pointer and JSON Format";

        MultiException multiException = new MultiException();
        multiException.add(new Exception("Null Pointer"));
        multiException.add(new Exception("JSON Format"));

        //when - then
        assertEquals(expectedMessage, multiException.getLocalizedMessage());
        assertEquals(expectedMessage, multiException.getMessage());
    }

    @Test
    public void testGetMessageReturnsBlankWhenNoSubExceptions(){
        //given
        MultiException multiException = new MultiException();

        //when - then
        assertEquals("", multiException.getLocalizedMessage());
        assertEquals("", multiException.getMessage());
    }

    @Test
    public void testGetStaceTraceReturnsTracesOfAllExceptions(){
        //given
        StackTraceElement stackTrace1 = new StackTraceElement("Foo", "bar", "Foo.java", 1);
        StackTraceElement stackTrace2 = new StackTraceElement("Fizz", "buzz", "Fizz.java", 1);

        Exception exception1 = new Exception();
        StackTraceElement[] fullTrace1 = {stackTrace1};
        exception1.setStackTrace(fullTrace1);

        Exception exception2 = new Exception();
        StackTraceElement[] fullTrace2 = {stackTrace2};
        exception2.setStackTrace(fullTrace2);

        MultiException multiException = new MultiException();
        multiException.add(exception1);
        multiException.add(exception2);

        //when
        StackTraceElement[] result = multiException.getStackTrace();

        //then
        assertTrue(Arrays.asList(result).contains(stackTrace1));
        assertTrue(Arrays.asList(result).contains(stackTrace2));
    }

    @Test
    public void testGetStaceTraceReturnsStackFromTestWhenNoSubExceptions(){
        //given
        MultiException multiException = new MultiException();

        //when
        StackTraceElement[] result = multiException.getStackTrace();

        //then
        StackTraceElement firstElement = result[0];
        assertEquals(this.getClass().getName(), firstElement.getClassName());
        assertEquals(this.getClass().getSimpleName() + ".java", firstElement.getFileName());
    }

    @Test
    public void printStackTraceCallsAllSubStaceTraces(){
        //given
        Exception exception1 = mock(Exception.class);
        Exception exception2 = mock(Exception.class);

        MultiException multiException = new MultiException();
        multiException.add(exception1);
        multiException.add(exception2);

        //when
        multiException.printStackTrace();

        //then
        verify(exception1).printStackTrace();
        verify(exception2).printStackTrace();
    }
}
