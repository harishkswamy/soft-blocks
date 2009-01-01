package build;

import buildBlocks.ProjectInfo;
import buildBlocks.java.jee.gwt.GWTLayout;
import buildBlocks.java.jee.gwt.GWTLib;
import buildBlocks.java.jee.gwt.GWTProject;

/**
 * @author hkrishna
 */
@ProjectInfo(group = "com.google.code.soft-blocks", id = "jee-blocks", version = "1.0.0")
public class JEEBlocks extends GWTProject<GWTLayout>
{
    public JEEBlocks()
    {
        super("1.5", new GWTLayout());

        modules(new GWTLib<JEEBlocks>(this));

        projectDeps(new JBlocks("1.0.0.7"));

        buildDeps("javax.servlet:servlet-api:jar:2.5");
    }

    public JEEBlocks(String version)
    {
        this();
        version(version);
    }

    public String[] buildArtifacts()
    {
        return new String[] { clientJarSpec(), serverJarSpec() };
    }

    public String[] runtimeArtifacts()
    {
        return new String[] { serverJarSpec() };
    }
}
