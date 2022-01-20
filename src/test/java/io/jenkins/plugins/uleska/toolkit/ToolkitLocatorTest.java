package io.jenkins.plugins.uleska.toolkit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

public class ToolkitLocatorTest {

    @InjectMocks
    private ToolkitLocator toolkitLocator;

    @Mock
    private ToolkitApi toolkitApi;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFindToolkitByNameReturnsNothingWhenNameIsNull() {
        //given
        String name = null;

        //when
        Optional<Toolkit> maybeToolkit = toolkitLocator.findByName(name);

        //then
        assertFalse(maybeToolkit.isPresent());
    }

    @Test
    public void testFindToolkitByNameReturnsNothingWhenNameIsEmpty() {
        //given
        String name = "    ";

        //when
        Optional<Toolkit> maybeToolkit = toolkitLocator.findByName(name);

        //then
        assertFalse(maybeToolkit.isPresent());
    }

    @Test
    public void testFindByNameReturnsToolkitWhenNameIsTheSame() {
        //given
        String name = "My toolkit";
        Toolkit expectedToolkit = new Toolkit(name);
        List<Toolkit> toolkits = Arrays.asList(new Toolkit("Bob"), expectedToolkit, new Toolkit("Sally"));
        given(toolkitApi.fetchToolkits()).willReturn(toolkits);

        //when
        Optional<Toolkit> maybeToolkit = toolkitLocator.findByName(name);

        //then
        assertTrue(maybeToolkit.isPresent());
        assertEquals(expectedToolkit, maybeToolkit.get());
    }

    @Test
    public void testFindByNameReturnsToolkitWhenNameIsTheSameButWithSpaces() {
        //given
        String name = "My toolkit";
        String nameWithSpaces = "   " + name + "   ";
        Toolkit expectedToolkit = new Toolkit(nameWithSpaces);
        List<Toolkit> toolkits = Arrays.asList(new Toolkit("Bob"), expectedToolkit, new Toolkit("Sally"));
        given(toolkitApi.fetchToolkits()).willReturn(toolkits);

        //when
        Optional<Toolkit> maybeToolkit = toolkitLocator.findByName(name);

        //then
        assertTrue(maybeToolkit.isPresent());
        assertEquals(expectedToolkit, maybeToolkit.get());
    }

    @Test
    public void testFindByNameReturnsToolkitWhenNameHasDifferentCase() {
        //given
        String name = "My ToolKit";
        String nameLowerCase = "My toolkit";
        Toolkit expectedToolkit = new Toolkit(nameLowerCase);
        List<Toolkit> toolkits = Arrays.asList(new Toolkit("Bob"), expectedToolkit, new Toolkit("Sally"));
        given(toolkitApi.fetchToolkits()).willReturn(toolkits);

        //when
        Optional<Toolkit> maybeToolkit = toolkitLocator.findByName(name);

        //then
        assertTrue(maybeToolkit.isPresent());
        assertEquals(expectedToolkit, maybeToolkit.get());
    }

    @Test
    public void testFindByNameReturnsToolkitWhenNameHasDifferentCaseAndSpaces() {
        //given
        String name = "My ToolKit\t";
        String nameLowerCase = "My toolkit";
        Toolkit expectedToolkit = new Toolkit(nameLowerCase);
        List<Toolkit> toolkits = Arrays.asList(new Toolkit("Bob"), expectedToolkit, new Toolkit("Sally"));
        given(toolkitApi.fetchToolkits()).willReturn(toolkits);

        //when
        Optional<Toolkit> maybeToolkit = toolkitLocator.findByName(name);

        //then
        assertTrue(maybeToolkit.isPresent());
        assertEquals(expectedToolkit, maybeToolkit.get());
    }

}
