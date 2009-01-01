package buildBlocks.bootstrap;

import static buildBlocks.Context.*;

import buildBlocks.BuildError;
import buildBlocks.Project;
import buildBlocks.ProjectInfo;
import buildBlocks.java.JavaLayout;
import buildBlocks.java.JavaProject;

/**
 * @author hkrishna
 */
@ProjectInfo(group = "com.google.code.soft-blocks", version = "0.8.0")
public final class Builder extends JavaProject<JavaLayout>
{
    public static void main(String[] args)
    {
        new Builder().run(args);
    }

    private String     _bbClasspath;
    private Project<?> _project;

    private Builder()
    {
        super("1.5", new JavaLayout());
    }

    private void run(String[] args)
    {
        try
        {
            System.out.println();

            BuilderArgs bArgs = new BuilderArgs(args);

            _bbClasspath = bArgs.bbClasspath();

            BuilderModule module = new BuilderModule(this, bArgs.bbHome());
            _project = module.buildProject(bArgs.exportProject());

            if (bArgs.projectTasks().length == 0)
                printHelp(true, true);
            else
                _project.build(bArgs.projectTasks());
        }
        catch (Throwable t)
        {
            if (ctx().traceOn())
                t.printStackTrace();
            else
                System.out.println(String.format("%s%n%nUse -t option to see the stack trace.", t));

            System.out.println();

            if (t instanceof BuildError)
            {
                BuildError e = (BuildError) t;
                printHelp(e.showUsageHelp(), e.showProjectHelp());
            }

            System.out.println("Build failed.");
            System.out.println();
            System.exit(-1);
        }
    }

    private void printHelp(boolean showUsageHelp, boolean showProjectHelp)
    {
        if (showUsageHelp)
            printUsageHelp();

        if (_project != null && showProjectHelp)
            _project.help();
    }

    private void printUsageHelp()
    {
        System.out.println("Build Blocks");
        System.out.println("Version : " + version());
        System.out.println("--------------------------------------------------------------");
        System.out.println("Usage:");
        System.out.println("    bb <options> <tasks>");

        System.out.println("Options:");
        BuilderArgs.printOptions();

        System.out.println("--------------------------------------------------------------");
        System.out.println();
    }

    @Override
    public String mainClasspath()
    {
        return _bbClasspath;
    }
}
