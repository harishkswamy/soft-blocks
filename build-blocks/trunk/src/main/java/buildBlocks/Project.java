package buildBlocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hkrishna
 */
public abstract class Project<L extends Layout> implements TaskContainer
{
    private String                                  _group;
    private String                                  _id;
    private String                                  _qid;
    private String                                  _name;
    private String                                  _version;
    private String                                  _classifier;

    private L                                       _layout;

    private List<Artifact>                          _deps        = new ArrayList<Artifact>();
    private List<Artifact>                          _compileDeps = new ArrayList<Artifact>();
    private List<Artifact>                          _testDeps    = new ArrayList<Artifact>();
    private List<Artifact>                          _runtimeDeps = new ArrayList<Artifact>();

    private TaskManager                             _taskManager = new TaskManager();
    @SuppressWarnings("unchecked")
    private Map<Class<? extends Module>, Module<?>> _modules     = new HashMap<Class<? extends Module>, Module<?>>();

    protected Project(L layout)
    {
        ProjectInfo info = getClass().getAnnotation(ProjectInfo.class);

        if (info == null)
            throw new Error("Projects must be annotated with " + ProjectInfo.class);

        _group = info.group().trim().length() == 0 ? getClass().getPackage().getName() : info.group();
        _id = info.id().trim().length() == 0 ? getClass().getSimpleName() : info.id();
        _qid = _group + '.' + _id;
        _name = info.name().trim().length() == 0 ? _id : info.name();
        _version = info.version() + ".SNAPSHOT";

        _layout = layout;

        _taskManager.register(this);
    }

    // Public API ==================================================

    public String group()
    {
        return _group;
    }

    public String id()
    {
        return _id;
    }

    public String qid()
    {
        return _qid;
    }

    public String name()
    {
        return _name;
    }

    public void version(String version)
    {
        _version = version;
    }

    public String version()
    {
        return _version;
    }

    public void buildNumber(String num)
    {
        version(version().replaceFirst("SNAPSHOT", num));
    }

    public void classifier(String name)
    {
        _classifier = name;
    }

    public String classifier()
    {
        return _classifier;
    }

    public L layout()
    {
        return _layout;
    }

    public void modules(Module<?>... modules)
    {
        for (Module<?> module : modules)
            module(module.getClass(), module);
    }

    @SuppressWarnings("unchecked")
    public void module(Class<? extends Module> moduleClass, Module<?> module)
    {
        Module<?> oldMod = _modules.get(moduleClass);

        if (oldMod != null)
        {
            _taskManager.unregister(oldMod);

            String fmt = "WARNING: Overwriting module %s with module %s.";
            System.out.println(String.format(fmt, oldMod, module));
        }

        _modules.put(moduleClass, module);
        _taskManager.register(module);
    }

    public <T extends Module<?>> T module(Class<T> moduleClass)
    {
        T module = moduleClass.cast(_modules.get(moduleClass));

        if (module == null)
            throw new Error(String.format("Module %s has not been registered in the project.", moduleClass));

        return module;
    }

    private void addDeps(List<Artifact> cache, String[] deps)
    {
        if (deps == null)
            return;

        for (String dep : deps)
        {
            Artifact artifact = Artifact.find(dep);

            if (!cache.contains(artifact))
                cache.add(artifact);
        }
    }

    /**
     * Dependencies required to compile, test and run the source.
     */
    public void deps(String... deps)
    {
        addDeps(_deps, deps);
    }

    public List<Artifact> deps()
    {
        return _deps;
    }

    /**
     * Dependencies required only to compile the source.
     */
    public void compileDeps(String... deps)
    {
        addDeps(_compileDeps, deps);
    }

    public List<Artifact> compileDeps()
    {
        return _compileDeps;
    }

    /**
     * Dependencies required only to compile and run the test suite.
     */
    public void testDeps(String... deps)
    {
        addDeps(_testDeps, deps);
    }

    public List<Artifact> testDeps()
    {
        return _testDeps;
    }

    /**
     * Dependencies required only to run the source.
     */
    public void runtimeDeps(String... deps)
    {
        addDeps(_runtimeDeps, deps);
    }

    public List<Artifact> runtimeDeps()
    {
        return _runtimeDeps;
    }

    public void projectDeps(Project<?>... projects)
    {
        for (Project<?> p : projects)
        {
            _deps.addAll(p.deps());
            _compileDeps.addAll(p.compileDeps());
            _testDeps.addAll(p.testDeps());
            _runtimeDeps.addAll(p.runtimeDeps());

            deps(p.artifacts());
            compileDeps(p.compileArtifacts());
            runtimeDeps(p.runtimeArtifacts());
        }
    }

    protected String[] artifacts()
    {
        return null;
    }

    protected String[] compileArtifacts()
    {
        return null;
    }

    protected String[] runtimeArtifacts()
    {
        return null;
    }

    public void execute(String... tasks)
    {
        _taskManager.execute(tasks);
    }

    // Common tasks ==============================================

    @TaskInfo(desc = "Prints the project's help text.")
    public void help()
    {
        System.out.println("--------------------------------------------------------------");
        System.out.println(String.format("Tasks available for %s:", name()));

        _taskManager.printHelp();

        System.out.println("--------------------------------------------------------------");
        System.out.println();
    }

    @TaskInfo(desc = "Cleans the project's target space.")
    public void clean()
    {
        new FileTask(layout().targetPath()).delete();
    }

    // Object impl ====================================================

    @Override
    public String toString()
    {
        return name();
    }
}
