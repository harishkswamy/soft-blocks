package buildBlocks.repos.svn;

import java.io.File;

import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import buildBlocks.BuildError;
import buildBlocks.repos.SourceRepository;

/**
 * @author hkrishna
 */
public class Svn implements SourceRepository
{
    private static class UpdateEventHandler implements ISVNEventHandler
    {
        public void handleEvent(SVNEvent event, double progress)
        {
            System.out.print('.');
        }

        public void checkCancelled() throws SVNCancelException
        {
        }
    }

    private SVNClientManager _manager;
    private SVNURL           _url;

    public void init(String url, String userName, String password)
    {
        try
        {
            System.setProperty("svnkit.http.methods", "Basic,Digest,Negotiate,NTLM");

            DAVRepositoryFactory.setup();
            SVNRepositoryFactoryImpl.setup();
            FSRepositoryFactory.setup();

            DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
            _manager = SVNClientManager.newInstance(options, userName, password);
            _url = SVNURL.parseURIEncoded(url);
        }
        catch (Exception e)
        {
            throw new BuildError(e, false, false);
        }
    }

    public long checkout(String revision, String path, String target)
    {
        try
        {
            SVNURL reposUrl = _url.appendPath(path, false);

            System.out.println("Checking out " + reposUrl);

            SVNUpdateClient updateClient = _manager.getUpdateClient();
            updateClient.setEventHandler(new UpdateEventHandler());
            updateClient.setIgnoreExternals(false);

            SVNRevision rev = revision == null || revision.trim().length() == 0 ? SVNRevision.HEAD : SVNRevision
                .create(Long.parseLong(revision));

            return updateClient.doCheckout(reposUrl, new File(target), rev, rev, SVNDepth.INFINITY, true);
        }
        catch (Exception e)
        {
            throw new BuildError(e, false, false);
        }
    }
}
