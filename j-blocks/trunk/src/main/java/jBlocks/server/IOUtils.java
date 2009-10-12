// Copyright 2007 Harish Krishnaswamy
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package jBlocks.server;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

/**
 * @author hkrishna
 */
public class IOUtils
{
    public interface StreamReader
    {
        void read(InputStream inputStream) throws Exception;
    }

    public interface StreamWriter
    {
        void write(FileOutputStream outputStream) throws Exception;
    }

    public interface CharReader
    {
        void read(BufferedReader reader) throws Exception;
    }

    public interface CharWriter
    {
        void write(BufferedWriter writer) throws Exception;
    }

    public interface LineHandler
    {
        void handleLine(String line) throws Exception;

        void endOfFile();
    }

    public interface CsvHandler
    {
        void handleLine(List<String> tokens) throws Exception;

        void endOfFile();
    }

    public interface ScsvHandler
    {
        void handleLine(List<String> tokens) throws Exception;

        void endOfFile();
    }

    public interface SyamlHandler
    {
        void handleMapping(String key, String value, int level, int line);

        void endOfFile();
    }

    public interface AttributesHandler
    {
        void handleAttribute(String key, String value);

        void endOfFile();
    }

    private IOUtils()
    {
        // Static class
    }

    public static URL toURL(File file)
    {
        try
        {
            return file.toURI().toURL();
        }
        catch (MalformedURLException e)
        {
            throw AggregateException.with(e, "Unable to create URL for file: " + file);
        }
    }

    public static URL toURL(String urlStr)
    {
        try
        {
            return new URL(urlStr);
        }
        catch (MalformedURLException e)
        {
            throw AggregateException.with(e, "Unable to create URL from path: " + urlStr);
        }
    }

    public static void writeFile(File file, StreamWriter streamWriter)
    {
        try
        {
            FileOutputStream stream = new FileOutputStream(file);

            try
            {
                streamWriter.write(stream);
            }
            finally
            {
                if (stream != null)
                {
                    stream.flush();
                    stream.close();
                }
            }
        }
        catch (Exception e)
        {
            throw AggregateException.with(e);
        }
    }

    public static void readURL(URL url, StreamReader streamReader)
    {
        try
        {
            InputStream inputStream = url.openStream();

            try
            {
                streamReader.read(inputStream);
            }
            finally
            {
                if (inputStream != null)
                    inputStream.close();
            }
        }
        catch (Exception e)
        {
            throw AggregateException.with(e);
        }
    }

    /**
     * Creates a UTF-8 buffered writer and passes it to provided {@link CharWriter}.
     */
    public static void writeCharFile(File file, CharWriter charWriter)
    {
        try
        {
            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));

            try
            {
                charWriter.write(bufWriter);
            }
            finally
            {
                if (bufWriter != null)
                {
                    bufWriter.flush();
                    bufWriter.close();
                }
            }
        }
        catch (Exception e)
        {
            throw AggregateException.with(e);
        }
    }

    /**
     * Creates a UTF-8 buffered reader and passes it to provided {@link CharReader}.
     */
    public static void readCharURL(URL url, CharReader charReader)
    {
        try
        {
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));

            try
            {
                charReader.read(bufReader);
            }
            finally
            {
                if (bufReader != null)
                    bufReader.close();
            }
        }
        catch (Exception e)
        {
            throw AggregateException.with(e);
        }
    }

    /**
     * Reads characters encoded in UTF-8 format and passes each line of text to the provided {@link LineHandler}.
     */
    public static void readCharURL(URL url, final LineHandler handler)
    {
        readCharURL(url, new CharReader()
        {
            public void read(BufferedReader reader) throws Exception
            {
                String line = null;

                while ((line = reader.readLine()) != null)
                    handler.handleLine(line);

                handler.endOfFile();
            }
        });
    }

    /**
     * Reads comma separated values encoded in UTF-8 format and passes the list of values in each line to the provided
     * {@link CsvHandler}.
     */
    public static void readCsvURL(URL url, final CsvHandler handler)
    {
        readCharURL(url, new LineHandler()
        {
            public void handleLine(String line) throws Exception
            {
                handler.handleLine(Utils.splitQuoted(line, ','));
            }

            public void endOfFile()
            {
                handler.endOfFile();
            }
        });
    }

    /**
     * Reads semi-colon separated values encoded in UTF-8 format and passes the list of values in each line to the
     * provided {@link ScsvHandler}.
     */
    public static void readScsvURL(URL url, final ScsvHandler handler)
    {
        readCharURL(url, new LineHandler()
        {
            public void handleLine(String line) throws Exception
            {
                handler.handleLine(Utils.splitQuoted(line, ';'));
            }

            public void endOfFile()
            {
                handler.endOfFile();
            }
        });
    }

    public static void readSyaml(URL url, final SyamlHandler handler)
    {
        readCharURL(url, new LineHandler()
        {
            private int     _line, _col, _level, _spaces;
            private Pattern _pattern = Pattern.compile(":\\s|:$");

            public void handleLine(String line) throws Exception
            {
                _line++;

                String tLine = line.trim();

                if (tLine.length() == 0 || tLine.startsWith("#"))
                    return;

                String[] entry = _pattern.split(tLine, 2);

                String value;
                value = entry.length == 1 || (value = entry[1].trim()).length() == 0 ? null : value;

                handler.handleMapping(entry[0], value, level(line), _line);
            }

            private int level(String line)
            {
                int col = 0, pCol = _col;

                while (line.charAt(col) == ' ')
                    col++;

                _col = col;

                if (_spaces == 0)
                {
                    if (col == 0)
                        return 0;

                    _spaces = col;
                }

                int dLevel = (col - pCol) / _spaces;

                return _level += dLevel;
            }

            public void endOfFile()
            {
                handler.endOfFile();
            }
        });
    }

    /**
     * Reads an attributes file encoded in UTF-8 format and passes the key, value pairs to the provided
     * {@link AttributesHandler} in the order it is defined.
     * <p>
     * Attributes must be defined as follows.
     * <ul>
     * <li>The attributes file is a line-based file where each line defines an atttribute. Multi-line attribute
     * definitions must end each line, except the last line, with a backslash '\' character
     * <li>The attribute key and value must be separated by an '=' character
     * <li>Keys and values that contain the '=' character must be enclosed in double-quotes '"'
     * <li>Lines starting with a '#' character are ignored
     * </ul>
     */
    public static void readAttributesURL(URL url, final AttributesHandler handler)
    {
        readCharURL(url, new LineHandler()
        {
            private StringBuilder _line = new StringBuilder();

            public void handleLine(String line) throws Exception
            {
                line = line.trim();

                if (line.startsWith("#") || line.length() == 0)
                    return;

                if (line.endsWith("\\"))
                    _line.append(line.subSequence(0, line.length() - 1));

                else
                {
                    _line.append(line);

                    List<String> attr = Utils.splitQuoted(_line.toString(), '=');

                    handler.handleAttribute(attr.get(0), attr.get(1));

                    _line.delete(0, _line.length());
                }
            }

            public void endOfFile()
            {
                handler.endOfFile();
            }
        });
    }

    public static boolean copyFileIO(final File fromFile, File toFile)
    {
        try
        {
            writeFile(toFile, new StreamWriter()
            {
                public void write(final FileOutputStream out) throws Exception
                {
                    readURL(toURL(fromFile), new StreamReader()
                    {
                        public void read(InputStream in) throws Exception
                        {
                            // Read 64K chunks
                            byte[] buf = new byte[65536];
                            int len;

                            while ((len = in.read(buf)) > 0)
                                out.write(buf, 0, len);
                        }
                    });
                }
            });

            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public static boolean copyFile(File srcFile, File destFile, boolean overwrite)
    {
        try
        {
            if (srcFile.isDirectory())
                throw new IllegalArgumentException("Source path " + srcFile
                    + " must represent a file and not a directory.");

            if (destFile.exists())
            {
                if (destFile.isDirectory())
                    destFile = new File(destFile.getPath(), srcFile.getName());
            }
            else
            {
                File parent = destFile.getParentFile();

                if (parent != null && !parent.exists())
                {
                    if (!parent.mkdirs())
                        throw new Error("Failed to create parent directories.");
                }
            }

            if (!destFile.createNewFile() && !overwrite)
                return false;

            FileChannel src = null, dest = null;

            try
            {
                src = new FileInputStream(srcFile).getChannel();
                dest = new FileOutputStream(destFile).getChannel();

                long count = 0, size = src.size();

                while (count < size)
                    count += dest.transferFrom(src, count, size - count);

                return true;
            }
            finally
            {
                if (src != null)
                    src.close();

                if (dest != null)
                    dest.close();
            }
        }
        catch (Exception e)
        {
            throw AggregateException.with(e, "Unable to copy file " + srcFile + " to " + destFile);
        }
    }

    /**
     * This method copies the source file to the destination file and then deletes the source file. Unlike the
     * {@link File#renameTo(File)}, this method is guaranteed to work irrespective of the underlying file systems.
     * 
     * @return true is the move was successful, false otherwise.
     */
    public static boolean moveFile(File srcFile, File destFile, boolean overwrite)
    {
        if (copyFile(srcFile, destFile, overwrite))
            return srcFile.delete();

        return false;
    }

    /**
     * Creates, loads and returns the properties from the URL provided.
     * 
     * @throws WrapperException
     *             When unable to load the properties.
     */
    public static Properties loadProperties(URL url, Properties defaults)
    {
        try
        {
            Properties props = new Properties(defaults);

            props.load(url.openStream());

            return props;
        }
        catch (Exception e)
        {
            throw AggregateException.with(e, "Unable to load properties from " + url);
        }
    }

    public static Properties loadProperties(URL url)
    {
        return loadProperties(url, null);
    }

    public static String askPassword(String message)
    {
        JLabel wLabel = new JLabel(message);
        final JPasswordField wPwd = new JPasswordField();

        JOptionPane jop = new JOptionPane(new Object[] { wLabel, wPwd }, JOptionPane.QUESTION_MESSAGE,
            JOptionPane.OK_CANCEL_OPTION);

        JDialog dialog = jop.createDialog(null, "Password");

        dialog.addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentShown(ComponentEvent e)
            {
                wPwd.requestFocusInWindow();
            }
        });

        dialog.setVisible(true);
        dialog.dispose();

        int result = (Integer) jop.getValue();

        char[] pwd = null;

        if (result == JOptionPane.OK_OPTION)
            pwd = wPwd.getPassword();

        if (pwd == null)
            return null;

        String password = new String(pwd);

        for (int i = 0; i < pwd.length; i++)
            pwd[i] = 0;

        return password;
    }

    public static void createOrEnsureFullControl(String dirPath)
    {
        File dir = new File(dirPath);

        dir.mkdir();

        if (!dir.exists() || !dir.canRead() || !dir.canWrite())
            throw new ApplicationException("Insufficient privileges to access dir path: " + dir.getAbsolutePath());
    }
}
