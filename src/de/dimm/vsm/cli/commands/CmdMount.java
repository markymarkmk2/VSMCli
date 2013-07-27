/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.cli.commands;

import de.dimm.vsm.auth.User;
import de.dimm.vsm.cli.CliApi;
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
                try
                {
                    wrapper = api.mountEntry(user, mountEntry);
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
        return !hasErrors();
    }
    
}
