package buildBlocks.bootstrap;

import static buildBlocks.Context.*;

import buildBlocks.BuildError;
import buildBlocks.Project;
import buildBlocks.ProjectInfo;
import buildBlocks.java.JavaLayout;

/**
 * @author hkrishna
 */
@ProjectInfo(group = "com.google.code.soft-blocks", version = "0.8.0")
public final class TaskExecutor extends Project<JavaLayout>
{
    public static void main(String[] args)
    {
        new TaskExecutor().run(args);
    }

    private Project<?> _project;

    private TaskExecutor()
    {
        super(new JavaLayout());
    }

    private void run(String[] args)
    {
        try
        {
            System.out.println();
            //System.out.println("Preparing project...");

            // Parse arguments
            ArgsParser bArgs = new ArgsParser(args);

            // Load project
            ProjectLoader loader = new ProjectLoader(this, bArgs.bbHome(), bArgs.bbClasspath());
            _project = loader.loadProject(bArgs.exportProject());

            // Execute tasks
            if (bArgs.projectTasks().length == 0)
                printHelp(true, true);
            else
            {
                System.out.println(String.format("Building %s...", _project.name()));
                System.out.println();

                long start = System.currentTimeMillis();

                _project.execute(bArgs.projectTasks());

                long end = System.currentTimeMillis();

                System.out.println(String.format("Build succeeded in %ss.", (end - start) / 1000.0));
                System.out.println();
            }
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
        ArgsParser.printOptions();

        System.out.println("--------------------------------------------------------------");
        System.out.println();
    }
}
