package buildBlocks.java.jee;

import buildBlocks.Artifact;
import buildBlocks.FileTask;
import buildBlocks.ModuleInfo;
import buildBlocks.TaskInfo;
import buildBlocks.ZipTask;
import buildBlocks.java.JavaModule;

/**
 * @author hkrishna
 */
@ModuleInfo(prefix = "jee")
public class JEEModule<P extends JEEProject<?>> extends JavaModule<P>
{
    public JEEModule(P project)
    {
        super(project);
    }

    @TaskInfo(desc = "Cleans the project's main and webapp target spaces.")
    public void clean()
    {
        super.clean();
        new FileTask(project().layout().targetWebappPath()).delete();
    }

    @TaskInfo(desc = "Deploys the application locally in the target space.", deps = { "jar" })
    public void deploy()
    {
        P p = project();
        JEELayout l = p.layout();

        FileTask fileTask = new FileTask(l.sourceWebappPath()).exclude(null);
        fileTask.copyToDir(l.targetWebappPath(), false);

        String libPath = l.targetWebinfLib();

        fileTask.reset(p.jarPath()).copyToDir(libPath, false);

        for (Artifact artifact : p.runtimeDeps())
            fileTask.reset(artifact.getPath()).copyToDir(libPath, false);
    }

    @TaskInfo(desc = "Builds and packages the project as JEE war.", deps = { "deploy" })
    public void war()
    {
        P p = project();
        JEELayout l = p.layout();

        String warPathName = p.jarPath().replaceFirst("\\.jar", ".war");

        new ZipTask(warPathName).from(l.targetWebappPath()).exclude(null).add().createJar();
    }
}
