/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.cli.commands;

import de.dimm.vsm.auth.User;
import de.dimm.vsm.cli.CliApi;
import de.dimm.vsm.net.interfaces.GuiServerApi;
import java.util.Properties;

/**
 *
 * @author Administrator
 */
public class CmdStatus extends AbstractCommand
{
    @Override
    public String getName()
    {
        return "status";
    }

    @Override
    public boolean exec( CliApi cliApi )
    {
        GuiServerApi api = cliApi.checkLogin();
        User user = cliApi.getUser();
        Properties props = api.getProperties();
        System.out.println("Verbunden als Benutzer <" + user.getNiceName() + "> mit VSM Server Version: " + props.getProperty("Version"));        
        return true;
    }
   
    @Override
    public boolean readArgs( String[] args )
    {       
        return true;
    }
   

    @Override
    public String usage()
    {
        return usage("Read status of VSM connect", null,  null);
    }
}
