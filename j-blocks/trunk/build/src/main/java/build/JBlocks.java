package build;

import buildBlocks.ProjectInfo;
import buildBlocks.TaskInfo;
import buildBlocks.java.JavaModule;
import buildBlocks.java.JavaProject;
import buildBlocks.java.jee.gwt.GWTLib;

/**
 * @author hkrishna
 */
@ProjectInfo(group = "com.google.code.soft-blocks", id = "j-blocks", version = "1.0.0")
public class JBlocks extends JavaProject
{
    public JBlocks()
    {
        super("1.5");

        modules(new GWTLib<JBlocks>(this));

        deps("org.slf4j:slf4j-api:jar:1.5.0");

        testDeps("cglib:cglib-nodep:jar:2.1_3", "org.objenesis:objenesis:jar:1.0", "org.jmock:jmock:jar:2.4.0",
            "org.jmock:jmock-legacy:jar:2.4.0", "org.hamcrest:hamcrest-core:jar:1.1",
            "org.hamcrest:hamcrest-library:jar:1.1");

        runtimeDeps("ch.qos.logback:logback-core:jar:0.9.9", "ch.qos.logback:logback-classic:jar:0.9.9");
    }

    public JBlocks(String version)
    {
        this();
        version(version);
    }

    @Override
    public String[] artifacts()
    {
        GWTLib<?> gwtLib = module(GWTLib.class);
        return new String[] { gwtLib.serverJarSpec() };
    }

    @Override
    public String[] compileArtifacts()
    {
        GWTLib<?> gwtLib = module(GWTLib.class);
        return new String[] { gwtLib.clientJarSpec() };
    }
}
