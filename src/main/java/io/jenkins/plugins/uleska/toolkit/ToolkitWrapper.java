package io.jenkins.plugins.uleska.toolkit;

public class ToolkitWrapper {

    private final Toolkit toolkit;

    public ToolkitWrapper(Toolkit toolkit){
        this.toolkit = toolkit;
    }

    public Toolkit getToolkit(){
        return toolkit;
    }

}
