package buildBlocks.java.jee;

import buildBlocks.Artifact;
import buildBlocks.FileTask;
import buildBlocks.JarTask;
import buildBlocks.Module;
import buildBlocks.ModuleInfo;
import buildBlocks.Project;
import buildBlocks.TaskInfo;
import buildBlocks.java.JavaModule;

/**
 * @author hkrishna
 */
@ModuleInfo(group = "buildBlocks", id = "jee")
public class JEEModule<P extends Project<? extends JEELayout>> extends Module<P>
{
    public JEEModule(P project)
    {
        super(project);
    }

    @TaskInfo(desc = "Cleans the project's main and webapp target spaces.", deps = { "buildBlocks.j:clean" })
    public void clean()
    {
        new FileTask(project().layout().targetWebappPath()).delete();
    }

    @TaskInfo(desc = "Deploys the application locally in the target space.", deps = { "buildBlocks.j:jar" })
    public void deploy()
    {
        P p = project();
        JEELayout l = p.layout();

        FileTask fileTask = new FileTask(l.sourceWebappPath()).exclude((String) null);
        fileTask.copyToDir(l.targetWebappPath(), false);

        String libPath = l.targetWebinfLib();
        JavaModule<?> jModule = p.module(JavaModule.class);

        fileTask.reset(jModule.jarPath()).copyToDir(libPath, false);

        for (Artifact artifact : p.deps())
            fileTask.reset(artifact.getPath()).copyToDir(libPath, false);

        for (Artifact artifact : p.runtimeDeps())
            fileTask.reset(artifact.getPath()).copyToDir(libPath, false);
    }

    @TaskInfo(desc = "Builds and packages the project as JEE war.", deps = { "deploy" })
    public void war()
    {
        P p = project();
        JEELayout l = p.layout();

        JavaModule<?> jModule = p.module(JavaModule.class);
        String warPathName = jModule.jarPath().replaceFirst("\\.jar", ".war");

        new JarTask(warPathName, p.buildId()).from(l.targetWebappPath()).exclude(null).add().create();
    }
}
