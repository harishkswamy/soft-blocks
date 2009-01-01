package buildBlocks.java.jee.gwt;

import buildBlocks.FileTask;
import buildBlocks.ModuleInfo;
import buildBlocks.TaskInfo;
import buildBlocks.Utils;
import buildBlocks.java.jee.JEEModule;

/**
 * @author hkrishna
 */
@ModuleInfo(prefix = "gwtapp")
public class GWTApp<P extends GWTProject<?>> extends JEEModule<P>
{
    public GWTApp(P project)
    {
        super(project);

        project.modules(new GWTLib<P>(project));
    }

    @TaskInfo(desc = "Cleans the project's main, webapp and gwt target spaces.")
    public void clean()
    {
        super.clean();
        new FileTask(project().layout().targetGwtPath()).delete();
    }

    @TaskInfo(desc = "Generates Javascript from the project's client files.", deps = { "gwtlib:compile" })
    public void compile()
    {
        P p = project();

        if (p.moduleName() == null)
            throw new Error("GWT module name has not been specified in the project.");

        Utils.java(p.gwtCompileJavaArgs(), p.gwtClasspath(), "com.google.gwt.dev.GWTCompiler", p.gwtCompilerArgs());
    }

    @TaskInfo(desc = "Deploys the application locally in the target space.", deps = { "compile", "gwtlib:jar" })
    public void deploy()
    {
        P p = project();
        GWTLayout l = p.layout();

        FileTask fileTask = new FileTask(l.targetWebappPath()).select(".*\\.cache\\..*|.*gwt\\..*").delete();

        super.deploy();

        fileTask.reset(p.serverJarPath()).copyToDir(l.targetWebinfLib(), false);

        String gwtOut = l.targetGwtOutPath() + "/" + p.moduleName();

        fileTask.reset(gwtOut).copyToDir(l.targetWebappPath(), false);
    }
}
