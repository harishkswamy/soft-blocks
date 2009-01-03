package buildBlocks.java;

import buildBlocks.Project;

/**
 * A convenience class preconfigured for a Java project.
 * 
 * @author hkrishna
 */
public class JavaProject extends Project<JavaLayout>
{
    protected JavaProject(String javaVersion)
    {
        super(new JavaLayout());

        modules(new JavaModule<JavaProject>(javaVersion, this));
    }
}
