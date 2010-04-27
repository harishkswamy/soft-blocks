package buildBlocks.java;

import java.io.File;

import org.eclipse.jdt.internal.compiler.batch.Main;

/**
 * @author hkrishna
 */
public class EclipseCompiler extends JavaCompiler
{
    @SuppressWarnings("deprecation")
    @Override
    protected boolean invokeCompiler(String sourcePath, String classPath, String targetPath)
    {
        StringBuilder b = new StringBuilder();

        for (String option : options())
            b.append(option).append(' ');

        b.append("-d ").append(targetPath).append(" -classpath \".").append(File.pathSeparatorChar).append(classPath);
        b.append("\" ").append(sourcePath);

        return Main.compile(b.toString());
    }
}
