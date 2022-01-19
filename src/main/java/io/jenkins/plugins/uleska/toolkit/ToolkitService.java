package io.jenkins.plugins.uleska.toolkit;

import java.util.Optional;

public class ToolkitService {

    private final ToolkitApi toolkitApi;

    public ToolkitService(ToolkitApi toolkitApi){
        this.toolkitApi = toolkitApi;
    }

    public Optional<Toolkit> findByName(String name) {
        return toolkitApi.fetchToolkits().stream().filter(toolkit -> doNamesMatch(toolkit, name)).findAny();
    }

    private boolean doNamesMatch(Toolkit toolkit, String searchName){
        if(toolkit.getName() == null){
            return false;
        }
        return toolkit.getName().trim().equalsIgnoreCase(searchName.trim());
    }

}
