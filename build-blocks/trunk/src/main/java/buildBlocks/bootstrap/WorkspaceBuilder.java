package buildBlocks.bootstrap;

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
import buildBlocks.Context;
import buildBlocks.FileTask;
import buildBlocks.Utils;
import buildBlocks.repos.SourceRepository;

/**
 * @author hkrishna
 */
public class WorkspaceBuilder
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
                SourceRepository repos = (SourceRepository) Utils.loadClass(getClass().getClassLoader(), _reposClass)
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

        CountDownLatch           _checkout = new CountDownLatch(1);
        CountDownLatch           _build    = new CountDownLatch(1);

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
            finally
            {
                _checkout.countDown();
            }
        }

        void awaitCheckout()
        {
            try
            {
                _checkout.await();
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
                if (_checkout.await(awaitMs, TimeUnit.MILLISECONDS))
                {
                    if (_deps == null)
                        return true;

                    for (ProjectConfig dep : _deps)
                    {
                        if (!dep._build.await(awaitMs, TimeUnit.MILLISECONDS))
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
            try
            {
                BuilderCtx bCtx = _builderCtx.create(_buildCmd);
                bCtx.setProperty("build#", _reposRev);

                ProjectBuilder builder = new ProjectBuilder(_version, bCtx);
                builder.layout().projectPath(_name);
                builder.build();
            }
            catch (Exception e)
            {
                throw new BuildError(e, false, false);
            }
            finally
            {
                _build.countDown();
            }
        }
    }

    private volatile String            _version;
    private volatile BuilderCtx        _builderCtx;
    private Map<String, ReposConfig>   _repos    = new HashMap<String, ReposConfig>();
    private Map<String, ProjectConfig> _projects = new HashMap<String, ProjectConfig>();

    WorkspaceBuilder(String version, BuilderCtx builderCtx)
    {
        _version = version;
        _builderCtx = builderCtx;
    }

    void build()
    {
        if (_builderCtx.cleanWorkspace())
            cleanWorkspace();

        List<List<ProjectConfig>> projects = parseWorkspace(new File(_builderCtx.workspace()[0] + ".workspace"));

        checkoutProjects(projects);

        projects.get(0).get(0).awaitCheckout();

        buildProjects(projects);
    }

    private void cleanWorkspace()
    {
        FileTask fileTask = new FileTask("");

        for (File file : new File(".").listFiles())
        {
            if (file.isDirectory())
                fileTask.reset(file.getPath()).delete();
        }
    }

    private void buildProjects(List<List<ProjectConfig>> wsProjects)
    {
        int threads = Integer.parseInt(Context.ctx().property("workspace.builder.threads", "1"));
        threads = threads > 0 ? threads : 1;
        ExecutorService builders = Executors.newFixedThreadPool(threads);

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

        builders.shutdown();
    }

    private void checkoutProjects(List<List<ProjectConfig>> wsProjects)
    {
        int threads = Integer.parseInt(Context.ctx().property("workspace.checkout.threads", "1"));
        threads = threads > 0 ? threads : 1;
        ExecutorService coWorkers = Executors.newFixedThreadPool(threads);

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

        coWorkers.shutdown();
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
        if (_builderCtx.workspace().length == 1 || pc._needToBuild)
            return true;

        for (int i = 1; i < _builderCtx.workspace().length; i++)
        {
            ProjectConfig prj = _projects.get(_builderCtx.workspace()[i]);

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
}
