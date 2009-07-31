package buildBlocks.bootstrap;

import static buildBlocks.BuildCtx.*;

import jBlocks.server.AggregateException;
import jBlocks.server.ApplicationException;
import jBlocks.server.IOUtils;
import jBlocks.server.ReflectUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author hkrishna
 */
public class WorkspaceBuilder extends Builder implements IOUtils.SyamlHandler
{
    public static void main(String[] args)
    {
        new WorkspaceBuilder(args).run();
    }

    private boolean                   _checkout;
    private boolean                   _inRepos, _inProjects;
    private ReposModel                _defRepos;
    private ProjectModel              _defproject;
    private Object                    _entity;
    private String                    _key, _value;

    private Map<String, ReposModel>   _reposMap   = new HashMap<String, ReposModel>();
    private Map<String, ProjectModel> _projectMap = new HashMap<String, ProjectModel>();

    WorkspaceBuilder(String[] args)
    {
        super(args);

        _defRepos = new ReposModel();
        _defproject = new ProjectModel(this, _checkout, ctx().traceOn());
    }

    protected boolean parse(String arg)
    {
        if ("-co".equals(arg))
            return _checkout = true;

        return false;
    }

    protected void build()
    {
        List<List<ProjectModel>> projects = parseWorkspace(new File(buildParams()[0] + ".workspace"));

        if (_checkout)
        {
            checkoutProjects(projects);
            projects.get(0).get(0).awaitCheckout();
        }

        buildProjects(projects);
    }

    private List<List<ProjectModel>> parseWorkspace(File wsFile)
    {
        try
        {
            IOUtils.readSyaml(wsFile.toURL(), this);

            return filterAndSortProjectsByDependency();
        }
        catch (Exception e)
        {
            throw AggregateException.with(e);
        }
    }

    private List<List<ProjectModel>> filterAndSortProjectsByDependency()
    {
        List<List<ProjectModel>> wsProjects = new ArrayList<List<ProjectModel>>();

        for (ProjectModel prj : _projectMap.values())
        {
            if (!needToBuild(prj))
                continue;

            int hops = maxHopsToRoot(prj);

            while (hops >= wsProjects.size())
                wsProjects.add(new ArrayList<ProjectModel>());

            List<ProjectModel> hopList = wsProjects.get(hops);

            hopList.add(prj);
        }

        return wsProjects;
    }

    private boolean needToBuild(ProjectModel pc)
    {
        if (buildParams().length == 1 || pc.needToBuild())
            return true;

        // buildParams[0] is the workspace name, the rest are project names
        for (int i = 1; i < buildParams().length; i++)
        {
            ProjectModel prj = _projectMap.get(buildParams()[i]);

            if (prj == pc || prj.isDep(pc))
                return pc.needToBuild(true);
        }

        return false;
    }

    private int maxHopsToRoot(ProjectModel pc)
    {
        if (pc.deps() == null || pc.deps().length == 0)
            return 0;

        if (pc.maxHopsToRoot() > 0)
            return pc.maxHopsToRoot();

        pc.maxHopsToRoot(1);

        for (ProjectModel dep : pc.deps())
        {
            int depMaxHops = maxHopsToRoot(dep) + 1;

            if (pc.maxHopsToRoot() < depMaxHops)
                pc.maxHopsToRoot(depMaxHops);
        }

        return pc.maxHopsToRoot();
    }

    public void handleMapping(String key, String value, int level, int line)
    {
        _key = key;
        _value = value;

        if (level == 0)
            handleLevel0();
        else if (level == 1)
            handleLevel1();
        else if (level == 2)
            handleLevel2();
        else
        {
            String fmt = "Invalid level: %s at line: %s, key: %s, value: %s. Make sure the line is indented properly using only spaces (no tabs).";
            throw new ApplicationException(String.format(fmt, level, line, key, value));
        }
    }

    private void handleLevel2()
    {
        ReflectUtils.invokeMethod(_entity, propertyName(), _value);
    }

    private String propertyName()
    {
        String[] parts = _key.split("-");

        if (parts.length <= 1)
            return _key;

        StringBuilder fieldName = new StringBuilder(parts[0]);

        for (int i = 1; i < parts.length; i++)
            fieldName.append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].substring(1));

        return fieldName.toString();
    }

    private void handleLevel1()
    {
        if ("defaults!".equals(_key))
        {
            if (_inRepos)
                _entity = _defRepos;

            else if (_inProjects)
                _entity = _defproject;
        }
        else if (_inRepos)
            _entity = getReposModel(_key);

        else if (_inProjects)
            _entity = getProjectModel(_key);
    }

    private void handleLevel0()
    {
        if ("repositories".equals(_key))
            _inProjects = !(_inRepos = true);

        else if ("projects".equals(_key))
            _inRepos = !(_inProjects = true);
        else
            _inRepos = _inProjects = false;
    }

    ReposModel getReposModel(String key)
    {
        ReposModel repos = _reposMap.get(key);

        if (repos == null)
            _reposMap.put(key, repos = _defRepos.create());

        return repos;
    }

    ProjectModel getProjectModel(String key)
    {
        ProjectModel prj = _projectMap.get(key);

        if (prj == null)
            _projectMap.put(key, prj = _defproject.create(key));

        return prj;
    }

    public void endOfFile()
    {
    }

    private void buildProjects(List<List<ProjectModel>> wsProjects)
    {
        int threads = ctx().get("workspace.builder.threads", 1);
        threads = threads > 0 ? threads : 1;
        ExecutorService builders = Executors.newFixedThreadPool(threads);

        try
        {
            for (List<ProjectModel> hopPrjs : wsProjects)
            {
                while (true)
                {
                    for (int i = 0; i < hopPrjs.size();)
                    {
                        ProjectModel hopPrj = hopPrjs.get(i);

                        if (hopPrj.readyToBuild(0))
                        {
                            final ProjectModel prjModel = hopPrjs.remove(i);

                            builders.execute(new Runnable()
                            {
                                public void run()
                                {
                                    prjModel.build();
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

    private void checkoutProjects(List<List<ProjectModel>> wsProjects)
    {
        int threads = ctx().get("workspace.checkout.threads", 1);
        threads = threads > 0 ? threads : 1;
        ExecutorService coWorkers = Executors.newFixedThreadPool(threads);

        try
        {
            for (List<ProjectModel> hopPrjs : wsProjects)
            {
                for (final ProjectModel prjModel : hopPrjs)
                {
                    coWorkers.execute(new Runnable()
                    {
                        public void run()
                        {
                            prjModel.checkout();
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
