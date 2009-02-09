package build;

import buildBlocks.ProjectInfo;
import buildBlocks.java.JavaProject;
import buildBlocks.java.jee.gwt.GWTLib;

/**
 * @author hkrishna
 */
@ProjectInfo(group = "com.google.code.soft-blocks", id = "jee-blocks", version = "1.0.0")
public class JEEBlocks extends JavaProject
{
    public JEEBlocks()
    {
        super("1.5");

        modules(new GWTLib<JEEBlocks>(this));

        projectDeps(new JBlocks("1.0.0"));

        compileDeps("javax.servlet:servlet-api:jar:2.5");
    }

    public JEEBlocks(String version)
    {
        this();
        version(version);
    }

    @Override
    public String[] artifacts()
    {
        return new String[] { module(GWTLib.class).serverJarSpec() };
    }

    @Override
    public String[] compileArtifacts()
    {
        return new String[] { module(GWTLib.class).clientJarSpec() };
    }
}
