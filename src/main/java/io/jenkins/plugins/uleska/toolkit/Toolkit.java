package io.jenkins.plugins.uleska.toolkit;

import java.util.UUID;

public class Toolkit {

    public Toolkit(String name){
        this.name = name;
    }

    private UUID id;
    private String name;
    private boolean uleskaApproved;

    public UUID getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public boolean isUleskaApproved(){
        return uleskaApproved;
    }


}