package buildBlocks.java.jee.gwt;

import java.io.File;

import buildBlocks.FileTask;
import buildBlocks.Module;
import buildBlocks.ModuleInfo;
import buildBlocks.Project;
import buildBlocks.TaskInfo;
import buildBlocks.BuildUtils;
import buildBlocks.java.JavaModule;
import buildBlocks.java.jee.JEEModule;

/**
 * @author hkrishna
 */
@ModuleInfo(group = "buildBlocks", id = "gwtapp")
public class GWTApp<P extends Project<? extends GWTLayout>> extends Module<P>
{
    private String _gwtVersion;
    private String _moduleName;

    public GWTApp(String gwtVersion, String moduleName, P project)
    {
        this(gwtVersion, project);

        _moduleName = moduleName;
    }

    public GWTApp(String gwtVersion, P project)
    {
        super(project);

        _gwtVersion = gwtVersion;

        init();
    }

    private void init()
    {
        StringBuilder b = new StringBuilder();

        String userJar = b.append("com.google.gwt:gwt-user:jar:").append(_gwtVersion).toString();
        b.delete(0, b.length());
        String windowsJar = b.append("com.google.gwt:gwt-dev:jar:windows:").append(_gwtVersion).toString();
        b.delete(0, b.length());
        String servletJar = b.append("com.google.gwt:gwt-servlet:jar:").append(_gwtVersion).toString();

        P p = project();

        p.deps(servletJar);
        p.compileDeps(userJar, windowsJar);
    }

    public String moduleName()
    {
        return _moduleName;
    }

    public String[] gwtCompileJavaArgs()
    {
        return new String[] { "-Xms128M", "-Xmx256M" };
    }

    public String[] gwtCompilerArgs()
    {
        P p = project();
        GWTLayout l = p.layout();

        return new String[] { "-out", l.targetGWTOutPath(), "-gen", l.targetGWTGenPath(), _moduleName };
    }

    public String gwtClasspath()
    {
        P p = project();
        GWTLayout l = p.layout();
        JavaModule<?> jModule = p.module(JavaModule.class);

        return new StringBuilder(l.mainJavaPath()).append(File.pathSeparator).append(l.targetMainBinPath()).append(
            File.pathSeparator).append(jModule.mainClasspath()).toString();
    }

    @TaskInfo(desc = "Cleans the project's main, webapp and gwt target spaces.", deps = { "buildBlocks.jee:clean" })
    public void clean()
    {
        new FileTask(project().layout().targetGWTPath()).delete();
    }

    @TaskInfo(desc = "Generates Javascript from the project's client files.", deps = { "buildBlocks.j:compile" })
    public void compile()
    {
        if (moduleName() == null)
            throw new Error("GWT module name has not been specified in the project.");

        BuildUtils.java(gwtCompileJavaArgs(), gwtClasspath(), "com.google.gwt.dev.GWTCompiler", gwtCompilerArgs());
    }

    @TaskInfo(desc = "Deploys the application locally in the target space.", deps = { "compile",
        "buildBlocks.gwtlib:jar" })
    public void deploy()
    {
        P p = project();
        GWTLayout l = p.layout();

        FileTask fileTask = new FileTask(l.targetWebappPath()).select(".*\\.cache\\..*|.*gwt\\..*").delete();

        JEEModule<?> jeeModule = p.module(JEEModule.class);
        jeeModule.deploy();

        GWTLib<?> gwtLib = p.module(GWTLib.class);

        fileTask.reset(gwtLib.serverJarPath()).copyToDir(l.targetWebinfLib(), false);

        String gwtOut = l.targetGWTOutPath() + "/" + moduleName();

        fileTask.reset(gwtOut).copyToDir(l.targetWebappPath(), false);
    }

    @TaskInfo(desc = "Builds a JEE war file of the GWT application.", deps = { "deploy" })
    public void war()
    {
        P p = project();
        JEEModule<?> jeeModule = p.module(JEEModule.class);
        jeeModule.war();
    }
}
