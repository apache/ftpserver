package org.apache.ftpserver.config.spring;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.ftpserver.DefaultCommandFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.command.HELP;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.listener.mina.MinaListener;
import org.apache.mina.filter.firewall.Subnet;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;

public class SpringConfigTest extends TestCase {

    public void test() throws Throwable {
        XmlBeanFactory factory = new XmlBeanFactory(new FileSystemResource("src/test/resources/spring-config/config-spring-1.xml"));

        FtpServer server = (FtpServer) factory.getBean("server");
        server.start();
        
        Map<String, Listener> listeners = server.getServerContext().getListeners(); 
        assertEquals(3, listeners.size());
        
        Listener listener = listeners.get("listener0");
        assertNotNull(listener);
        assertTrue(listener instanceof MinaListener);
        assertEquals(2222, ((MinaListener)listener).getPort());
        
        List<Subnet> subnets = ((MinaListener)listener).getBlockedSubnets();
        assertEquals(3, subnets.size());
        assertEquals(new Subnet(InetAddress.getByName("1.2.3.0"), 16), subnets.get(0));
        assertEquals(new Subnet(InetAddress.getByName("1.2.4.0"), 16), subnets.get(1));
        assertEquals(new Subnet(InetAddress.getByName("1.2.3.4"), 32), subnets.get(2));
        
        listener = listeners.get("listener1");
        assertNotNull(listener);
        assertTrue(listener instanceof MinaListener);
        assertEquals(2223, ((MinaListener)listener).getPort());
        
        listener = listeners.get("listener2");
        assertNotNull(listener);
        assertTrue(listener instanceof MinaListener);
        assertEquals(2224, ((MinaListener)listener).getPort());
        
        DefaultCommandFactory cf = (DefaultCommandFactory) server.getServerContext().getCommandFactory();
        assertEquals(1, cf.getCommandMap().size());
        assertTrue(cf.getCommand("FOO") instanceof HELP);
        
        String[] languages = server.getServerContext().getMessageResource().getAvailableLanguages();
        
        assertEquals(3, languages.length);
        assertEquals("se", languages[0]);
        assertEquals("no", languages[1]);
        assertEquals("da", languages[2]);
    }
}
