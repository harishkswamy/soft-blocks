package buildBlocks.bootstrap;

import static buildBlocks.Context.*;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import buildBlocks.BuildError;
import buildBlocks.FileTask;

/**
 * @author hkrishna
 */
public class BuilderCtx
{
    static void printWorkspaceOptions()
    {
        System.out.println("    -cws             : Clean and build workspace");
        System.out.println("    -D<name>=<value> : Set property");
        System.out.println("    -t               : Print trace messages");
        System.out.println("    -ws              : Build workspace");
    }

    static void printProjectOptions()
    {
        System.out.println("    -D<name>=<value> : Set property");
        System.out.println("    -e               : Export the project's builder");
        System.out.println("    -t               : Print trace messages");
    }

    private String              _bbHome;
    private String              _bbClasspath;
    private URLClassLoader      _bbClassLoader;
    private boolean             _cleanWorkspace;
    private boolean             _buildWorkspace;
    private boolean             _exportProject;
    private String[]            _args;
    private Map<String, String> _props = new HashMap<String, String>();

    BuilderCtx(String[] args)
    {
        parse(args);
        _bbClasspath = buildClasspath();
        _bbClassLoader = buildClassLoader();
    }

    private BuilderCtx()
    {

    }

    public BuilderCtx create(String cmd)
    {
        BuilderCtx bArgs = new BuilderCtx();
        bArgs._bbHome = _bbHome;
        bArgs._bbClasspath = buildClasspath();
        bArgs._bbClassLoader = buildClassLoader();
        bArgs.parse(cmd.split(" "), 0);

        return bArgs;
    }

    private String buildClasspath()
    {
        File[] extJars = extJars();

        StringBuilder b = new StringBuilder(_bbClasspath);

        for (File jar : extJars)
            b.append(File.pathSeparatorChar).append(jar.getPath());

        return b.toString();
    }

    private URLClassLoader buildClassLoader()
    {
        File[] extJars = extJars();
        URL[] urls = new URL[extJars.length];

        try
        {
            for (int i = 0; i < extJars.length; i++)
                urls[i] = extJars[i].toURL();
        }
        catch (Exception e)
        {
            throw new BuildError(e, false, false);
        }

        return new URLClassLoader(urls);
    }

    private File[] extJars()
    {
        return new File(_bbHome, "lib/builders").listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.endsWith(".jar");
            }
        });
    }

    private void parse(String[] args)
    {
        if (args.length < 2)
            throw new Error("Insufficient arguments to builder.");

        _bbHome = args[0];
        _bbClasspath = args[1];

        parse(args, 2);
    }

    private void parse(String[] args, int start)
    {
        for (int i = start; i < args.length; i++)
        {
            if ("-ws".equals(args[i]))
                _buildWorkspace = true;

            else if ("-cws".equals(args[i]))
                _buildWorkspace = _cleanWorkspace = true;

            else if ("-e".equals(args[i]))
                _exportProject = true;

            else if ("-t".equals(args[i]))
                ctx().trace(true);

            else if (args[i].startsWith("-D"))
            {
                String[] prop = args[i].substring(2).split("=");
                _props.put(prop[0].trim(), prop[1].trim());
            }
            else if (args[i].startsWith("-"))
                throw new BuildError(String.format("Invalid option : %s.", args[i]), true, false);

            else
                break;

            start = i + 1;
        }

        _args = new String[args.length - start];

        System.arraycopy(args, start, _args, 0, args.length - start);
    }

    String bbHome()
    {
        return _bbHome;
    }

    String bbClasspath()
    {
        return _bbClasspath;
    }

    URLClassLoader bbClassLoader()
    {
        return _bbClassLoader;
    }

    boolean cleanWorkspace()
    {
        return _cleanWorkspace;
    }

    boolean buildWorkspace()
    {
        return _buildWorkspace;
    }

    boolean exportProject()
    {
        return _exportProject;
    }

    String[] workspace()
    {
        return _args;
    }

    String[] tasks()
    {
        return _args;
    }

    void exportBuilder(String jarPath)
    {
        new FileTask(jarPath).copyToDir(_bbHome + "lib/builders", true);
    }

    public String property(String key)
    {
        return _props.get(key);
    }

    public String setProperty(String key, String value)
    {
        return _props.put(key, value);
    }
}
