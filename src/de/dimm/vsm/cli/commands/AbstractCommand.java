/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.cli.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public abstract class AbstractCommand implements ICommand
{
    List<String> errors;

    public AbstractCommand()
    {
        errors = new ArrayList<>();
    }
    
    protected void addError(String err, Object... params)
    {
        String s = String.format(err, params);
        errors.add(s);
        Logger.getLogger(AbstractCommand.class.getName()).log(Level.SEVERE, s);
    }
    protected void addError(Exception exc, String err, Object... params)
    {
        String s = String.format(err, params);
        errors.add(s);
        Logger.getLogger(AbstractCommand.class.getName()).log(Level.SEVERE, s, exc);
    }
    
    @Override
    public String getErrorText()
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < errors.size(); i++)
        {
            String string = errors.get(i);
            if (sb.length() > 0)
                sb.append("\n");
            sb.append(string);
        }
        return sb.toString();
    }

    @Override
    public List<String> getErrors()
    {
        return errors;
    }
    
    private String getStringArg( String argname, String[] args, boolean opt)
    {
        String wantArg = "--" + argname;
        for (int i = 0; i < args.length; i++)
        {
            String arg = args[i];
            if (arg.equals(wantArg) && i < args.length - 1)
            {
                return args[i+1];                
                
            }
        }       
        if (!opt)
            addError("Fehlendes Argument '--%s' fuer Befehl '%s'", argname, getName());
        
        return null;
    }
    protected String getStringArg( String argname, String[] args)
    {
        return getStringArg(argname, args, false);
    }
    protected String getOptStringArg( String argname, String[] args)
    {
        return getStringArg(argname, args, true);
    }

    @Override
    public boolean hasErrors()
    {
        return !errors.isEmpty();
    }
    
    protected String usage( String usage, String[] options, String[] optOptions)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(usage);
        if (options != null || optOptions != null)        
            sb.append("\n\nArgs:");
        
        if (options != null)
        {
            for (int i = 0; i < options.length; i++)
            {
                String string = options[i];
                if ( i> 0)
                    sb.append("\n");
                sb.append("\t");
                sb.append(string);            
            }
        }
        sb.append("\n");
        if (optOptions != null)
        {
            for (int i = 0; i < optOptions.length; i++)
            {
                String string = optOptions[i];
                if ( i> 0)
                    sb.append("\n");
                sb.append("\t[");            
                sb.append(string);            
                sb.append("]");                        
            }
        }
        
        return sb.toString();
    }
    
    
    
}
