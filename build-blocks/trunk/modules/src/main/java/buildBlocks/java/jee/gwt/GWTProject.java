package buildBlocks.java.jee.gwt;

import buildBlocks.Project;
import buildBlocks.java.JavaModule;
import buildBlocks.java.jee.JEEModule;

/**
 * @author hkrishna
 */
public class GWTProject extends Project<GWTLayout>
{
    protected GWTProject(String javaVersion, String gwtVersion)
    {
        this(javaVersion, gwtVersion, null);
    }

    protected GWTProject(String javaVersion, String gwtVersion, String moduleName)
    {
        super(new GWTLayout());

        modules(new JavaModule<GWTProject>(javaVersion, this), new JEEModule<GWTProject>(this), new GWTLib<GWTProject>(
            this), new GWTApp<GWTProject>(gwtVersion, moduleName, this));
    }
}
