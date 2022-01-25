package io.jenkins.plugins.uleska.toolkit;

import java.util.UUID;

public class Toolkit {

    public Toolkit(UUID id, String name){
        this.id = id;
        this.name = name;
    }

    public Toolkit(String name){
        this.name = name;
    }

    private UUID id;
    private String name;

    public UUID getId(){
        return id;
    }

    public String getName(){
        return name;
    }

}
