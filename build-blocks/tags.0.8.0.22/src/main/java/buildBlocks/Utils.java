package buildBlocks;

import static buildBlocks.Context.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

/**
 * @author hkrishna
 */
public class Utils
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
        String chkSum = getSHA1(data);

        if (sha1Chksum == null)
            System.out.println("SHA1 checksum: " + chkSum);

        else
        {
            if (!chkSum.equals(sha1Chksum))
                throw new Error(String.format("SHA1 checksum mismatch, expected: %s, downloaded: %s", sha1Chksum,
                    chkSum));
        }

        return Utils.writeFile(targetFile, data);
    }

    private static String getSHA1(byte[] data)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            return convertBytesToString(md.digest(data));
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new Error(e);
        }
    }

    private static String convertBytesToString(byte[] value)
    {
        StringBuilder b = new StringBuilder(value.length * 2);

        for (int i = 0; i < value.length; i++)
        {
            int c = value[i] & 0xff;
            b.append(Integer.toString(c >> 4, 16));
            b.append(Integer.toString(c & 0xf, 16));
        }

        return b.toString();
    }

    public static Properties loadProperties(File file)
    {
        FileInputStream inputStream = null;

        try
        {
            inputStream = new FileInputStream(file);

            Properties props = new Properties();
            props.load(inputStream);

            return props;
        }
        catch (IOException e)
        {
            throw new Error("Unable to load properties from file: " + file, e);
        }
        finally
        {
            try
            {
                if (inputStream != null)
                    inputStream.close();
            }
            catch (IOException e)
            {
            }
        }
    }

    public static String trim(String str, String trim)
    {
        int start = 0;

        while (str.startsWith(trim, start))
            start += trim.length();

        str = str.substring(start);

        while (str.endsWith(trim))
            str = str.substring(0, str.length() - trim.length());

        return str;
    }

    public static void java(String[] javaArgs, String classpath, String className, String[] classArgs)
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

        Utils.exec(cArr);
    }

    public static void exec(String... command)
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
        try
        {
            RandomAccessFile ra = new RandomAccessFile(file, "r");
            long len = ra.length();

            if (len >= Integer.MAX_VALUE)
                throw new Error("File " + file.getPath() + " is too large");

            byte[] buffer = new byte[(int) len];
            ra.readFully(buffer);
            ra.close();

            return buffer;
        }
        catch (IOException e)
        {
            throw new Error("Unable to read from file " + file, e);
        }
    }

    public static File writeFile(File file, byte[] data)
    {
        try
        {
            RandomAccessFile ra = new RandomAccessFile(file, "rw");
            ra.write(data);
            ra.setLength(data.length);
            ra.close();

            return file;
        }
        catch (IOException e)
        {
            throw new Error("Unable to write to file " + file, e);
        }
    }

    private Utils()
    {
    }
}
