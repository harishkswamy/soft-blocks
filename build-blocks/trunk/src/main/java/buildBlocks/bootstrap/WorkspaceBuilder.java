package buildBlocks.bootstrap;

import static buildBlocks.BuildCtx.*;

import jBlocks.server.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import buildBlocks.BuildError;
import buildBlocks.BuildUtils;
import buildBlocks.repos.SourceRepository;

/**
 * @author hkrishna
 */
public class WorkspaceBuilder extends Builder
{
    private static class ReposConfig
    {
        volatile String           _reposClass;
        volatile String           _url;
        volatile String           _user;
        volatile String           _password;
        volatile SourceRepository _srcRepos;

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
                SourceRepository repos = (SourceRepository) BuildUtils.loadClass(getClass().getClassLoader(),
                    _reposClass).newInstance();
                repos.init(_url, _user, _password);
                return repos;
            }
            catch (Exception e)
            {
                throw new BuildError(e, false, false);
            }
        }
    }

    private class ProjectConfig
    {
        volatile ReposConfig     _repos;
        volatile String          _name;
        volatile String          _reposPath;
        volatile String          _reposRev;
        volatile ProjectConfig[] _deps;
        volatile String          _buildCmd;
        volatile int             _maxHopsToRoot;
        volatile boolean         _needToBuild;
        volatile boolean         _checkoutFailed;

        CountDownLatch           _checkoutGate = new CountDownLatch(1);
        CountDownLatch           _buildGate    = new CountDownLatch(1);

        boolean isDep(ProjectConfig pc)
        {
            if (_deps == null || _deps.length == 0)
                return false;

            for (ProjectConfig dep : _deps)
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
                handleError(t);
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

                    for (ProjectConfig dep : _deps)
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
                // if checkout requested and checkout failed then skip build but release latch
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
                handleError(t);
            }
            finally
            {
                _buildGate.countDown();
            }
        }
    }

    public static void main(String[] args)
    {
        new WorkspaceBuilder(args).run();
    }

    private volatile boolean           _traceOn;
    private volatile boolean           _checkout;
    private Map<String, ReposConfig>   _repos    = new HashMap<String, ReposConfig>();
    private Map<String, ProjectConfig> _projects = new HashMap<String, ProjectConfig>();

    WorkspaceBuilder(String[] args)
    {
        super(args);

        _traceOn = ctx().traceOn();
    }

    protected boolean parse(String arg)
    {
        if ("-co".equals(arg))
            return _checkout = true;

        return false;
    }

    protected void build()
    {
        List<List<ProjectConfig>> projects = parseWorkspace(new File(buildParams()[0] + ".workspace"));

        if (_checkout)
        {
            checkoutProjects(projects);
            projects.get(0).get(0).awaitCheckout();
        }

        buildProjects(projects);
    }

    private void buildProjects(List<List<ProjectConfig>> wsProjects)
    {
        int threads = ctx().get("workspace.builder.threads", 1);
        threads = threads > 0 ? threads : 1;
        ExecutorService builders = Executors.newFixedThreadPool(threads);

        try
        {
            for (List<ProjectConfig> hopPrjs : wsProjects)
            {
                while (true)
                {
                    for (int i = 0; i < hopPrjs.size();)
                    {
                        ProjectConfig hopPrj = hopPrjs.get(i);

                        if (hopPrj.readyToBuild(0))
                        {
                            final ProjectConfig prjConfig = hopPrjs.remove(i);

                            builders.execute(new Runnable()
                            {
                                public void run()
                                {
                                    prjConfig.build();
                                }
                            });
                        }
                        else
                            i++;
                    }

                    if (hopPrjs.size() > 0)
                        hopPrjs.get(0).readyToBuild(1000);
                    else
                        break;
                }
            }
        }
        finally
        {
            builders.shutdown();
        }
    }

    private void checkoutProjects(List<List<ProjectConfig>> wsProjects)
    {
        int threads = ctx().get("workspace.checkout.threads", 1);
        threads = threads > 0 ? threads : 1;
        ExecutorService coWorkers = Executors.newFixedThreadPool(threads);

        try
        {
            for (List<ProjectConfig> hopPrjs : wsProjects)
            {
                for (final ProjectConfig prjConfig : hopPrjs)
                {
                    coWorkers.execute(new Runnable()
                    {
                        public void run()
                        {
                            prjConfig.checkout();
                        }
                    });
                }
            }
        }
        finally
        {
            coWorkers.shutdown();
        }
    }

    private List<List<ProjectConfig>> parseWorkspace(File wsFile)
    {
        Properties wsProps = Utils.loadProperties(wsFile, null);

        for (Object keyObj : wsProps.keySet())
        {
            String key = keyObj.toString();
            String value = wsProps.getProperty(key);
            String name = key.substring(0, key.indexOf('.'));

            if (key.endsWith(".repos"))
                getReposConfig(name)._reposClass = value;

            else if (key.endsWith(".repos.url"))
                getReposConfig(name)._url = value;

            else if (key.endsWith(".repos.user"))
                getReposConfig(name)._user = value;

            else if (key.endsWith(".repos.password"))
                getReposConfig(name)._password = value;

            else if (key.endsWith(".repos.path"))
            {
                String reposName = key.substring(key.indexOf('.') + 1, key.lastIndexOf(".repos.path"));
                getProjectConfig(name)._repos = getReposConfig(reposName);
                getProjectConfig(name)._reposPath = value;
            }
            else if (key.endsWith(".repos.rev"))
                getProjectConfig(name)._reposRev = value;

            else if (key.endsWith(".build-cmd"))
                getProjectConfig(name)._buildCmd = value;

            else if (key.endsWith(".deps"))
            {
                String[] depNames = value.split(",");
                ProjectConfig[] deps = new ProjectConfig[depNames.length];

                for (int i = 0; i < depNames.length; i++)
                    deps[i] = getProjectConfig(depNames[i].trim());

                getProjectConfig(name)._deps = deps;
            }
        }

        return filterAndSortProjectsByDependency();
    }

    private List<List<ProjectConfig>> filterAndSortProjectsByDependency()
    {
        List<List<ProjectConfig>> wsProjects = new ArrayList<List<ProjectConfig>>();

        for (ProjectConfig prj : _projects.values())
        {
            if (!needToBuild(prj))
                continue;

            int hops = maxHopsToRoot(prj);

            while (hops >= wsProjects.size())
                wsProjects.add(new ArrayList<ProjectConfig>());

            List<ProjectConfig> hopList = wsProjects.get(hops);

            hopList.add(prj);
        }

        return wsProjects;
    }

    private boolean needToBuild(ProjectConfig pc)
    {
        if (buildParams().length == 1 || pc._needToBuild)
            return true;

        for (int i = 1; i < buildParams().length; i++)
        {
            ProjectConfig prj = _projects.get(buildParams()[i]);

            if (prj == pc || prj.isDep(pc))
                return pc._needToBuild = true;
        }

        return false;
    }

    private int maxHopsToRoot(ProjectConfig pc)
    {
        if (pc._deps == null || pc._deps.length == 0)
            return 0;

        if (pc._maxHopsToRoot > 0)
            return pc._maxHopsToRoot;

        pc._maxHopsToRoot = 1;

        for (ProjectConfig dep : pc._deps)
        {
            int depMaxHops = maxHopsToRoot(dep) + 1;

            if (pc._maxHopsToRoot < depMaxHops)
                pc._maxHopsToRoot = depMaxHops;
        }

        return pc._maxHopsToRoot;
    }

    private ReposConfig getReposConfig(String name)
    {
        ReposConfig reposConfig = _repos.get(name);

        if (reposConfig == null)
        {
            reposConfig = new ReposConfig();
            _repos.put(name, reposConfig);
        }

        return reposConfig;
    }

    private ProjectConfig getProjectConfig(String name)
    {
        ProjectConfig projectConfig = _projects.get(name);

        if (projectConfig == null)
        {
            projectConfig = new ProjectConfig();
            projectConfig._name = name;
            _projects.put(name, projectConfig);
        }

        return projectConfig;
    }

    protected void printUsageHelp()
    {
        System.out.println("Usage:");
        System.out.println();
        System.out.println("    bbw <options> <workspace> [<projects>]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("    -co              : Checkout projects before building");
        System.out.println("    -D<name>=<value> : Set property");
        System.out.println("    -t               : Print trace messages");
    }

    @Override
    protected void printProjectHelp()
    {
    }
}
