package io.jenkins.plugins.uleska.toolkitscanner;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class MultiException extends Exception{
    private final List<Exception> exceptions = new LinkedList<>();

    public void add(Exception e){
        this.exceptions.add(e);
    }

    @Override
    public String getMessage() {
        return this.exceptions.stream().map(Exception::getMessage).collect(Collectors.joining(" and "));
    }

    @Override
    public synchronized Throwable getCause() {
        if(this.isNotEmpty()){
           return exceptions.get(0).getCause();
        }
        return null;
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        if(this.isNotEmpty()){
            return exceptions.stream()
                .flatMap(e -> Arrays.stream(e.getStackTrace()))
                .toArray(StackTraceElement[]::new);
        }
        return super.getStackTrace();
    }

    @Override
    public void printStackTrace() {
        if(this.isNotEmpty()){
            exceptions.forEach(Exception::printStackTrace);
        }else {
            super.printStackTrace();
        }

    }

    public boolean isNotEmpty(){
        return !this.exceptions.isEmpty();
    }
}
