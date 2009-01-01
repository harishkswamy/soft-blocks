package build;

import buildBlocks.ProjectInfo;
import buildBlocks.java.JavaLayout;
import buildBlocks.java.JavaModule;
import buildBlocks.java.JavaProject;

/**
 * @author hkrishna
 */
@ProjectInfo(group = "com.google.code.soft-blocks", version = "0.8.0")
public class BuildBlocks extends JavaProject<JavaLayout>
{
    public static void main(String[] args)
    {
        new BuildBlocks().build("help", "jar", "pack");
    }

    public BuildBlocks()
    {
        super("1.5", new JavaLayout());

        modules(new JavaModule<BuildBlocks>(this));
    }
}
