package buildBlocks.java;

import static buildBlocks.Context.*;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import buildBlocks.FileTask;
import buildBlocks.Utils;

/**
 * @author hkrishna
 */
public class JUnitTester extends JavaTester
{
    public JUnitTester(JavaModule<?> javaModule)
    {
        super(javaModule);

        javaModule.project().testDeps("junit:junit:jar:4.5");
    }

    public boolean run()
    {
        return Utils.java(null, javaModule().testClasspath(), "org.junit.runner.JUnitCore", getTestClassNames()) == 0;
    }

    @SuppressWarnings("unchecked")
    private String[] getTestClassNames()
    {
        JavaLayout l = javaModule().project().layout();

        String testBinPath = l.testBinPath();
        List<File> classFiles = new FileTask(testBinPath).select(".*\\.class").getFiles(false);

        URLClassLoader cl = Utils.newClassLoader(javaModule().testClasspath());
        Class<? extends Annotation> testAnno = (Class<? extends Annotation>) Utils.loadClass(cl, "org.junit.Test");
        List<String> classNames = new ArrayList<String>();

        boolean printClPath = true;

        for (File classFile : classFiles)
        {
            String path = classFile.getPath();
            String className = path.substring(testBinPath.length(), path.length() - 6).replace(File.separatorChar, '.');
            className = className.startsWith(".") ? className.substring(1) : className;

            try
            {
                Class<?> testClass = cl.loadClass(className);

                if (isTestClass(testClass, testAnno))
                    classNames.add(className);
            }
            catch (ClassNotFoundException e)
            {
                if (ctx().traceOn())
                    printClPath = printTestTrace(e, cl, printClPath);
            }
        }

        return classNames.toArray(new String[classNames.size()]);
    }

    private boolean isTestClass(Class<?> testClass, Class<? extends Annotation> testAnno)
    {
        for (Method m : testClass.getMethods())
        {
            if (m.getAnnotation(testAnno) != null)
                return true;
        }

        return false;
    }

    private boolean printTestTrace(ClassNotFoundException e, URLClassLoader cl, boolean printClPath)
    {
        String fmt = "Skipping probable test class, 'cause %s";
        System.out.println(String.format(fmt, e));

        if (printClPath)
        {
            StringBuilder b = new StringBuilder();

            for (URL url : cl.getURLs())
                b.append(url).append(';');

            System.out.println("Test class loader path: " + b);
        }

        return false;
    }
}
