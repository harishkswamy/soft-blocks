package buildBlocks.java;

import java.io.File;

import buildBlocks.Project;
import buildBlocks.Artifact;

/**
 * @author hkrishna
 */
public class JavaProject<L extends JavaLayout> extends Project<L>
{
    private JavaCompiler _compiler = new EclipseCompiler();

    private String       _mainClasspath;
    private String       _testClasspath;
    private String       _runtimeClasspath;

    private String       _jarPath;

    protected JavaProject(String javaVersion, L layout)
    {
        super(layout);

        _compiler.options("-source", javaVersion);
    }

    public void compiler(JavaCompiler compiler)
    {
        _compiler = compiler;
    }

    public JavaCompiler compiler()
    {
        return _compiler;
    }

    /**
     * @return The class path string to be used to compile the main files.
     */
    public String mainClasspath()
    {
        if (_mainClasspath != null)
            return _mainClasspath;

        StringBuilder b = new StringBuilder();

        for (Artifact artifact : buildDeps())
            b.append(artifact.getPath()).append(File.pathSeparatorChar);

        return _mainClasspath = b.toString();
    }

    /**
     * @return The class path string to be used to compile and run the test suite.
     */
    public String testClasspath()
    {
        if (_testClasspath != null)
            return _testClasspath;

        StringBuilder b = new StringBuilder(mainClasspath());

        for (Artifact artifact : testDeps())
            b.append(artifact.getPath()).append(File.pathSeparatorChar);

        b.append(layout().targetMainPath());

        return _testClasspath = b.toString();
    }

    /**
     * @return The class path string to be used to run this project.
     */
    public String runtimeClasspath()
    {
        if (_runtimeClasspath != null)
            return _runtimeClasspath;

        StringBuilder b = new StringBuilder();

        for (Artifact artifact : runtimeDeps())
            b.append(artifact.getPath()).append(File.pathSeparatorChar);

        return _runtimeClasspath = b.toString();
    }

    public String jarPath()
    {
        if (_jarPath == null)
            _jarPath = new StringBuilder(layout().targetPath()).append(Artifact.toName(this, "jar", classifier()))
                .toString();

        return _jarPath;
    }
}
