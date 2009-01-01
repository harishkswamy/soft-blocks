package buildBlocks.java;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hkrishna
 */
public abstract class JavaCompiler
{
    private List<String> _options = new ArrayList<String>();

    public void options(String... options)
    {
        for (String option : options)
            _options.add(option);
    }

    protected List<String> options()
    {
        return _options;
    }

    public void compile(String sourcePath, String classPath, String targetPath)
    {
        if (!invokeCompiler(sourcePath, classPath, targetPath))
        {
            System.out.println();
            throw new Error("Java compiler error!");
        }
    }

    protected abstract boolean invokeCompiler(String sourcePath, String classPath, String targetPath);
}
