package io.jenkins.plugins.uleska.toolkit;

import java.util.Optional;

public class ToolkitLocator  implements AutoCloseable{

    private final ToolkitApi toolkitApi;

    public ToolkitLocator(ToolkitApi toolkitApi){
        this.toolkitApi = toolkitApi;
    }

    public Optional<Toolkit> findByName(String name) {
        return toolkitApi.fetchToolkits().stream().filter(toolkit -> doNamesMatch(toolkit, name)).findAny();
    }

    @Override
    public void close() throws Exception {
        this.toolkitApi.close();
    }

    private boolean doNamesMatch(Toolkit toolkit, String searchName){
        if(toolkit.getName() == null){
            return false;
        }
        return toolkit.getName().trim().equalsIgnoreCase(searchName.trim());
    }

}
