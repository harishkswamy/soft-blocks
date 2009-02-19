package buildBlocks.java;

import java.io.File;
import java.util.List;

import buildBlocks.BuildCtx;
import buildBlocks.FileTask;

import com.sun.tools.javac.Main;

/**
 * @author hkrishna
 */
public class SunCompiler extends JavaCompiler
{
    @Override
    protected boolean invokeCompiler(String sourcePath, String classPath, String targetPath)
    {
        new File(targetPath).mkdirs();

        List<File> srcDirs = new FileTask(sourcePath).getFiles(false);

        List<String> options = options();
        int idx = options.size();

        String[] args = options.toArray(new String[idx + srcDirs.size() + 4]);

        args[idx++] = "-classpath";
        args[idx++] = "\"." + File.pathSeparator + classPath + '"';
        args[idx++] = "-d";
        args[idx++] = targetPath;

        for (File dir : srcDirs)
            args[idx++] = dir.getPath();

        if (BuildCtx.ctx().traceOn())
        {
            StringBuilder b = new StringBuilder();

            for (String arg : args)
                b.append(arg).append(' ');

            System.out.println(b.toString());
        }

        return Main.compile(args) == 0;
    }
}
