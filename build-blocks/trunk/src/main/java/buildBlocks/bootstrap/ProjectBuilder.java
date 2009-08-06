package buildBlocks.bootstrap;

import java.net.URLClassLoader;
import java.util.Date;

import buildBlocks.Project;

/**
 * @author hkrishna
 */
public final class ProjectBuilder extends Builder
{
    public static void main(String[] args)
    {
        new ProjectBuilder(args).run();
    }

    private String         _projectPath;
    private String         _buildNum;
    private boolean        _exportBuilder;
    private String         _classpath;
    private URLClassLoader _classLoader;
    private Project<?>     _project;

    private ProjectBuilder(String[] args)
    {
        super(args);

        init("", null);
    }

    ProjectBuilder(String projectPath, String buildNum, String buildCmd)
    {
        super(buildCmd);

        init(projectPath, buildNum);
    }

    private void init(String projectPath, String buildNum)
    {
        _projectPath = projectPath;
        _buildNum = buildNum;
        _classpath = buildExtClasspath();
        _classLoader = buildExtClassLoader();
    }

    protected boolean parse(String arg)
    {
        if ("-e".equals(arg))
            return _exportBuilder = true;

        return false;
    }

    protected void build()
    {
        System.out.println();
        System.out.println("Loading project...");

        loadProject().init();

        System.out.println();

        // Execute tasks
        System.out.println(String.format("Building %s...", _project.name()));
        System.out.println();

        long start = System.currentTimeMillis();

        _project.execute(buildParams());

        long end = System.currentTimeMillis();

        System.out.println(String.format("%s built successfully in %ss at %s.", _project.name(),
            (end - start) / 1000.0, new Date()));
        System.out.println();
    }

    private Project<?> loadProject()
    {
        if (_project != null)
            return _project;

        return _project = new ProjectLoader(this).loadProject();
    }

    String projectPath()
    {
        return _projectPath;
    }

    String buildNum()
    {
        return _buildNum;
    }

    String classpath()
    {
        return _classpath;
    }

    URLClassLoader classLoader()
    {
        return _classLoader;
    }

    boolean exportBuilder()
    {
        return _exportBuilder;
    }

    void exportBuilder(String jarPath)
    {
        copyToLibExt(jarPath);
    }

    protected void printUsageHelp()
    {
        System.out.println("Usage:");
        System.out.println();
        System.out.println("    bbp <options> <tasks>");
        System.out.println();
        System.out.println("Options:");
        System.out.println("    -D<name>=<value> : Set property");
        System.out.println("    -e               : Export the project's builder as a Build-Blocks extension");
        System.out.println("    -t               : Print trace messages");
    }

    @Override
    protected void printProjectHelp()
    {
        loadProject().help();
    }
}
