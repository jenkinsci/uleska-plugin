package io.jenkins.plugins.uleska.api;

public class HttpException extends Exception{

    public HttpException(Throwable e){
        super(e);
    }

    public HttpException(String message){
        super(message);
    }
}
