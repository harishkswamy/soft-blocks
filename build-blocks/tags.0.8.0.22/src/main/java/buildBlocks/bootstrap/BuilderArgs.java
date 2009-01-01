package buildBlocks.bootstrap;

import static buildBlocks.Context.*;

import buildBlocks.BuildError;

/**
 * @author hkrishna
 */
public class BuilderArgs
{
    static void printOptions()
    {
        System.out.println("    -D<name>=<value> : Set builder property");
        System.out.println("    -e               : Export the project's build jar to build-blocks' lib");
        System.out.println("    -t               : Print trace messages");
    }

    private String   _bbHome;
    private String   _bbClasspath;
    private boolean  _exportProject;
    private String[] _projectTasks;

    BuilderArgs(String[] args)
    {
        parse(args);
    }

    private void parse(String[] args)
    {
        if (args.length < 2)
            throw new Error("Insufficient arguments to builder.");

        _bbHome = args[0];
        _bbClasspath = args[1];

        int j = 2;

        for (int i = 2; i < args.length; i++)
        {
            if ("-e".equals(args[i]))
                _exportProject = true;

            else if ("-t".equals(args[i]))
                ctx().trace(true);

            else if (args[i].startsWith("-D"))
            {
                String[] prop = args[i].substring(2).split("=");
                ctx().setProperty(prop[0].trim(), prop[1].trim());
            }
            else if (args[i].startsWith("-"))
                throw new BuildError(String.format("Invalid option : %s.", args[i]), true, false);

            else
                break;

            j = i + 1;
        }

        _projectTasks = new String[args.length - j];

        System.arraycopy(args, j, _projectTasks, 0, args.length - j);
    }

    String bbHome()
    {
        return _bbHome;
    }

    String bbClasspath()
    {
        return _bbClasspath;
    }

    boolean exportProject()
    {
        return _exportProject;
    }

    String[] projectTasks()
    {
        return _projectTasks;
    }
}
