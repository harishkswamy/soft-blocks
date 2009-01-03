package buildBlocks;

import buildBlocks.java.JavaLayout;

/**
 * @author hkrishna
 */
public class BuildBlocks extends Project<JavaLayout>
{
    public static void main(String[] args)
    {
        new BuildBlocks().execute("help", "jar", "pack");
    }
    
    private BuildBlocks()
    {
        super(new JavaLayout());
    }

    @TaskInfo(desc = "Initializes the project.")
    public void init()
    {

    }

    @TaskInfo(desc = "Packs project into a zip file.", deps = { "jar" })
    public void pack()
    {

    }
    
    public void dummy()
    {
        
    }
}
