package buildBlocks.java.jee;

import buildBlocks.Project;
import buildBlocks.java.JavaModule;

/**
 * @author hkrishna
 */
public class JEEProject extends Project<JEELayout>
{
    protected JEEProject(String javaVersion)
    {
        super(new JEELayout());

        modules(new JavaModule<JEEProject>(javaVersion, this), new JEEModule<JEEProject>(this));
    }
}
