package buildBlocks.java;

import buildBlocks.Module;
import buildBlocks.Artifact;
import buildBlocks.FileTask;
import buildBlocks.ModuleInfo;
import buildBlocks.TaskInfo;
import buildBlocks.ZipTask;

/**
 * @author hkrishna
 */
@ModuleInfo(prefix = "j")
public class JavaModule<P extends JavaProject<?>> extends Module<P>
{
    public JavaModule(P project)
    {
        super(project);
    }

    // Tasks ================================================================================

    @TaskInfo(desc = "Cleans the project's main target space.")
    public void clean()
    {
        new FileTask(project().layout().targetMainPath()).delete();
    }

    @TaskInfo(desc = "Compiles the project in the target space.")
    public void compile()
    {
        P p = project();
        JavaLayout l = p.layout();

        p.compiler().compile(l.mainJavaPath(), p.mainClasspath(), l.targetMainPath());

        new FileTask(l.mainResourcePath()).exclude(null).copyToDir(l.targetMainPath(), true);
    }

    @TaskInfo(desc = "Builds and runs the test suite for the project.", deps = { "compile" })
    public void test()
    {
        P p = project();
        JavaLayout l = p.layout();

        p.compiler().compile(l.testJavaPath(), p.testClasspath(), l.targetTestPath());

        new FileTask(l.testResourcePath()).exclude(null).copyToDir(l.targetTestPath(), true);
    }

    @TaskInfo(desc = "Builds and packages the project in a jar.", deps = { "compile" })
    public void jar()
    {
        P p = project();
        JavaLayout l = p.layout();

        new ZipTask(p.jarPath()).from(l.targetMainPath()).exclude(null).add().createJar();
    }

    @TaskInfo(desc = "Builds and publishes the project's jar to the local repository.", deps = { "jar" })
    public void install()
    {
        P p = project();
        Artifact.install(Artifact.toSpec(p, "jar", p.classifier()), p.jarPath());
    }
}
