package io.jenkins.plugins.uleska;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class UleskaInstanceTest {

    @Test
    public void testGetNameReturnsName() {
        // Given
        String expected = "some instance";
        UleskaInstance uleskaInstance = new UleskaInstance(expected, "", "");

        // When
        String actual = uleskaInstance.getName();

        // Then
        assertEquals(expected, actual);
    }

    @Test
    public void testGetUrlReturnsUrl() {
        // Given
        String expected = "http://uleska.com";
        UleskaInstance uleskaInstance = new UleskaInstance("", expected, "");

        // When
        String actual = uleskaInstance.getUrl();

        // Then
        assertEquals(expected, actual);
    }

    @Test
    public void testGetCredentialsIdReturnsCredentialsId() {
        // Given
        String expected = "Dev Api Key";
        UleskaInstance uleskaInstance = new UleskaInstance("", "", expected);

        // When
        String actual = uleskaInstance.getCredentialsId();

        // Then
        assertEquals(expected, actual);
    }

    @Test
    public void testIsNameValidReturnsTrueForNonEmptyString() {
        // Given
        String name = "Live";

        // When
        boolean result = UleskaInstance.isNameValid(name);

        // Then
        assertTrue(result);
    }

    @Test
    public void testIsNameValidReturnsFalseForNullString() {
        // Given / When
        boolean result = UleskaInstance.isNameValid(null);

        // Then
        assertFalse(result);
    }

    @Test
    public void testIsNameValidReturnsFalseForEmptyString() {
        // Given
        String name = "   ";

        // When
        boolean result = UleskaInstance.isNameValid(name);

        // Then
        assertFalse(result);
    }

    @Test
    public void testIsUrlValidReturnsTrueForValidUrl() {
        // Given
        String url = "http://uleska.com";

        // When
        boolean result = UleskaInstance.isUrlValid(url);

        // Then
        assertTrue(result);
    }

    @Test
    public void testIsUrlValidReturnsFalseForUrlWithoutSchema() {
        // Given
        String url = "uleska.com";

        // When
        boolean result = UleskaInstance.isUrlValid(url);

        // Then
        assertFalse(result);
    }

    @Test
    public void testIsUrlValidReturnsFalseForUrlWithPath() {
        // Given
        String url = "http://uleska.com/path";

        // When
        boolean result = UleskaInstance.isUrlValid(url);

        // Then
        assertFalse(result);
    }

    @Test
    public void testIsUrlValidReturnsFalseForUrlWithInvalidCharacters() {
        // Given
        String url = "http://ules;ka.com";

        // When
        boolean result = UleskaInstance.isUrlValid(url);

        // Then
        assertFalse(result);
    }

    @Test
    public void testIsCredentialsIdValidReturnsTrueForNonEmptyString() {
        // Given
        String credentialsId = "Live";

        // When
        boolean result = UleskaInstance.isCredentialsIdValid(credentialsId);

        // Then
        assertTrue(result);
    }

    @Test
    public void testIsCredentialsIdValidReturnsFalseForNullString() {
        // Given / When
        boolean result = UleskaInstance.isCredentialsIdValid(null);

        // Then
        assertFalse(result);
    }

    @Test
    public void testIsCredentialsIdValidReturnsFalseForEmptyString() {
        // Given
        String credentialsId = "   ";

        // When
        boolean result = UleskaInstance.isCredentialsIdValid(credentialsId);

        // Then
        assertFalse(result);
    }

    @Test
    public void getErrorsReturnsNullWhenValid() {
        // Given
        UleskaInstance instance = new UleskaInstance("Production Env", "http://uleska.com", "API Key");

        // When
        String result = instance.getErrors();

        // Then
        assertNull(result);
    }

    @Test
    public void getErrorsReturnsMessagesWhenNameAndCredentialsIdAreInvalid() {
        // Given
        UleskaInstance instance = new UleskaInstance("", "http://uleska.com", null);

        // When
        String result = instance.getErrors();

        // Then
        assertTrue(result.contains(Messages.UleskaInstance_Errors_NameInvalid()));
        assertTrue(result.contains(Messages.UleskaInstance_Errors_CredentialsInvalid()));
    }

    @Test
    public void getErrorsReturnsMessagesWhenUrlIsInvalid() {
        // Given
        UleskaInstance instance = new UleskaInstance("Production Env", "http:/uleska.com", "API Key");

        // When
        String result = instance.getErrors();

        // Then
        assertTrue(result.contains(Messages.UleskaInstance_Errors_UrlInvalid()));
    }

}
