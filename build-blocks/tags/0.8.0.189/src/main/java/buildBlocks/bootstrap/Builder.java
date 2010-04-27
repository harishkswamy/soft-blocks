package buildBlocks.bootstrap;

import static buildBlocks.BuildCtx.*;

import jBlocks.server.Utils;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;

import buildBlocks.BuildError;
import buildBlocks.FileTask;

/**
 * @author hkrishna
 */
abstract class Builder
{
    private static final String     _version = "0.8.0";
    private static final String     _libExt  = "lib/ext";

    private static volatile String  _home;
    private static volatile String  _bootClasspath;
    private static volatile boolean _buildFailed;

    public static String version()
    {
        return _version;
    }

    private String[] _buildParams;

    protected Builder(String[] args)
    {
        if (args.length < 2)
            throw new Error("Insufficient arguments to builder.");

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
        {
            long start = System.currentTimeMillis();

            public void run()
            {
                long end = System.currentTimeMillis();

                if (_buildFailed)
                    System.out.println("BUILD FAILED!\n");

                System.out.println(String.format("Build completed in %ss.", (end - start) / 1000.0));
                System.out.println();
            }
        }));

        _home = args[0];
        _bootClasspath = args[1];

        String[] bArgs = new String[args.length - 2];
        System.arraycopy(args, 2, bArgs, 0, bArgs.length);

        parse(bArgs);
    }

    protected Builder(String cmd)
    {
        parse(Utils.splitQuoted(cmd, ' ').toArray(new String[0]));
    }

    protected void parse(String[] args)
    {
        int start = 0;

        for (int i = 0; i < args.length; i++)
        {
            if ("-t".equals(args[i]))
                ctx().setTrace(true);

            else if (args[i].startsWith("-D"))
            {
                String[] prop = args[i].substring(2).split("=");
                ctx().putInThread(prop[0].trim(), prop[1].trim());
            }
            else if (parse(args[i]))
                ; // Handled by the subclass

            else if (args[i].startsWith("-"))
                throw new BuildError(String.format("Invalid option : %s.", args[i]), true, false);

            else
                break;

            start = i + 1;
        }

        _buildParams = new String[args.length - start];

        System.arraycopy(args, start, _buildParams, 0, args.length - start);
    }

    protected abstract boolean parse(String arg);

    protected String buildExtClasspath()
    {
        File[] extJars = extJars();

        StringBuilder b = new StringBuilder(_bootClasspath);

        for (File jar : extJars)
            b.append(File.pathSeparatorChar).append(jar.getPath());

        return b.toString();
    }

    protected URLClassLoader buildExtClassLoader()
    {
        try
        {
            File[] extJars = extJars();
            URL[] urls = new URL[extJars.length];

            for (int i = 0; i < extJars.length; i++)
                urls[i] = extJars[i].toURL();

            return new URLClassLoader(urls);
        }
        catch (Exception e)
        {
            throw new Error("Unable to build extension class loader", e);
        }
    }

    private File[] extJars()
    {
        return new File(_home, _libExt).listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.endsWith(".jar");
            }
        });
    }

    protected String[] buildParams()
    {
        return _buildParams;
    }

    protected void run()
    {
        try
        {
            System.out.println();
            System.out.println("Loading builder...");
            System.out.println();

            if (_buildParams.length == 0)
            {
                printUseHelp();
                printProjectHelp();
            }
            else
                build();
        }
        catch (Throwable t)
        {
            handleError(t);
            System.exit(-1);
        }
    }

    protected void handleError(Throwable t)
    {
        _buildFailed = true;

        if (ctx().traceOn())
            t.printStackTrace();
        else
            System.out.println(String.format("%s%n%nUse -t option to see the stack trace.", t));

        System.out.println();

        if (t instanceof BuildError)
        {
            BuildError e = (BuildError) t;

            if (e.showUsageHelp())
                printUseHelp();

            if (e.showProjectHelp())
                printProjectHelp();
        }
    }

    protected abstract void build();

    private void printUseHelp()
    {
        System.out.println("Build Blocks");
        System.out.println("Version : " + version());
        System.out.println("--------------------------------------------------------------");

        printUsageHelp();

        System.out.println("--------------------------------------------------------------");
        System.out.println();
    }

    protected abstract void printUsageHelp();

    protected abstract void printProjectHelp();

    protected void copyToLibExt(String jarPath)
    {
        new FileTask(jarPath).copyToDir(_home + _libExt, true);
    }
}
