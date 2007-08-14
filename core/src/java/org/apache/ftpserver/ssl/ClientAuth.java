package org.apache.ftpserver.ssl;

public class ClientAuth {
    public static final ClientAuth NEED = new ClientAuth("Need");
    public static final ClientAuth WANT = new ClientAuth("Want");
    public static final ClientAuth NONE = new ClientAuth("None");
    
    private String type;
    
    private ClientAuth(String type) {
        this.type = type;
    }

    public String toString() {
        return type;
    }
}