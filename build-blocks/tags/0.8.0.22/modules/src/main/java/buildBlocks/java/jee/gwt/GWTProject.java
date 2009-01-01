package buildBlocks.java.jee.gwt;

import java.io.File;

import buildBlocks.Artifact;
import buildBlocks.java.jee.JEEProject;

/**
 * @author hkrishna
 */
public class GWTProject<L extends GWTLayout> extends JEEProject<L>
{
    private String _gwtVersion;

    private String _clientJarPath;
    private String _serverJarPath;
    private String _clientJarSpec;
    private String _serverJarSpec;

    private String _moduleName;

    protected GWTProject(String javaVersion, String gwtVersion, String moduleName, L layout)
    {
        this(javaVersion, gwtVersion, layout);

        _moduleName = moduleName;
    }

    protected GWTProject(String javaVersion, String gwtVersion, L layout)
    {
        this(javaVersion, layout);

        _gwtVersion = gwtVersion;

        init();
    }

    protected GWTProject(String javaVersion, L layout)
    {
        super(javaVersion, layout);
    }

    private void init()
    {
        StringBuilder b = new StringBuilder();

        String userJar = b.append("com.google.gwt:gwt-user:jar:").append(_gwtVersion).toString();
        b.delete(0, b.length());
        String windowsJar = b.append("com.google.gwt:gwt-dev:jar:windows:").append(_gwtVersion).toString();
        b.delete(0, b.length());
        String servletJar = b.append("com.google.gwt:gwt-servlet:jar:").append(_gwtVersion).toString();

        buildDeps(userJar, windowsJar, servletJar);
        runtimeDeps(servletJar);
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
        GWTLayout l = layout();

        return new String[] { "-out", l.targetGwtOutPath(), "-gen", l.targetGwtGenPath(), _moduleName };
    }

    public String gwtClasspath()
    {
        GWTLayout l = layout();

        return new StringBuilder(l.mainJavaPath()).append(File.pathSeparator).append(l.targetMainPath()).append(
            File.pathSeparator).append(mainClasspath()).toString();
    }

    public String clientJarPath()
    {
        if (_clientJarPath == null)
            _clientJarPath = new StringBuilder(layout().targetPath()).append(Artifact.toName(this, "jar", "client"))
                .toString();

        return _clientJarPath;
    }

    public String serverJarPath()
    {
        if (_serverJarPath == null)
            _serverJarPath = new StringBuilder(layout().targetPath()).append(Artifact.toName(this, "jar", "server"))
                .toString();

        return _serverJarPath;
    }

    public String clientJarSpec()
    {
        if (_clientJarSpec == null)
            _clientJarSpec = Artifact.toSpec(this, "jar", "client");

        return _clientJarSpec;
    }

    public String serverJarSpec()
    {
        if (_serverJarSpec == null)
            _serverJarSpec = Artifact.toSpec(this, "jar", "server");

        return _serverJarSpec;
    }
}
