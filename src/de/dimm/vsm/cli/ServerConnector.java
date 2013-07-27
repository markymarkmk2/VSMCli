/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.cli;

import de.dimm.vsm.Utilities.DefaultSSLSocketFactory;
import de.dimm.vsm.net.RemoteCallFactory;
import de.dimm.vsm.net.interfaces.AgentApi;
import de.dimm.vsm.net.interfaces.GuiLoginApi;
import de.dimm.vsm.net.interfaces.GuiServerApi;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.security.Security;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;



/**
 *
 * @author Administrator
 */
public class ServerConnector
{
    
  

    private static class CustomizedHostnameVerifier implements HostnameVerifier
    {
        @Override
        public boolean verify( String hostname, SSLSession session )
        {
            return true;
        }
    }

    public class AgentApiEntry
    {
        AgentApi api;
        RemoteCallFactory factory;

        public AgentApiEntry( AgentApi api, RemoteCallFactory factory )
        {
            this.api = api;
            this.factory = factory;
        }
        
    }
    public class GuiServerApiEntry
    {
        GuiServerApi api;
        RemoteCallFactory factory;

        public GuiServerApiEntry( GuiServerApi api, RemoteCallFactory factory )
        {
            this.api = api;
            this.factory = factory;
        }
        
    }
    public class GuiLoginApiEntry
    {
        GuiLoginApi api;
        RemoteCallFactory factory;

        public GuiLoginApiEntry( GuiLoginApi api, RemoteCallFactory factory )
        {
            this.api = api;
            this.factory = factory;
        }
        
    }
    
    
    public ServerConnector(String keyStore, String keyPwd)
    {
//        System.setProperty("javax.net.ssl.trustStore", getKeyStore());
//        System.setProperty("javax.net.ssl.keyStore", getKeyStore());
//        System.setProperty("javax.net.ssl.keyStorePassword", getKeyPwd());   
        // Wegen selbst signiertem Zerifikat
        HttpsURLConnection.setDefaultHostnameVerifier(new CustomizedHostnameVerifier());

        DefaultSSLSocketFactory.setKeyStore(keyStore);
        DefaultSSLSocketFactory.setPassword(keyPwd.toCharArray());
        Security.setProperty( "ssl.SocketFactory.provider", DefaultSSLSocketFactory.class.getCanonicalName());        
    }

    public GuiServerApiEntry connectServerApi( InetAddress adress, int port, long loginId, boolean ssl, boolean tcp )
    {
        try
        {
            GuiServerApiEntry api = generate_server_api(adress, port, ssl, "ui?Id=" + loginId,  tcp);
            return api;
        }
        catch (Exception e)
        {
            System.out.println("Connect to Server " + adress.toString() + " failed: " + e.toString());
        }
        return null;
    }
    
    public GuiLoginApiEntry connectLoginApi( InetAddress adress, int port, boolean ssl, boolean tcp )
    {
        try
        {
            GuiLoginApiEntry api = generate_login_api(adress, port, ssl, "login",  tcp);
            return api;
        }
        catch (Exception e)
        {
            System.out.println("Connect to Server " + adress.toString() + " failed: " + e.toString());
        }
        return null;
    }
    
    public AgentApiEntry connectAgentApi( InetAddress adress, int port, boolean ssl, boolean tcp )
    {
        try
        {
            AgentApiEntry api = generate_agent_api(adress, port, ssl, "net", tcp);
            return api;
        }
        catch (Exception e)
        {
            System.out.println("Connect to Server " + adress.toString() + " failed: " + e.toString());
        }
        return null;
    }

    private AgentApiEntry generate_agent_api( InetAddress adress, int port, boolean ssl, String path,  boolean tcp )
    {
        AgentApiEntry apiEntry = null;        
        try
        {
            RemoteCallFactory factory = new RemoteCallFactory(adress, port, path, ssl, tcp);
            AgentApi api = (AgentApi) factory.create(AgentApi.class);            
            apiEntry = new AgentApiEntry(api, factory);
        }
        catch (MalformedURLException malformedURLException)
        {
            System.out.println("Err: " + malformedURLException.getMessage());
        }
        return apiEntry;
    }
    
    private GuiServerApiEntry generate_server_api( InetAddress adress, int port, boolean ssl, String path, boolean tcp )
    {
        GuiServerApiEntry apiEntry = null;

        try
        {
            RemoteCallFactory factory = new RemoteCallFactory(adress, port, path, ssl, tcp);
            GuiServerApi api = (GuiServerApi) factory.create(GuiServerApi.class);
            apiEntry = new GuiServerApiEntry(api, factory);
        }
        catch (MalformedURLException malformedURLException)
        {
            System.out.println("Err: " + malformedURLException.getMessage());
        }
        return apiEntry;
    }
    
    private GuiLoginApiEntry generate_login_api( InetAddress adress, int port, boolean ssl, String path,  boolean tcp )
    {
        GuiLoginApiEntry apiEntry = null;

        try
        {
            RemoteCallFactory factory = new RemoteCallFactory(adress, port, path, ssl, tcp);

            GuiLoginApi api = (GuiLoginApi) factory.create(GuiLoginApi.class);
            apiEntry = new GuiLoginApiEntry(api, factory);
        }
        catch (MalformedURLException malformedURLException)
        {
            System.out.println("Err: " + malformedURLException.getMessage());
        }
        return apiEntry;
    }
}
