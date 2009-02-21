package buildBlocks.bootstrap;

import buildBlocks.BuildError;
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
            repos.init(_url, _user, _password);
            return repos;
        }
        catch (Exception e)
        {
            throw new BuildError(e, false, false);
        }
    }
}
