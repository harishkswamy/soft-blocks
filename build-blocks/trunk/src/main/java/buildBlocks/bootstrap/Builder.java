package buildBlocks.bootstrap;

import static buildBlocks.Context.*;

import buildBlocks.BuildError;

/**
 * @author hkrishna
 */
public class Builder
{
    private static final String VERSION = "0.8.0";

    public static void main(String[] args)
    {
        new Builder().run(args);
    }

    private BuilderCtx _builderCtx;

    private Builder()
    {
    }

    String version()
    {
        return VERSION;
    }

    BuilderCtx builderCtx()
    {
        return _builderCtx;
    }

    private void run(String[] args)
    {
        try
        {
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
            {
                long start = System.currentTimeMillis();

                public void run()
                {
                    long end = System.currentTimeMillis();
                    System.out.println(String.format("Build completed in %ss.", (end - start) / 1000.0));
                    System.out.println();
                }
            }));

            System.out.println();
            System.out.println("Loading...");
            System.out.println();

            // Parse arguments
            _builderCtx = new BuilderCtx(args);

            if (_builderCtx.tasks().length == 0)
                printUsageHelp();
            else if (_builderCtx.buildWorkspace())
                new WorkspaceBuilder(version(), _builderCtx).build();
            else
                new ProjectBuilder(version(), _builderCtx).build();
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

                if (e.showUsageHelp())
                    printUsageHelp();

                if (e.showProjectHelp() && e.project() != null)
                    e.project().help();
            }

            System.out.println("Build failed.");
            System.out.println();
            System.exit(-1);
        }
    }

    private void printUsageHelp()
    {
        System.out.println("Build Blocks");
        System.out.println("Version : " + VERSION);
        System.out.println("--------------------------------------------------------------");
        System.out.println("Workspace Usage:");
        System.out.println();
        System.out.println("    bb <options> <workspace> [<projects>]");
        System.out.println();
        System.out.println("Options:");
        BuilderCtx.printWorkspaceOptions();
        System.out.println();
        System.out.println();
        System.out.println("Project Usage:");
        System.out.println();
        System.out.println("    bb <options> <tasks>");
        System.out.println();
        System.out.println("Options:");
        BuilderCtx.printProjectOptions();

        System.out.println("--------------------------------------------------------------");
        System.out.println();
    }
}
