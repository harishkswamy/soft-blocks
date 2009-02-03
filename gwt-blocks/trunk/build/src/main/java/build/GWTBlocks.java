package build;

import buildBlocks.ProjectInfo;
import buildBlocks.java.jee.gwt.GWTLib;
import buildBlocks.java.jee.gwt.GWTProject;

/**
 * @author hkrishna
 */
@ProjectInfo(group = "com.google.code.soft-blocks", id = "gwt-blocks", version = "2.0.0")
public class GWTBlocks extends GWTProject
{
    public GWTBlocks()
    {
        super("1.5", "1.5.3");

        projectDeps(new JEEBlocks("1.0.0.110"));
    }

    public GWTBlocks(String version)
    {
        this();
        version(version);
    }

    @Override
    public String[] artifacts()
    {
        return new String[] {  module(GWTLib.class).serverJarSpec() };
    }

    @Override
    public String[] compileArtifacts()
    {
        return new String[] { module(GWTLib.class).clientJarSpec() };
    }
}
