package buildBlocks.java.jee.gwt;

import buildBlocks.Artifact;
import buildBlocks.ModuleInfo;
import buildBlocks.TaskInfo;
import buildBlocks.ZipTask;
import buildBlocks.java.JavaLayout;
import buildBlocks.java.JavaModule;

/**
 * @author hkrishna
 */
@ModuleInfo(prefix = "gwtlib")
public class GWTLib<P extends GWTProject<?>> extends JavaModule<P>
{
    public GWTLib(P project)
    {
        super(project);
    }

    @TaskInfo(desc = "Builds and packages the project in separate client and server jars.", deps = { "compile" })
    public void jar()
    {
        P p = project();
        JavaLayout l = p.layout();

        String clientExcludes = ".*/server(/.*)?";

        ZipTask zipTask = new ZipTask(p.clientJarPath()).to("").from(l.targetMainPath()).exclude(clientExcludes).add();
        zipTask.from(l.mainJavaPath()).exclude(clientExcludes).add().createJar();

        String serverExcludes = ".*/client(/.*)?|.*/generators(/.*)?|.*/public(/.*)?|.*.gwt.xml";

        zipTask.reset(p.serverJarPath()).to("").from(l.targetMainPath()).exclude(serverExcludes).add().createJar();
    }

    @TaskInfo(desc = "Builds and publishes the project's client and server jars to the local repository.", deps = { "jar" })
    public void install()
    {
        P p = project();
        Artifact.install(p.clientJarSpec(), p.clientJarPath());
        Artifact.install(p.serverJarSpec(), p.serverJarPath());
    }
}
