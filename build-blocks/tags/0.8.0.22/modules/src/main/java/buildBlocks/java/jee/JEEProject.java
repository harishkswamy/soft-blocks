package buildBlocks.java.jee;

import buildBlocks.java.JavaProject;

/**
 * @author hkrishna
 */
public class JEEProject<L extends JEELayout> extends JavaProject<L>
{
    protected JEEProject(String javaVersion, L layout)
    {
        super(javaVersion, layout);
    }
}
