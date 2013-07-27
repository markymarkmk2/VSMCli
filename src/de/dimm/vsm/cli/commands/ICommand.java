/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.cli.commands;

import de.dimm.vsm.cli.CliApi;
import java.util.List;

/**
 *
 * @author Administrator
 */
public interface ICommand
{
    String getName();
    boolean exec( CliApi cliApi);
    boolean readArgs( String[] args);
    List<String> getErrors();
    String getErrorText();
    boolean hasErrors();
    
}
