/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.cli;

import de.dimm.vsm.auth.GuiUser;
import de.dimm.vsm.auth.User;
import de.dimm.vsm.cli.ServerConnector.GuiLoginApiEntry;
import de.dimm.vsm.cli.ServerConnector.GuiServerApiEntry;
import de.dimm.vsm.cli.commands.AbstractCommand;
import de.dimm.vsm.cli.commands.CommandManager;
import de.dimm.vsm.cli.commands.ICommand;
import de.dimm.vsm.log.LogListener;
import de.dimm.vsm.log.LogManager;
import de.dimm.vsm.net.GuiWrapper;
import de.dimm.vsm.net.interfaces.GuiLoginApi;
import de.dimm.vsm.net.interfaces.GuiServerApi;
import de.dimm.vsm.records.RoleOption;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class CliMain
{

    private GuiWrapper guiWrapper;
    private boolean loggedIn;
    GuiUser guiUser;
    GuiLoginApiEntry loginApi;
    GuiServerApiEntry serverApi;
    
    CliApi uiApi;
    
    InetAddress addr;
    int port; 
    String user; 
    String pwd;
    CommandManager cmdMgr;
    ServerConnector connector;
    boolean ssl = true;
    
    

    public CliMain()
    {
        
        cmdMgr = new CommandManager();
    }

    public boolean isSsl()
    {
        return ssl;
    }
    

    void readParams(String[] args) throws UnknownHostException
    {
        String keyStore = "client.jks";
        String keyPwd = "1234fuenf";            
        String host = "127.0.0.1";
        port = 8443; 
        user = "system";
        pwd = "helikon";
        
        for (int i = 0; i < args.length; i++)
        {
            String arg = args[i];
            if (arg.equals("-h") && i < args.length - 1)
            {
                host = args[i+1];
                i++;
            }
            if (arg.equals("-p") && i < args.length - 1)
            {
                port = Integer.parseInt(args[i+1]);
                i++;
            }
            if (arg.equals("-u") && i < args.length - 1)
            {
                user = args[i+1];
                i++;
            }
            if (arg.equals("-x") && i < args.length - 1)
            {
                pwd = args[i+1];
                i++;
            }
            if (arg.equals("--keyStore") && i < args.length - 1)
            {
                keyStore = args[i+1];
                i++;
            }
            if (arg.equals("--keyPwd") && i < args.length - 1)
            {
                keyPwd = args[i+1];
                i++;
            }
        }
        
        connector = new ServerConnector(keyStore, keyPwd);        
        addr = InetAddress.getByName(host);    
    }
             
             
    public void handleLogout() throws IOException
    {
        if (loginApi != null)
        {
            loginApi.api.logout(guiWrapper);
            loginApi.factory.close();
            loginApi = null;
        }

        guiWrapper = null;
        loggedIn = false;
        uiApi = null;
        
    }

    protected boolean checkAccesRight(User user)
    {
         return (user.getRole().hasRoleOption(RoleOption.RL_ALLOW_VIEW_PARAM) ||
                 user.getRole().hasRoleOption(RoleOption.RL_ALLOW_EDIT_PARAM) );

    }
    
    public void handleLogin(GuiWrapper wr, String name, String pwd ) throws IOException
    {
        if (wr != null)
        {
            if (!checkAccesRight( wr.getUser()))
            {
                notify( Txt("Sie haben keine Berechtigung für diesen Programmteil"), "");
                handleLogout();
            }
            else
            {
                guiWrapper = wr;
                loggedIn = true;
                uiApi = new CliApi(this);              
            }
        }
        else
        {
            handleLogout();
        }
    }
    public static String Txt( String s)
    {
        return s;
    }
    
    static void notify( String header, String txt) 
    {
        LogManager.msg_system(LogListener.LVL_INFO, header + ": " + txt);
    }
    static void error( String txt) 
    {
        LogManager.msg_system(LogListener.LVL_ERR, txt);
    }
    static void error( String txt, Exception exc) 
    {
        LogManager.msg_system(LogListener.LVL_ERR, txt, exc);
    }

    
    public GuiUser getGuiUser()
    {
        return guiUser;
    }    

    boolean isLoggedIn()
    {
        return loggedIn;
    }
    boolean logout() throws IOException
    {
        if (guiWrapper == null)
        {
            error("Not logged in");
            return false;
        }

        handleLogout();
        return true;
    }

    boolean tryLogin() throws IOException
    {
        GuiLoginApiEntry api = connector.connectLoginApi(addr, port, isSsl(), false);
        GuiWrapper wrapper = api.api.login(user, pwd);
        
        
        if (wrapper != null)
        {
            loginApi = api;
            serverApi = createGuiServerApi(wrapper);
            handleLogin(wrapper, user, pwd);
            return true;
        }
        else
        {
            handleLogout();

        }
        return false;
    }
    public GuiServerApi getGuiServerApi()
    {
        return serverApi.api;
    }
    public User getUser()
    {
        return guiWrapper.getUser();
    }

    public GuiServerApiEntry createGuiServerApi(GuiWrapper wrapper)
    {
        if (wrapper == null)
        {
            error("Not logged in");
            return null;
        }
        
        return connector.connectServerApi(addr, port, wrapper.getLoginIdx(), isSsl(), false);
    }
    
    public GuiLoginApi getGuiLoginApi()
    {
        return loginApi.api;
    }

    


    public static Object callLogicControl( String func )
    {
        try
        {
            return callLogicControl(func, true);
        }
        catch (Exception ex)
        {
            // CANNOT HAPPEN, IS CAUGHT INSIDE
        }
        return null;
    }
    public static Object callLogicControl( String func, boolean catchException ) throws Exception
    {
        try
        {
            Class cl = Class.forName("de.dimm.vsm.Main");
            Method get_control = cl.getMethod("get_control", (Class[]) null);
            Object logicControl = get_control.invoke(null, (Object[]) null);

            Class logic_class = Class.forName("de.dimm.vsm.LogicControl");
            Method m_func = logic_class.getMethod(func, (Class[]) null);
            Object result = m_func.invoke(logicControl, (Object[]) null);
            return result;
        }
        catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException exc)
        {
            if (!catchException)
                throw exc;


            if (exc instanceof InvocationTargetException)
            {
                InvocationTargetException ite = (InvocationTargetException)exc;
                notify( ite.getTargetException().getMessage(), "");
                return null;
            }
            error("Error in reflection call " + func + ":" + exc.getMessage(), exc);
        }
        return null;
    }       

    public static Object callLogicControl( String func, Object arg)
    {
        try
        {
            return callLogicControl(func, arg, true);
        }
        catch (Exception ex)
        {
            // CANNOT HAPPEN, IS CAUGHT INSIDE
        }
        return null;
    }

    public static Object callLogicControl( String func, Object arg, boolean catchException ) throws Exception
    {
        try
        {
            Class cl = Class.forName("de.dimm.vsm.Main");
            Class[] types = new Class[1];
            types[0] = arg.getClass();
            Object[] args = new Object[1];
            args[0] = arg;
            Method get_control = cl.getMethod("get_control", (Class[]) null);
            Object logicControl = get_control.invoke(null, (Object[]) null);

            Class logic_class = Class.forName("de.dimm.vsm.LogicControl");
            Method m_func = logic_class.getMethod(func, types);
            Object result = m_func.invoke(logicControl, args);
            return result;
        }
        catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException exc)
        {
            if (!catchException)
                throw exc;

            if (exc instanceof InvocationTargetException)
            {
                InvocationTargetException ite = (InvocationTargetException)exc;
                notify(ite.getTargetException().getMessage(), "");
                return null;
            }
            error("Error in reflection call " + func + ":" + exc.getMessage(), exc);
        }
        return null;
    }
    public static Object callLogicControl( String func, Object arg[])
    {
        try
        {
            return callLogicControl(func, arg, true);
        }
        catch (Exception ex)
        {
            // CANNOT HAPPEN, IS CAUGHT INSIDE
        }
        return null;
    }

    public static Object callLogicControl( String func, Object arg[], boolean catchException ) throws Exception
    {
        try
        {
            Class cl = Class.forName("de.dimm.vsm.Main");
            Class[] types = new Class[1];
            types[0] = arg.getClass();
            Object[] args = new Object[1];
            args[0] = arg;
            Method get_control = cl.getMethod("get_control", (Class[]) null);
            Object logicControl = get_control.invoke(null, (Object[]) null);

            Class logic_class = Class.forName("de.dimm.vsm.LogicControl");
            Method m_func = logic_class.getMethod(func, types);
            Object result = m_func.invoke(logicControl, args);
            return result;
        }
        catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException exc)
        {
            if (!catchException)
                throw exc;

            if (exc instanceof InvocationTargetException)
            {
                InvocationTargetException ite = (InvocationTargetException)exc;
                notify( ite.getTargetException().getMessage(), "");
                return null;
            }
            error("Error in reflection call " + func + ":" + exc.getMessage(), exc);
        }
        return null;
    }
    
    public static void main( String[] args)
    {
        CliMain main = new CliMain();
        
        try
        {
            main.readParams(args);
        }
        catch (UnknownHostException unknownHostException)
        {
            System.err.printf("Fehler beim Aufloesen des Hostnamens: " + unknownHostException.getMessage());
            return;
        }
        
        
        try
        {
            if (main.tryLogin())
            {
                GuiServerApi api = main.getGuiServerApi();
                Properties props = api.getProperties();
                System.out.println("Verbunden mit VSM Server Version: " + props.getProperty("Version"));
                
                boolean ret = main.handleCommand(args);
                
                main.logout();                
                
                
                System.exit(ret ? 0 : 1);
                
            }
            else
            {
                System.err.printf("Anmeldung ist fehlgeschlagen");                
                System.exit(2);
            }            
        }
        catch (IOException iOException)
        {
            System.err.printf("Connect ist fehlgeschlagen");                
            System.exit(3);
        }
        
    }

    protected void logError(String err, Object... params)
    {
        String s = String.format(err, params);        
        Logger.getLogger(AbstractCommand.class.getName()).log(Level.SEVERE, s);
    }
    
    private boolean handleCommand( String[] args )
    {       
        String cmdName = null;
        for (int i = 0; i < args.length; i++)
        {
            String arg = args[i];
            if (arg.equals("--cmd") && i < args.length - 1)
            {
                cmdName = args[i+1];
                i++;
            }
            if (arg.equals("-p") && i < args.length - 1)
            {
                port = Integer.parseInt(args[i+1]);
                i++;
            }
            if (arg.equals("-u") && i < args.length - 1)
            {
                user = args[i+1];
                i++;
            }
            if (arg.equals("-x") && i < args.length - 1)
            {
                pwd = args[i+1];
                i++;
            }
        }      
        
        if (!cmdMgr.cmdExists(cmdName))
        {
            logError( "Unbekannter Befehl %s", cmdName);
            return false;
        }
        ICommand cmd = cmdMgr.getCmd(cmdName);
        
        if (!cmd.readArgs(args))
        {
            logError(  "Fehler beim Lesen der Parameter fuer Befehl %s: %s", cmdName, cmd.getErrorText());
            return false;
        }
        if (!cmd.exec(uiApi))
        {
            logError(  "Fehler beim Ausführen von Befehl %s: %s", cmdName, cmd.getErrorText());
            return false;
        }
        return true;                
    }
    
   
}
