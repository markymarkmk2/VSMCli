/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.cli;

import de.dimm.vsm.Exceptions.PathResolveException;
import de.dimm.vsm.Exceptions.PoolReadOnlyException;
import de.dimm.vsm.auth.User;
import de.dimm.vsm.jobs.JobEntry;
import de.dimm.vsm.net.GuiWrapper;
import de.dimm.vsm.net.LogQuery;
import de.dimm.vsm.net.RemoteFSElem;
import de.dimm.vsm.net.ScheduleStatusEntry;
import de.dimm.vsm.net.SearchEntry;
import de.dimm.vsm.net.SearchStatus;
import de.dimm.vsm.net.SearchWrapper;
import de.dimm.vsm.net.StoragePoolWrapper;
import de.dimm.vsm.net.interfaces.GuiLoginApi;
import de.dimm.vsm.net.interfaces.GuiServerApi;
import de.dimm.vsm.net.interfaces.IWrapper;
import de.dimm.vsm.records.AbstractStorageNode;
import de.dimm.vsm.records.ArchiveJob;
import de.dimm.vsm.records.FileSystemElemNode;
import de.dimm.vsm.records.HotFolder;
import de.dimm.vsm.records.MessageLog;
import de.dimm.vsm.records.MountEntry;
import de.dimm.vsm.records.Schedule;
import de.dimm.vsm.records.StoragePool;
import de.dimm.vsm.tasks.TaskEntry;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Administrator
 */
public class CliApi implements GuiServerApi
{
    String lastUser;
    String lastPwd;
    
    CliMain main;
   // GuiLoginApi loginApi;
    
    //GuiWrapper wrapper;
   // String user;
   // String pwd;

    public CliApi( CliMain main/*, GuiWrapper wrapper*/)
    {
        this.main = main;
        //this.wrapper = wrapper;
        
    }

    public GuiServerApi checkLogin()
    {
        if ( !main.isLoggedIn())
        {
            try
            {
                main.tryLogin();
            }
            catch (IOException iOException)
            {
            }
        }
        if ( !main.isLoggedIn())
        {
            return null;
        }

        return main.getGuiServerApi();
    }



    public GuiLoginApi getGuiLoginApi()
    {
        return main.getGuiLoginApi();
    }
    public User getUser()
    {
        return main.getUser();
    }


    @Override
    public boolean startBackup( Schedule sched, User user ) throws Exception
    {

        GuiServerApi api = checkLogin();
        if (api != null)
            return api.startBackup(sched, user);
        
        return false;
    }

    @Override
    public StoragePoolWrapper mountVolume( String agentIp, int agentPort, StoragePool pool, Date timestamp, String subPath, User user, String drive ) throws IOException
    {
        GuiServerApi api = checkLogin();
        if (api != null)
            return api.mountVolume(agentIp, agentPort, pool, timestamp, subPath, user, drive);

        return null;
    }

    @Override
    public StoragePoolWrapper mountVolume( String agentIp, int agentPort, StoragePoolWrapper poolWrapper, String drive )  throws IOException
    {
        GuiServerApi api = checkLogin();
        if (api != null)
            return api.mountVolume(agentIp, agentPort, poolWrapper, drive);

        return null;
    }


    @Override
    public boolean unmountVolume( StoragePoolWrapper pool )
    {
        GuiServerApi api = checkLogin();
        if (api != null)
            return api.unmountVolume( pool);

        return false;
    }

    @Override
    public StoragePoolWrapper getMounted( String agentIp, int agentPort, StoragePool pool )
    {
        GuiServerApi api = checkLogin();
        if (api != null)
            return api.getMounted(agentIp, agentPort, pool);

        return null;
    }


    @Override
    public boolean unmountAllVolumes()
    {
        GuiServerApi api = checkLogin();
        if (api != null)
            return api.unmountAllVolumes();

        return false;
    }

    @Override
    public boolean remountVolume( StoragePoolWrapper wrapper )
    {
        GuiServerApi api = checkLogin();
        if (api != null)
            return api.remountVolume(wrapper);

        return false;
    }

    @Override
    public StoragePoolWrapper openPoolView( StoragePool pool, Date timestamp, String subPath, User user )
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.openPoolView(pool, timestamp, subPath, user);

        return null;
    }

    @Override
    public StoragePoolWrapper openPoolView( StoragePool pool, boolean rdonly, String subPath, User user )
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.openPoolView(pool, rdonly, subPath, user);

        return null;
    }

    @Override
    public StoragePoolWrapper openPoolView( StoragePool pool, boolean rdonly, FileSystemElemNode node, User user )
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.openPoolView(pool, rdonly, node, user);

        return null;
    }

    @Override
    public List<RemoteFSElem> listDir( StoragePoolWrapper wrapper, RemoteFSElem path ) throws SQLException
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.listDir(wrapper, path);

        return null;
    }

    @Override
    public void closePoolView( StoragePoolWrapper wrapper )
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            guiServerApi.closePoolView(wrapper);
    }

    @Override
    public boolean removeFSElem( IWrapper wrapper, RemoteFSElem path ) throws PoolReadOnlyException, SQLException
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.removeFSElem(wrapper, path);

        return false;
    }

    @Override
    public FileSystemElemNode createFileSystemElemNode( StoragePool pool, String path, String type ) throws IOException, PoolReadOnlyException, PathResolveException
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.createFileSystemElemNode(pool, path, type);

        return null;
    }

    @Override
    public FileSystemElemNode createFileSystemElemNode( StoragePoolWrapper wrapper, String path, String type ) throws IOException, PoolReadOnlyException, PathResolveException
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.createFileSystemElemNode(wrapper, path, type);

        return null;
    }

    @Override
    public List<ScheduleStatusEntry> listSchedulerStats()
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.listSchedulerStats();

        return null;
    }

    @Override
    public boolean abortBackup( Schedule sched )
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.abortBackup(sched);

        return false;
    }

    @Override
    public boolean restoreFSElem( IWrapper wrapper, RemoteFSElem path, String targetIP, int targetPort, String targetPath, int flags, User user ) throws PoolReadOnlyException, SQLException, IOException
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.restoreFSElem(wrapper, path, targetIP, targetPort, targetPath, flags, user);

        return false;
    }

    @Override
    public boolean restoreFSElems( IWrapper wrapper, List<RemoteFSElem> paths, String targetIP, int targetPort, String targetPath, int flags, User user ) throws SQLException, PoolReadOnlyException, IOException
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.restoreFSElems(wrapper, paths, targetIP, targetPort, targetPath, flags, user);

        return false;
   }


    @Override
    public Properties getAgentProperties( String ip, int port, boolean wm )
    {
        GuiServerApi api = checkLogin();
        if (api != null)
        {
            return api.getAgentProperties(ip, port, wm);
        }
        return null;
    }

    @Override
    public SearchWrapper search(  StoragePool pool, ArrayList<SearchEntry> slist )
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.search( pool, slist, 0);

        return null;
    }
    @Override
    public SearchWrapper search(  StoragePool pool, ArrayList<SearchEntry> slist, int max )
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.search( pool, slist, max);

        return null;
    }

    @Override
    public SearchWrapper searchJob( StoragePool pool, ArrayList<SearchEntry> slist, int max )
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.searchJob( pool, slist, max);

        return null;
    }


    @Override
    public SearchStatus getSearchStatus( SearchWrapper wrapper )
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.getSearchStatus(wrapper);

        return null;
    }

    @Override
    public List<RemoteFSElem> getSearchResult( SearchWrapper wrapper, int start, int limit )
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.getSearchResult(wrapper, start, limit);

        return null;
    }

    @Override
    public List<ArchiveJob> getJobSearchResult( SearchWrapper wrapper, int start, int limit )
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.getJobSearchResult(wrapper, start, limit);

        return null;
    }


    @Override
    public void closeSearch( SearchWrapper wrapper )
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            guiServerApi.closeSearch(wrapper);
    }

    @Override
    public List<RemoteFSElem> listSearchDir( SearchWrapper wrapper, RemoteFSElem path ) throws SQLException
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.listSearchDir(wrapper, path);

        return null;
    }

    @Override
    public StoragePoolWrapper mountVolume( String ip, int port, SearchWrapper searchWrapper, User object, String drive )
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.mountVolume(ip, port, searchWrapper, object, drive);

        return null;
    }

    @Override
    public StoragePoolWrapper getMounted( String ip, int port, SearchWrapper searchWrapper )
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.getMounted(ip, port, searchWrapper);

        return null;
    }

    @Override
    public void reSearch( SearchWrapper searchWrapper, ArrayList<SearchEntry> slist )
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            guiServerApi.reSearch(searchWrapper, slist);
    }

    @Override
    public JobEntry[] listJobs(User user)
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.listJobs(user);

        return null;
    }

    @Override
    public void emptyNode( AbstractStorageNode node, User user ) throws SQLException
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            guiServerApi.emptyNode(node, user);
    }

    @Override
    public void moveNode( AbstractStorageNode node, AbstractStorageNode toNode, User user ) throws SQLException
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            guiServerApi.moveNode(node, toNode, user);
    }
//
//    @Override
//    public GuiServerApi getDummyGuiServerApi()
//    {
//        return loginApi.getDummyGuiServerApi();
//    }
//
//    @Override
//    public boolean isStillValid( GuiWrapper wrapper )
//    {
//        return loginApi.isStillValid(wrapper);
//    }

    @Override
    public TaskEntry[] listTasks()
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.listTasks();

        return null;
    }

    @Override
    public MessageLog[] listLogs( int cnt, long offsetIdx, LogQuery lq )
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.listLogs(cnt, offsetIdx, lq);

        return null;
    }

    @Override
    public MessageLog[] listLogsSince( long idx, LogQuery lq )
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.listLogsSince(idx, lq);

        return null;
    }

    @Override
    public long getLogCounter()
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.getLogCounter();

        return 0;
    }

    @Override
    public InputStream openStream( IWrapper wrapper, RemoteFSElem path )
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.openStream(wrapper, path);
        return null;
    }

    @Override
    public String resolvePath( IWrapper wrapper, RemoteFSElem path ) throws SQLException, PathResolveException
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.resolvePath(wrapper, path);

        return null;
    }

    @Override
    public boolean importMMArchiv( HotFolder node, long fromIdx, long tillIdx, boolean withOldJobs, User user ) throws Exception
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.importMMArchiv(node, fromIdx, tillIdx, withOldJobs, user);

        return false;
    }

    @Override
    public boolean restoreJob( SearchWrapper searchWrapper, ArchiveJob job, String ip, int port, String path, int rflags, User user ) throws SQLException, PoolReadOnlyException, IOException
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.restoreJob(searchWrapper, job, ip, port, path, rflags, user);

        return false;
    }
    
    @Override
    public boolean removeJob( SearchWrapper searchWrapper, ArchiveJob job ) throws SQLException, PoolReadOnlyException
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.removeJob(searchWrapper, job);

        return false;
    }

    @Override
    public void updateReadIndex( StoragePool pool )
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            guiServerApi.updateReadIndex(pool);

    }

    @Override
    public StoragePoolWrapper openPoolView( StoragePool pool, boolean rdonly, boolean showDeleted, String subPath, User user )
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.openPoolView(pool, rdonly, showDeleted, subPath, user);
        return null;
    }

    @Override
    public StoragePoolWrapper openPoolView( StoragePool pool, boolean rdonly, boolean showDeleted, FileSystemElemNode node, User user )
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.openPoolView(pool, rdonly, showDeleted, node, user);

        return null;
    }

    @Override
    public boolean undeleteFSElem( IWrapper wrapper, RemoteFSElem path ) throws SQLException, PoolReadOnlyException
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.undeleteFSElem(wrapper, path);

        return false;
    }

    @Override
    public boolean deleteFSElem( IWrapper wrapper, RemoteFSElem path ) throws SQLException, PoolReadOnlyException
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.deleteFSElem(wrapper, path);

        return false;
    }

    @Override
    public void syncNode( AbstractStorageNode t, AbstractStorageNode cloneNode, User user  ) throws SQLException, IOException
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            guiServerApi.syncNode(t, cloneNode, user);

    }

    @Override
    public boolean isBusyNode( AbstractStorageNode node )
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.isBusyNode(node);

        return false;
    }

    @Override
    public boolean initNode( AbstractStorageNode node, User user )
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.initNode(node, user);

        return false;
    }

    @Override
    public void initCheck(User user, String checkName, Object arg, Object optArg) {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
             guiServerApi.initCheck(user, checkName, arg, optArg);

        
    }

    @Override
    public List<String> getCheckNames(Class<?> clazz) {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.getCheckNames(clazz);

        return null;
    }

    @Override
    public List<MountEntry> getMountedMountEntries()
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.getMountedMountEntries();

        return null;
    }
    
    @Override
    public List<MountEntry> getAllMountEntries()
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.getAllMountEntries();

        return null;
    }
    
    @Override
    public StoragePoolWrapper mountEntry(User user,  MountEntry mountEntry ) throws IOException
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            return guiServerApi.mountEntry( user, mountEntry );

        return null;
    }
    
    @Override
    public void unMountEntry(MountEntry mountEntry )
    {
        GuiServerApi guiServerApi = checkLogin();
        if (guiServerApi != null)
            guiServerApi.unMountEntry( mountEntry );       
    }

    @Override
    public Properties getProperties()
    {
        return new Properties();
    }
    
}
