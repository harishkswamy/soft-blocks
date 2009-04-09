package buildBlocks.bootstrap;

import jBlocks.server.AggregateException;
import jBlocks.server.IOUtils;
import buildBlocks.BuildUtils;
import buildBlocks.repos.SourceRepository;

/**
 * @author hkrishna
 */
public class ReposModel
{
    private volatile String           _type;
    private volatile String           _url;
    private volatile String           _user;
    private volatile String           _password;
    private volatile SourceRepository _srcRepos;
    private volatile boolean          _invalidRepos;

    ReposModel()
    {

    }

    ReposModel(ReposModel src)
    {
        _type = src.type();
        _url = src.url();
        _user = src.user();
        _password = src.password();
    }

    ReposModel create()
    {
        return new ReposModel(this);
    }

    public String type()
    {
        return _type;
    }

    public void type(String type)
    {
        _type = type;
    }

    public String url()
    {
        return _url;
    }

    public void url(String url)
    {
        _url = url;
    }

    public String user()
    {
        return _user;
    }

    public void user(String user)
    {
        _user = user;
    }

    public String password()
    {
        return _password;
    }

    public void password(String password)
    {
        _password = password;
    }

    long checkout(String revision, String path, String target)
    {
        System.out.println("Checking out " + _url + path);

        if (_invalidRepos)
            throw new IllegalStateException("Repository is invalid");

        if (_srcRepos == null)
            _srcRepos = createSrcRepos();

        return _srcRepos.checkout(revision, path, target);
    }

    SourceRepository createSrcRepos()
    {
        try
        {
            SourceRepository repos = (SourceRepository) BuildUtils.loadClass(getClass().getClassLoader(), _type)
                .newInstance();

            if ("ask!".equalsIgnoreCase(_password))
                _password = IOUtils.askPassword("Please enter password for " + _url);

            repos.init(_url, _user, _password);

            return repos;
        }
        catch (Exception e)
        {
            _invalidRepos = true;
            throw AggregateException.with(e, "Unable to connect to repository.");
        }
    }
}
