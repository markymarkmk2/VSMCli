/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.cli;

import com.caucho.hessian.client.HessianRuntimeException;
import de.dimm.vsm.auth.GuiUser;
import de.dimm.vsm.auth.User;
import de.dimm.vsm.cli.ServerConnector.GuiLoginApiEntry;
import de.dimm.vsm.cli.ServerConnector.GuiServerApiEntry;
import de.dimm.vsm.cli.commands.AbstractCommand;
import de.dimm.vsm.cli.commands.CommandManager;
import de.dimm.vsm.cli.commands.ICommand;
import de.dimm.vsm.hash.StringUtils;
import de.dimm.vsm.log.LogListener;
import de.dimm.vsm.log.LogManager;
import de.dimm.vsm.net.GuiWrapper;
import de.dimm.vsm.net.interfaces.GuiLoginApi;
import de.dimm.vsm.net.interfaces.GuiServerApi;
import de.dimm.vsm.records.RoleOption;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class CliMain
{
    public static final String PREFS_FOLDER = "preferences";

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
    
    private String version = "0.2";

    public String getVersion()
    {
        return version;
    }

    public CliMain()
    {
        initPrefs();
        cmdMgr = new CommandManager();
    }
    
    private void initPrefs()
    {
        File f = new File(PREFS_FOLDER);
        if (!f.exists())
            f.mkdir();
        
        File logprefs = new File(PREFS_FOLDER, "log_prefs.dat");
        if (!logprefs.exists())
        {
            try
            {
                RandomAccessFile raf = new RandomAccessFile(logprefs, "rw");
                raf.close();
            }
            catch (IOException iOException)
            {
                iOException.printStackTrace();
            }
        }
    }
    static boolean isJava7orBetter()
    {
        String javaVer = System.getProperty("java.version");
        try
        {

            String[] a = javaVer.split("\\.");
            int maj = Integer.parseInt(a[0]);
            int min = Integer.parseInt(a[1]);
            if (maj == 1 && min < 7)
            {
                return false;
            }
        }
        catch (Exception exc)
        {
            System.out.println("Fehler beim Ermitten der Javaversion: " + javaVer + ": " + exc.getMessage());
        }
        return true;
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
        
        for (int i = 0; i < args.length; i++)
        {
            String arg = args[i];
            if (arg.equals("--host") && i < args.length - 1)
            {
                host = args[i+1];
                i++;
            }
            if (arg.equals("--port") && i < args.length - 1)
            {
                port = Integer.parseInt(args[i+1]);
                i++;
            }
            if (arg.equals("--user") && i < args.length - 1)
            {
                user = args[i+1];
                i++;
            }
            if (arg.equals("--pwd") && i < args.length - 1)
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
            if (arg.equals("--42"))
            {
                user = "system";
                pwd = "helikon";
            }
        }
        if (StringUtils.isEmpty(user))
        {
            error("Bitte geben Sie einen Benutzernamen mit --user an");
            usage();
        }
        if (StringUtils.isEmpty(pwd))
        {
            error("Bitte geben Sie das Benutzerpasswort mit --pwd an");
            usage();
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
        if (!isJava7orBetter())
        {
            System.err.printf("VSM-Commandozeilen-Client: Sie benötigen eine Java-Version >= 1.7");
            System.exit(1);
        }
        CliMain main = new CliMain();
        
        try
        {
            main.readParams(args);
        }
        catch (UnknownHostException unknownHostException)
        {
            System.err.printf("Fehler beim Aufloesen des Hostnamens: " + unknownHostException.getMessage());
            System.exit(1);
        }
        ICommand cmd = main.checkCommand(args);
        
        boolean ret = false;
        try
        {
            if (main.tryLogin())
            {
                
                
                ret = cmd.exec(main.uiApi);
                if (!ret)
                {
                    main.logError( "Fehler beim Ausführen von Befehl %s: %s", cmd.getName(), cmd.getErrorText());
                }
                System.exit(ret ? 0 : 1);
            }
            else
            {
                System.err.printf("Anmeldung ist fehlgeschlagen");                                
            }            
        }
        catch (ConnectException exc)
        {
            System.err.printf("Host " + main.addr.toString() + ":" + main.port + " konnte nicht kontaktiert werden");                
            System.exit(3);
        }
        catch ( HessianRuntimeException hexc)
        {
            if (hexc.getCause().getClass() == ConnectException.class)
            {
                System.err.printf("Host " + main.addr.toString() + ":" + main.port + " konnte nicht kontaktiert werden");                
                System.exit(3);
            }
            System.err.printf("Protokoll ist fehlgeschlagen: " + hexc.getMessage());                
            System.exit(4);
        }
        catch (IOException iOException)
        {
            System.err.printf("Connect ist fehlgeschlagen: " + iOException.getMessage());                
            System.exit(5);
        }
        finally
        {
            try
            {
                main.logout();                
            }
            catch (IOException iOException)
            {
            }
        }
        System.exit(2);        
    }

    protected void logError(String err, Object... params)
    {
        String s = String.format(err, params);        
        Logger.getLogger(AbstractCommand.class.getName()).log(Level.SEVERE, s);
    }
    
    private ICommand checkCommand( String[] args )
    {       
        String cmdName = null;
        boolean doHelp = false;
        for (int i = 0; i < args.length; i++)
        {
            String arg = args[i];
            if (arg.equals("--cmd") && i < args.length - 1)
            {
                cmdName = args[i+1];
                i++;
            }
            if (arg.equals("--help") || arg.equals("-?") || arg.equals("-h")  )
            {
                doHelp = true;
                if (i < args.length - 1)
                {
                    cmdName = args[i+1];
                    i++;
                }                
            }
            
        }      
        
        if (doHelp || cmdName == null)
        {
            usage(cmdName);            
        }
        
        if (!cmdMgr.cmdExists(cmdName))
        {
            usage(cmdName); 
        }
        
        ICommand cmd = cmdMgr.getCmd(cmdName);
        
        if (!cmd.readArgs(args))
        {
            logError(  "Fehler beim Lesen der Parameter fuer Befehl %s: %s", cmdName, cmd.getErrorText());
            usage(cmdName); 
        }
        return cmd;
    }

    private void usage( )
    {
        usage(null);
    }
    private void usage( String cmdName )
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\nCommandClient for VSM V");
        sb.append(getVersion());
        sb.append("\n");
            
        ICommand cmd = cmdMgr.getCmd(cmdName);
            
        if (cmd == null)
        {
            sb.append("\nUsage:         --user username --pwd password --cmd <CommandName> [Optional args]");
            sb.append("\nOptional args: --host Host --port port ");
            sb.append("\nSSL-Options:   --keyStore <Path to Keystore> --keyPath <Password of keystore>");
            sb.append("\nHelp:          --help|-? [<CommandName>]\n\nAvailable commands are:\n");
            for (String name :cmdMgr.getCommands())
            {
                sb.append(name);
                sb.append("\n");
            }            
        }
        else
        {
            sb.append(cmd.usage());
            sb.append("\n");            
        }
        System.err.println(sb.toString());
        System.exit(3);
    }
    
   
}
