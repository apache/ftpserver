package org.apache.ftpserver.config.spring;

import org.apache.ftpserver.ftplet.DefaultFtplet;

public class TestFtplet extends DefaultFtplet {

    private int foo;

    public int getFoo() {
        return foo;
    }

    public void setFoo(int foo) {
        this.foo = foo;
    }
    
}
