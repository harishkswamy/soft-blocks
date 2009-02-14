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

    private BuilderArgs _builderArgs;

    private Builder()
    {
    }

    String version()
    {
        return VERSION;
    }

    BuilderArgs builderArgs()
    {
        return _builderArgs;
    }

    private void run(String[] args)
    {
        try
        {
            System.out.println();
            System.out.println("Loading...");
            System.out.println();

            // Parse arguments
            _builderArgs = new BuilderArgs(args);

            if (_builderArgs.buildWorkspace())
                new WorkspaceBuilder(version(), _builderArgs).build();
            else
                new ProjectBuilder(version(), _builderArgs).build();
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
        System.out.println("Usage:");
        System.out.println("    bb <options> [<workspaces> | <tasks>]");

        System.out.println("Options:");
        BuilderArgs.printOptions();

        System.out.println("--------------------------------------------------------------");
        System.out.println();
    }
}
