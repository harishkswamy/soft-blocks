package buildBlocks;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipOutputStream;

/**
 * @author hkrishna
 */
abstract class AbstractZipTask<T extends AbstractZipTask<?>>
{
    private String                               _zipPath;
    private Map<String, Map<String, List<File>>> _files    = new HashMap<String, Map<String, List<File>>>();

    private String                               _toPath   = "";
    private String                               _fromPath = "";
    private FileTask                             _fileTask;

    private StringBuilder                        _buf      = new StringBuilder();

    protected AbstractZipTask(String path)
    {
        _zipPath = path;
    }

    @SuppressWarnings("unchecked")
    public T reset(String path)
    {
        _zipPath = path;
        _files = new HashMap<String, Map<String, List<File>>>();
        _toPath = _fromPath = "";

        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T to(String path)
    {
        _toPath = new File(path).getPath();

        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T from(String path)
    {
        if (_fileTask == null)
            _fileTask = new FileTask(path);
        else
            _fileTask.reset(path);

        _fromPath = new File(path).getPath();

        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T select(String expr)
    {
        _fileTask.select(expr);

        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T exclude(String expr)
    {
        _fileTask.exclude(expr);

        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T excludeOnly(String expr)
    {
        _fileTask.excludeOnly(expr);

        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T add()
    {
        return (T) addFiles(_fileTask.getFiles(true));
    }

    @SuppressWarnings("unchecked")
    public T add(String... paths)
    {
        List<File> files = new ArrayList<File>();

        for (String path : paths)
            files.add(new File(path));

        return (T) addFiles(files);
    }

    @SuppressWarnings("unchecked")
    private T addFiles(List<File> files)
    {
        if (files.size() == 0)
            return (T) this;

        Map<String, List<File>> toFiles = _files.get(_toPath);

        if (toFiles == null)
        {
            toFiles = new HashMap<String, List<File>>();
            _files.put(_toPath, toFiles);
        }

        List<File> fromFiles = toFiles.get(_fromPath);

        if (fromFiles == null)
            toFiles.put(_fromPath, files);
        else
            fromFiles.addAll(files);

        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T add(List<Artifact> artifacts)
    {
        for (Artifact artifact : artifacts)
            from(new File(artifact.getPath()).getParent()).add(artifact.getPath());

        return (T) this;
    }

    @SuppressWarnings("unchecked")
    protected T create(boolean jar)
    {
        new File(_zipPath).getAbsoluteFile().getParentFile().mkdirs();

        try
        {
            OutputStream out = new BufferedOutputStream(new FileOutputStream(_zipPath));

            ZipOutputStream zipOut = jar ? new JarOutputStream(out) : new ZipOutputStream(out);

            zipOut.setLevel(Deflater.BEST_COMPRESSION);

            List<String> entries = new ArrayList<String>();

            for (Map.Entry<String, Map<String, List<File>>> toEntry : _files.entrySet())
            {
                _toPath = toEntry.getKey();
                Map<String, List<File>> toFiles = toEntry.getValue();

                for (Map.Entry<String, List<File>> fromEntry : toFiles.entrySet())
                {
                    _fromPath = fromEntry.getKey();
                    List<File> fromFiles = fromEntry.getValue();

                    for (File file : fromFiles)
                    {
                        String entryName = entryName(file);

                        if (entries.contains(entryName))
                            continue;

                        entries.add(entryName);

                        JarEntry entry = new JarEntry(entryName);
                        entry.setSize(file.length());

                        zipOut.putNextEntry(entry);

                        if (file.isFile())
                        {
                            byte[] data = BuildUtils.readFile(file);
                            CRC32 crc = new CRC32();
                            crc.update(data);
                            entry.setCrc(crc.getValue());
                            zipOut.write(data);
                        }

                        zipOut.closeEntry();
                    }
                }
            }

            zipOut.close();

            return (T) this;
        }
        catch (IOException e)
        {
            throw new Error("Unable to create file " + _zipPath, e);
        }
    }

    private String entryName(File file)
    {
        _buf.delete(0, _buf.length());

        int toLen = _toPath.length(), fromLen = _fromPath.length();

        _buf.append(_toPath).append(file.getPath()).delete(toLen, fromLen + toLen);

        if (file.isDirectory())
            _buf.append('/');

        String path = _buf.toString().replace('\\', '/');

        return path.startsWith("/") ? path.substring(1) : path;
    }
}
