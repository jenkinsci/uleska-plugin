package io.jenkins.plugins.uleska.api;

public class ScanException extends Exception{

    public ScanException(Throwable e){
        super(e);
    }

    public ScanException(String message){
        super(message);
    }

}
