package buildBlocks;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hkrishna
 */
public abstract class Project<L extends Layout> implements TaskContainer
{
    private String         _group;
    private String         _id;
    private String         _name;
    private String         _version;
    private String         _classifier;

    private L              _layout;

    private List<Artifact> _buildDeps   = new ArrayList<Artifact>();
    private List<Artifact> _testDeps    = new ArrayList<Artifact>();
    private List<Artifact> _runtimeDeps = new ArrayList<Artifact>();

    private TaskManager    _taskManager = new TaskManager(this);

    protected Project(L layout)
    {
        ProjectInfo info = getClass().getAnnotation(ProjectInfo.class);

        if (info == null)
            throw new Error("Projects must be annotated with " + ProjectInfo.class);

        _group = info.group();
        _id = info.id().trim().length() == 0 ? getClass().getSimpleName() : info.id();
        _name = info.name().trim().length() == 0 ? _id : info.name();
        _version = info.version() + ".SNAPSHOT";

        _layout = layout;
    }

    public String group()
    {
        return _group;
    }

    public String id()
    {
        return _id;
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

    public String prefix()
    {
        return "";
    }

    public L layout()
    {
        return _layout;
    }

    public void modules(Module<?>... modules)
    {
        _taskManager.register(modules);
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

    public void build(String... tasks)
    {
        if (tasks.length == 0)
            help();
        else
        {
            System.out.println(String.format("Building %s...", name()));
            System.out.println();

            long start = System.currentTimeMillis();

            _taskManager.execute(tasks);

            long end = System.currentTimeMillis();

            System.out.println(String.format("Build completed in %ss.", (end - start) / 1000.0));
            System.out.println();
        }
    }

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

    @Override
    public String toString()
    {
        return name();
    }
}
