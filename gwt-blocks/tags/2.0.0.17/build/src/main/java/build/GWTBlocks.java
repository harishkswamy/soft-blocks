package build;

import buildBlocks.ProjectInfo;
import buildBlocks.java.jee.gwt.GWTLayout;
import buildBlocks.java.jee.gwt.GWTLib;
import buildBlocks.java.jee.gwt.GWTProject;

/**
 * @author hkrishna
 */
@ProjectInfo(group = "com.google.code.soft-blocks", id = "gwt-blocks", version = "2.0.0")
public class GWTBlocks extends GWTProject<GWTLayout>
{
    public GWTBlocks()
    {
        super("1.5", "1.5.3", new GWTLayout());

        modules(new GWTLib<GWTBlocks>(this));

        projectDeps(new JEEBlocks("1.0.0.11"));
    }

    public GWTBlocks(String version)
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
