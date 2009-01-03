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
    private String                                     _group;
    private String                                     _id;
    private String                                     _qid;
    private String                                     _name;
    private String                                     _version;
    private String                                     _classifier;

    private L                                          _layout;

    private List<Artifact>                             _buildDeps   = new ArrayList<Artifact>();
    private List<Artifact>                             _testDeps    = new ArrayList<Artifact>();
    private List<Artifact>                             _runtimeDeps = new ArrayList<Artifact>();

    private TaskManager                                _taskManager = new TaskManager();
    private Map<Class<? extends Module<?>>, Module<?>> _modules     = new HashMap<Class<? extends Module<?>>, Module<?>>();

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

    @SuppressWarnings("unchecked")
    public void modules(Module<?>... modules)
    {
        for (Module<?> module : modules)
            module((Class<? extends Module<?>>) module.getClass(), module);
    }

    public void module(Class<? extends Module<?>> moduleClass, Module<?> module)
    {
        if (_modules.containsKey(moduleClass))
        {
            System.out.println("WARNING: Skipping " + module + " registration; a module is already registered for "
                + moduleClass);
            return;
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

    /**
     * Dependencies required to build the source.
     */
    public void buildDeps(String... deps)
    {
        for (String dep : deps)
            _buildDeps.add(Artifact.find(dep));
    }

    public List<Artifact> buildDeps()
    {
        return _buildDeps;
    }

    /**
     * Dependencies required to build the test suite.
     */
    public void testDeps(String... deps)
    {
        for (String dep : deps)
            _testDeps.add(Artifact.find(dep));
    }

    public List<Artifact> testDeps()
    {
        return _testDeps;
    }

    /**
     * Dependencies required at runtime.
     */
    public void runtimeDeps(String... deps)
    {
        for (String dep : deps)
            _runtimeDeps.add(Artifact.find(dep));
    }

    public List<Artifact> runtimeDeps()
    {
        return _runtimeDeps;
    }

    public void projectDeps(Project<?>... projects)
    {
        for (Project<?> p : projects)
        {
            _buildDeps.addAll(p.buildDeps());
            _testDeps.addAll(p.testDeps());
            _runtimeDeps.addAll(p.runtimeDeps());

            buildDeps(p.buildArtifacts());
            runtimeDeps(p.runtimeArtifacts());
        }
    }

    protected String[] buildArtifacts()
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
