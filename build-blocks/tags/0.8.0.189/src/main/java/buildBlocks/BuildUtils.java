package buildBlocks;

import static buildBlocks.BuildCtx.*;

import jBlocks.server.Utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author hkrishna
 */
public class BuildUtils
{
    public static File download(String srcUrl, String targetPath, String sha1Chksum) throws Exception
    {
        File targetFile = new File(targetPath);

        targetFile.getAbsoluteFile().getParentFile().mkdirs();
        ByteArrayOutputStream buff = new ByteArrayOutputStream();

        System.out.println("Downloading " + srcUrl);

        URL url = new URL(srcUrl);
        InputStream in = new BufferedInputStream(url.openStream());
        long last = System.currentTimeMillis();
        int len = 0;

        while (true)
        {
            long now = System.currentTimeMillis();

            if (now > last + 100)
            {
                System.out.print(".");
                last = now;
            }

            int x = in.read();
            len++;

            if (x < 0)
                break;

            buff.write(x);
        }

        System.out.println("Done.");

        in.close();

        byte[] data = buff.toByteArray();
        String chkSum = Utils.getSHA1(data);

        if (sha1Chksum == null)
            System.out.println("SHA1 checksum: " + chkSum);

        else
        {
            sha1Chksum = sha1Chksum.split(" ")[0].trim();

            if (!chkSum.equals(sha1Chksum))
                throw new Exception(String.format("SHA1 checksum mismatch, expected: %s, downloaded: %s", sha1Chksum,
                    chkSum));
        }

        return BuildUtils.writeFile(targetFile, data);
    }

    public static URLClassLoader newClassLoader(String classpath)
    {
        try
        {
            String[] paths = classpath.split(File.pathSeparator);

            URL[] urls = new URL[paths.length];

            int i = 0;

            for (String path : paths)
                urls[i++] = new File(path).toURL();

            return new URLClassLoader(urls);
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    public static Class<?> loadClass(ClassLoader cl, String className)
    {
        try
        {
            return cl.loadClass(className);
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    /**
     * Loads and runs a static method.
     */
    public static Object run(String classpath, String className, String methodName, Class<?>[] methodParams,
        Object[] methodArgs)
    {
        URLClassLoader cl = newClassLoader(classpath);
        return run(cl, className, methodName, methodParams, methodArgs);
    }

    /**
     * Loads and runs a static method.
     */
    public static Object run(ClassLoader cl, String className, String methodName, Class<?>[] methodParams,
        Object[] methodArgs)
    {
        try
        {
            Class<?> classObj = cl.loadClass(className);
            Method method = classObj.getMethod(methodName, methodParams);
            return method.invoke(null, methodArgs);
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    /**
     * Loads and runs an instance method.
     */
    public static Object call(ClassLoader cl, String className, Class<?>[] ctorParamTypes, Object[] ctorArgs,
        String methodName, Class<?>[] mParamTypes, Object[] mArgs)
    {
        try
        {
            Class<?> clz = cl.loadClass(className);
            Object obj = clz.getConstructor(ctorParamTypes).newInstance(ctorArgs);
            return clz.getMethod(methodName, mParamTypes).invoke(obj, mArgs);
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    public static int java(String[] javaArgs, String classpath, String className, String[] classArgs)
    {
        if (javaArgs == null)
            javaArgs = new String[0];

        if (classArgs == null)
            classArgs = new String[0];

        String[] cArr = new String[javaArgs.length + classArgs.length + 4];

        cArr[0] = "java";

        System.arraycopy(javaArgs, 0, cArr, 1, javaArgs.length);

        int idx = javaArgs.length + 1;
        cArr[idx++] = "-cp";
        cArr[idx++] = classpath;
        cArr[idx++] = className;

        System.arraycopy(classArgs, 0, cArr, idx, classArgs.length);

        return BuildUtils.exec(cArr);
    }

    public static int exec(String... command)
    {
        StringBuilder b = new StringBuilder();

        for (String arg : command)
            b.append('"').append(arg).append("\" ");

        if (ctx().traceOn())
            System.out.println("Executing..." + b.toString());

        try
        {
            Process p = Runtime.getRuntime().exec(command);
            pipeStream(p.getInputStream(), System.out);
            pipeStream(p.getErrorStream(), System.out);
            p.waitFor();
            return p.exitValue();
        }
        catch (Exception e)
        {
            throw new Error("Unable to execute system command: " + b, e);
        }
    }

    private static void pipeStream(final InputStream in, final OutputStream out)
    {
        new Thread()
        {
            public void run()
            {
                try
                {
                    while (true)
                    {
                        int x = in.read();

                        if (x < 0)
                            return;

                        if (out != null)
                            out.write(x);
                    }
                }
                catch (Exception e)
                {
                    throw new Error("Unable to write system command output.", e);
                }
            }
        }.start();
    }

    public static byte[] readFile(File file)
    {
        RandomAccessFile raf = null;

        try
        {
            raf = new RandomAccessFile(file, "r");
            long len = raf.length();

            if (len >= Integer.MAX_VALUE)
                throw new Error("File " + file.getPath() + " is too large");

            byte[] buffer = new byte[(int) len];
            raf.readFully(buffer);

            return buffer;
        }
        catch (IOException e)
        {
            throw new Error("Unable to read from file " + file, e);
        }
        finally
        {
            try
            {
                if (raf != null)
                    raf.close();
            }
            catch (Exception e)
            {
                // Ignore
            }
        }
    }

    public static File writeFile(File file, byte[] data)
    {
        RandomAccessFile raf = null;

        try
        {
            raf = new RandomAccessFile(file, "rw");
            raf.write(data);
            raf.setLength(data.length);

            return file;
        }
        catch (IOException e)
        {
            throw new Error("Unable to write to file " + file, e);
        }
        finally
        {
            try
            {
                if (raf != null)
                    raf.close();
            }
            catch (Exception e)
            {
                // Ignore
            }
        }
    }

    private BuildUtils()
    {
    }
}
