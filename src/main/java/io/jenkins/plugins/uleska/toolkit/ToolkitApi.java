package io.jenkins.plugins.uleska.toolkit;

import java.util.Collection;

public interface ToolkitApi extends AutoCloseable{

    Collection<Toolkit> fetchToolkits();

}
