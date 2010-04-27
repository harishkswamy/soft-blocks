package build;

import buildBlocks.ProjectInfo;
import buildBlocks.java.JavaProject;

/**
 * @author hkrishna
 */
@ProjectInfo(group = "com.google.code.soft-blocks", version = "0.8.0")
public class BuildBlocks extends JavaProject
{
    public static void main(String[] args)
    {
        new BuildBlocks().execute("help", "jar", "pack");
    }

    public BuildBlocks()
    {
        super("1.5");
    }
}
