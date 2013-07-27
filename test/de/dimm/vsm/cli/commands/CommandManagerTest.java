/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.cli.commands;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Administrator
 */
public class CommandManagerTest
{
    
    public CommandManagerTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of getCmd method, of class CommandManager.
     */
    @Test
    public void testGetCmd()
    {
        System.out.println("getCmd");
        String cmdName = "mount";
        CommandManager instance = new CommandManager();
        
        ICommand result = instance.getCmd(cmdName);
        assertNotNull(result);
        assertEquals("Name", result.getName(),cmdName);
        
       
        assertTrue( result.readArgs(new String[]{"--name", "KAKA"}) );
        assertFalse( result.readArgs(new String[]{"--nme", "KAKA"}) );
        assertFalse( result.readArgs(new String[]{}) );
    }

    /**
     * Test of cmdExists method, of class CommandManager.
     */
    @Test
    public void testCmdExists()
    {
        System.out.println("cmdExists");
        String cmdName = "mount";
        CommandManager instance = new CommandManager();
        assertTrue( instance.cmdExists(cmdName) );
        assertFalse( instance.cmdExists("_" + cmdName) );
    }
}