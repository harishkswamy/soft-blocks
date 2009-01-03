package buildBlocks.java.jee.gwt;

import buildBlocks.Artifact;
import buildBlocks.Module;
import buildBlocks.ModuleInfo;
import buildBlocks.Project;
import buildBlocks.TaskInfo;
import buildBlocks.ZipTask;
import buildBlocks.java.JavaLayout;

/**
 * @author hkrishna
 */
@ModuleInfo(group = "buildBlocks", id = "gwtlib")
public class GWTLib<P extends Project<? extends JavaLayout>> extends Module<P>
{
    private String _clientJarPath;
    private String _serverJarPath;
    private String _clientJarSpec;
    private String _serverJarSpec;

    public GWTLib(P project)
    {
        super(project);
    }

    public String clientJarPath()
    {
        if (_clientJarPath == null)
            _clientJarPath = new StringBuilder(project().layout().targetPath()).append(
                Artifact.toName(project(), "jar", "client")).toString();

        return _clientJarPath;
    }

    public String serverJarPath()
    {
        if (_serverJarPath == null)
            _serverJarPath = new StringBuilder(project().layout().targetPath()).append(
                Artifact.toName(project(), "jar", "server")).toString();

        return _serverJarPath;
    }

    public String clientJarSpec()
    {
        if (_clientJarSpec == null)
            _clientJarSpec = Artifact.toSpec(project(), "jar", "client");

        return _clientJarSpec;
    }

    public String serverJarSpec()
    {
        if (_serverJarSpec == null)
            _serverJarSpec = Artifact.toSpec(project(), "jar", "server");

        return _serverJarSpec;
    }

    @TaskInfo(desc = "Builds and packages the project in separate client and server jars.", deps = { "buildBlocks.j:compile" })
    public void jar()
    {
        P p = project();
        JavaLayout l = p.layout();

        String clientExcludes = ".*/server(/.*)?";

        ZipTask zipTask = new ZipTask(clientJarPath()).to("").from(l.targetMainPath()).exclude(clientExcludes).add();
        zipTask.from(l.mainJavaPath()).exclude(clientExcludes).add().createJar();

        String serverExcludes = ".*/client(/.*)?|.*/generators(/.*)?|.*/public(/.*)?|.*.gwt.xml";

        zipTask.reset(serverJarPath()).to("").from(l.targetMainPath()).exclude(serverExcludes).add().createJar();
    }

    @TaskInfo(desc = "Builds and publishes the project's client and server jars to the local repository.", deps = { "jar" })
    public void install()
    {
        Artifact.install(clientJarSpec(), clientJarPath());
        Artifact.install(serverJarSpec(), serverJarPath());
    }
}
