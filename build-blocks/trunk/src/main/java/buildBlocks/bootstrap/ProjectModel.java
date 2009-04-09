package buildBlocks.bootstrap;

import static buildBlocks.BuildCtx.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import buildBlocks.BuildError;

/**
 * @author hkrishna
 */
public class ProjectModel
{
    private static volatile WorkspaceBuilder _builder;
    private static volatile boolean          _checkout;
    private static volatile boolean          _traceOn;

    private volatile String                  _name;
    private volatile ReposModel              _repos;
    private volatile String                  _reposPath;
    private volatile String                  _reposRev;
    private volatile ProjectModel[]          _deps;
    private volatile String                  _javaVersion;
    private volatile String                  _buildCmd;

    private volatile int                     _maxHopsToRoot;
    private volatile boolean                 _needToBuild;
    private volatile boolean                 _checkoutFailed;

    private volatile CountDownLatch          _checkoutGate = new CountDownLatch(1);
    private volatile CountDownLatch          _buildGate    = new CountDownLatch(1);

    ProjectModel(WorkspaceBuilder builder, boolean checkout, boolean traceOn)
    {
        _builder = builder;
        _checkout = checkout;
        _traceOn = traceOn;
    }

    private ProjectModel(String name, ProjectModel src)
    {
        _name = name;
        _repos = src.repos();
        _reposPath = src.reposPath();
        _reposRev = src.reposRev();
        _deps = src.deps();
        _javaVersion = src.javaVersion();
        _buildCmd = src.buildCmd();
    }

    ProjectModel create(String name)
    {
        return new ProjectModel(name, this);
    }

    // Getters/Setters ====================================================

    public String name()
    {
        return _name;
    }

    public void name(String name)
    {
        _name = name;
    }

    public ReposModel repos()
    {
        return _repos;
    }

    public ReposModel repos(String reposName)
    {
        return _repos = _builder.getReposModel(reposName);
    }

    public String reposPath()
    {
        return _reposPath;
    }

    public String reposPath(String reposPath)
    {
        return _reposPath = reposPath;
    }

    public String reposRev()
    {
        return _reposRev;
    }

    public String reposRev(String reposRev)
    {
        return _reposRev = reposRev;
    }

    public ProjectModel[] deps()
    {
        return _deps;
    }

    public ProjectModel[] deps(String depNames)
    {
        String[] prjs = depNames.split(",");
        ProjectModel[] deps = new ProjectModel[prjs.length];

        for (int i = 0; i < prjs.length; i++)
            deps[i] = _builder.getProjectModel(prjs[i].trim());

        return _deps = deps;
    }

    public String javaVersion()
    {
        return _javaVersion;
    }

    public String javaVersion(String javaVersion)
    {
        return _javaVersion = javaVersion;
    }

    public String buildCmd()
    {
        return _buildCmd;
    }

    public String buildCmd(String buildCmd)
    {
        return _buildCmd = buildCmd;
    }

    int maxHopsToRoot()
    {
        return _maxHopsToRoot;
    }

    int maxHopsToRoot(int maxHopsToRoot)
    {
        return _maxHopsToRoot = maxHopsToRoot;
    }

    boolean needToBuild()
    {
        return _needToBuild;
    }

    boolean needToBuild(boolean needToBuild)
    {
        return _needToBuild = needToBuild;
    }

    // Behaviour =================================================================

    boolean isDep(ProjectModel pc)
    {
        if (_deps == null || _deps.length == 0)
            return false;

        for (ProjectModel dep : _deps)
        {
            if (dep == pc || dep.isDep(pc))
                return true;
        }

        return false;
    }

    void checkout()
    {
        try
        {
            _reposRev = String.valueOf(_repos.checkout(_reposRev, _reposPath, _name));
        }
        catch (Throwable t)
        {
            _checkoutFailed = true;
            _builder.handleError(t);
        }
        finally
        {
            _checkoutGate.countDown();
        }
    }

    void awaitCheckout()
    {
        try
        {
            _checkoutGate.await();
        }
        catch (InterruptedException e)
        {
            throw new BuildError(e, false, false);
        }
    }

    boolean readyToBuild(int awaitMs)
    {
        try
        {
            if (!_checkout || _checkoutGate.await(awaitMs, TimeUnit.MILLISECONDS))
            {
                if (_deps == null)
                    return true;

                for (ProjectModel dep : _deps)
                {
                    if (!dep._buildGate.await(awaitMs, TimeUnit.MILLISECONDS))
                        return false;
                }

                return true;
            }
            else
                return false;
        }
        catch (InterruptedException e)
        {
            throw new BuildError(e, false, false);
        }
    }

    void build()
    {
        // If already built, skip build
        if (_buildGate.getCount() == 0)
            return;

        try
        {
            // if checkout was requested and checkout failed then skip build but release latch
            if (_checkout && _checkoutFailed)
                return;

            // If trace is turned on for the workspace, turn it on for the current thread as well
            // even if the project is not setup to run with trace on.
            if (_traceOn)
                ctx().setTrace(true);

            new ProjectBuilder(_name, _reposRev, _buildCmd).build();
        }
        catch (Throwable t)
        {
            _builder.handleError(t);
        }
        finally
        {
            _buildGate.countDown();
        }
    }
}
