package build;

import buildBlocks.ProjectInfo;
import buildBlocks.java.jee.gwt.GWTLayout;
import buildBlocks.java.jee.gwt.GWTLib;
import buildBlocks.java.jee.gwt.GWTProject;

/**
 * @author hkrishna
 */
@ProjectInfo(group = "com.google.code.soft-blocks", id = "j-blocks", version = "1.0.0")
public class JBlocks extends GWTProject<GWTLayout>
{
    public JBlocks()
    {
        super("1.5", new GWTLayout());

        modules(new GWTLib<JBlocks>(this));

        buildDeps("org.slf4j:slf4j-api:jar:1.5.0");

        testDeps("junit:junit:jar:4.5", "cglib:cglib-nodep:jar:2.1_3", "org.objenesis:objenesis:jar:1.0",
            "org.jmock:jmock:jar:2.4.0", "org.jmock:jmock-legacy:jar:2.4.0", "org.hamcrest:hamcrest-core:jar:1.1",
            "org.hamcrest:hamcrest-library:jar:1.1");

        runtimeDeps("org.slf4j:slf4j-api:jar:1.5.0", "ch.qos.logback:logback-core:jar:0.9.9",
            "ch.qos.logback:logback-classic:jar:0.9.9");
    }
    
    public JBlocks(String version)
    {
        this();
        version(version);
    }

    @Override
    public String[] buildArtifacts()
    {
        return new String[] { clientJarSpec(), serverJarSpec() };
    }

    @Override
    public String[] runtimeArtifacts()
    {
        return new String[] { serverJarSpec() };
    }
}
