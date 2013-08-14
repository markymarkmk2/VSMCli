/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.cli.commands;

import de.dimm.vsm.cli.CliApi;
import de.dimm.vsm.net.StoragePoolWrapper;
import de.dimm.vsm.net.interfaces.GuiServerApi;
import de.dimm.vsm.records.MountEntry;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class CmdUnmount extends AbstractCommand
{
    
    String mountName;
    StoragePoolWrapper wrapper;

    @Override
    public String getName()
    {
        return "unmount";
    }

    @Override
    public boolean exec( CliApi cliApi )
    {
        GuiServerApi api = cliApi.checkLogin();
        
        List<MountEntry> mountEntries = api.getAllMountEntries();
        for (int i = 0; i < mountEntries.size(); i++)
        {
            MountEntry mountEntry = mountEntries.get(i);
            
            System.out.println(mountEntry.getName());
            if (mountEntry.getName().equals(mountName))
            {
                api.unMountEntry(mountEntry);  
                return true;
            }            
        }     
        addError("Unbekannter Mounteintrag %s", mountName);
        
        return false;
    }

   
    @Override
    public boolean readArgs( String[] args )
    {
        mountName = getStringArg("name", args);
        return !hasErrors();
    }

    @Override
    public String usage()
    {
        return usage("Unmount a VSM-Mount", new String[] {"--name <VSM-MountName>"}, null); 
    }
    
    
}
