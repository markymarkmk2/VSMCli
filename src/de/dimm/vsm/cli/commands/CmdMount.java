/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.cli.commands;

import de.dimm.vsm.auth.User;
import de.dimm.vsm.cli.CliApi;
import de.dimm.vsm.net.RemoteFSElem;
import de.dimm.vsm.net.StoragePoolWrapper;
import de.dimm.vsm.net.interfaces.GuiServerApi;
import de.dimm.vsm.records.MountEntry;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class CmdMount extends AbstractCommand
{
    
    String mountPath;
    String subPath;
    String ip;
    String port;
    String mountName;
    StoragePoolWrapper wrapper;

    @Override
    public String getName()
    {
        return "mount";
    }

    @Override
    public boolean exec( CliApi cliApi )
    {
        GuiServerApi api = cliApi.checkLogin();
        User user = cliApi.getUser();
        
        List<MountEntry> mountEntries = api.getAllMountEntries();
        for (int i = 0; i < mountEntries.size(); i++)
        {
            MountEntry mountEntry = mountEntries.get(i);
            
            System.out.println(mountEntry.getName());
            if (mountEntry.getName().equals(mountName))
            {
                MountEntry modifiedMountEntry = checkModified( mountEntry);
                try
                {
                    wrapper = api.mountEntry(user, modifiedMountEntry);
                    return wrapper != null;                    
                }
                catch (IOException ex)
                {
                    addError( ex, "Mount schlug fehl fuer %s", mountName);
                }
            }            
        }   
        addError( "Unbekannter Mounteintrag %s", mountName);
        return false;
    }

   
    @Override
    public boolean readArgs( String[] args )
    {
        mountName = getStringArg("name", args);
        subPath = getOptStringArg("sub-path", args);
        ip = getOptStringArg("ip", args);
        port = getOptStringArg("port", args);
        mountPath = getOptStringArg("mount-path", args);
        return !hasErrors();
    }

    private MountEntry checkModified( MountEntry mountEntry )
    {
        MountEntry ret = mountEntry;
        if (subPath != null || ip != null | port != null || mountPath != null ) {
            ret = new MountEntry(mountEntry);
            if (ip != null)
                ret.setIp( ip );
            if (subPath != null)
                ret.setSubPath( subPath );
            if (port != null)
                ret.setPort( Integer.valueOf(port) );
            if (mountPath != null)
                ret.setMountPath(  RemoteFSElem.createDir(mountPath) );                     
        }
        return ret;
    }

    @Override
    public String usage()
    {
        return usage("Mount a VSM-Mount", new String[] {"--name <VSM-MountName>"},  
                new String[] {  "--sub-path <Override VSM-Path>", "--ip <Override agent IP>", 
                                "--port <Override agent port>","--mount-path <Override Agent mount path>"});
    }
}
