/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.cli.commands;

import de.dimm.vsm.cli.CliApi;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Administrator
 */
public class CommandManager
{
    Map<String, ICommand> cmdList;
        
    public CommandManager()
    {
        cmdList = new HashMap<>();
        
        addCommand( new CmdMount() );
        addCommand( new CmdUnmount() );        
    }
    
    private void addCommand( ICommand cmd)
    {
        cmdList.put(cmd.getName(), cmd);
    }
    
    public ICommand getCmd( String cmdName)
    {
        return cmdList.get(cmdName);        
    }
    
    public boolean cmdExists(String cmdName)
    {
        return cmdList.containsKey(cmdName);
    }        
}
