package buildBlocks.java;

import java.io.File;

import buildBlocks.Artifact;
import buildBlocks.FileTask;
import buildBlocks.JarTask;
import buildBlocks.Module;
import buildBlocks.ModuleInfo;
import buildBlocks.Project;
import buildBlocks.TaskInfo;

/**
 * @author hkrishna
 */
@ModuleInfo(group = "buildBlocks", id = "j")
public class JavaModule<P extends Project<? extends JavaLayout>> extends Module<P>
{
    private JavaCompiler     _compiler     = new EclipseCompiler();
    private JavaTester       _tester       = new JUnitTester("4.5", this);
    private JavadocGenerator _docGenerator = new JavadocGenerator(this);

    private String           _mainClasspath;
    private String           _testClasspath;
    private String           _runtimeClasspath;

    private String           _jarPath;

    public JavaModule(String javaVersion, P project)
    {
        super(project);

        _compiler.options("-source", javaVersion);
    }

    public JavaModule<P> compiler(JavaCompiler compiler)
    {
        _compiler = compiler;

        return this;
    }

    public JavaCompiler compiler()
    {
        return _compiler;
    }

    public JavaModule<P> tester(JavaTester tester)
    {
        _tester = tester;

        return this;
    }

    public JavaTester tester()
    {
        return _tester;
    }

    public JavaModule<P> docGenerator(JavadocGenerator docGenerator)
    {
        _docGenerator = docGenerator;

        return this;
    }

    public JavadocGenerator docGenerator()
    {
        return _docGenerator;
    }

    /**
     * @return The class path string to be used to compile the main files.
     */
    public String mainClasspath()
    {
        if (_mainClasspath != null)
            return _mainClasspath;

        StringBuilder b = new StringBuilder();

        for (Artifact artifact : project().deps())
            b.append(artifact.getPath()).append(File.pathSeparatorChar);

        for (Artifact artifact : project().compileDeps())
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

        for (Artifact artifact : project().testDeps())
            b.append(artifact.getPath()).append(File.pathSeparatorChar);

        for (Artifact artifact : project().runtimeDeps())
            b.append(artifact.getPath()).append(File.pathSeparatorChar);

        JavaLayout l = project().layout();
        b.append(l.targetMainBinPath()).append(File.pathSeparatorChar).append(l.targetTestBinPath());

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

        for (Artifact artifact : project().deps())
            b.append(artifact.getPath()).append(File.pathSeparatorChar);

        for (Artifact artifact : project().runtimeDeps())
            b.append(artifact.getPath()).append(File.pathSeparatorChar);

        return _runtimeClasspath = b.toString();
    }

    public String jarPath()
    {
        if (_jarPath == null)
            _jarPath = new StringBuilder(project().layout().targetPath()).append(
                Artifact.toName(project(), "jar", project().classifier())).toString();

        return _jarPath;
    }

    // Tasks ================================================================================

    @TaskInfo(desc = "Cleans the project's main and test target spaces.")
    public void clean()
    {
        JavaLayout l = project().layout();
        new FileTask(l.targetMainBinPath()).delete().reset(l.targetTestBinPath()).delete();
    }

    @TaskInfo(desc = "Compiles the project in the target space.")
    public void compile()
    {
        P p = project();
        JavaLayout l = p.layout();

        compiler().compile(l.mainJavaPath(), mainClasspath(), l.targetMainBinPath());

        new FileTask(l.mainJavaPath()).exclude(".*\\.java").copyToDir(l.targetMainBinPath(), false).reset(
            l.mainResourcePath()).copyToDir(l.targetMainBinPath(), false);
    }

    @TaskInfo(desc = "Builds and runs the test suite for the project.", deps = { "compile" })
    public void test()
    {
        P p = project();
        JavaLayout l = p.layout();

        System.out.println("Compiling test source...");

        compiler().compile(l.testJavaPath(), testClasspath(), l.targetTestBinPath());

        new FileTask(l.testJavaPath()).exclude(".*\\.java").copyToDir(l.targetTestBinPath(), false).reset(
            l.testResourcePath()).copyToDir(l.targetTestBinPath(), false);

        System.out.println("Running tests...");

        tester().test();
    }

    @TaskInfo(desc = "Builds and packages the project in a jar.", deps = { "compile" })
    public void jar()
    {
        P p = project();
        JavaLayout l = p.layout();

        new JarTask(jarPath(), p.buildId()).from(l.targetMainBinPath()).exclude(null).add().create();
    }

    @TaskInfo(desc = "Builds and publishes the project's jar to the local repository.", deps = { "jar" })
    public void install()
    {
        P p = project();
        Artifact.install(Artifact.toSpec(p, "jar", p.classifier()), jarPath());
    }

    @TaskInfo(desc = "Generates Javadoc for the project's main source.")
    public void doc()
    {
        docGenerator().generate();
    }
}
