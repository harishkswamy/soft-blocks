package buildBlocks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author hkrishna
 */
public class FileTask
{
    private static String _defaultExcludes = ".*/\\.svn(/.*)?";

    public static void defaultExcludes(String expr)
    {
        _defaultExcludes = expr;
    }

    private File    _source;
    private String  _includeExpr;
    private String  _excludeExpr;

    private Pattern _include;
    private Pattern _exclude;

    private boolean _includeDirs;
    private boolean _includeFiles;
    private boolean _forceWrite;

    public FileTask(String path)
    {
        source(path);
    }

    public FileTask reset(String source)
    {
        reset();

        return source(source);
    }

    private FileTask reset()
    {
        _includeExpr = null;
        _excludeExpr = null;

        _include = null;
        _exclude = null;

        _includeDirs = false;
        _includeFiles = false;
        _forceWrite = false;

        return this;
    }

    private FileTask source(String path)
    {
        _source = new File(path);

        return this;
    }

    public FileTask select(String expr)
    {
        _includeExpr = expr;

        return this;
    }

    public FileTask exclude(String expr)
    {
        if (expr == null)
            _excludeExpr = _defaultExcludes;
        else if (_defaultExcludes == null)
            _excludeExpr = expr;
        else
            _excludeExpr = new StringBuilder(expr).append('|').append(_defaultExcludes).toString();

        return this;
    }

    public FileTask excludeOnly(String expr)
    {
        _excludeExpr = expr;

        return this;
    }

    private Pattern includes()
    {
        if (_include == null)
            _include = _includeExpr == null ? null : Pattern.compile(_includeExpr);

        return _include;
    }

    private Pattern excludes()
    {
        if (_exclude == null)
            _exclude = _excludeExpr == null ? null : Pattern.compile(_excludeExpr);

        return _exclude;
    }

    public List<File> getDirs()
    {
        _includeDirs = true;

        return get();
    }

    public List<File> getFiles(boolean includeDirs)
    {
        _includeFiles = true;
        _includeDirs = includeDirs;

        return get();
    }

    private List<File> get()
    {
        List<File> files = new ArrayList<File>();

        if (_source.isDirectory())
            addFiles(_source, files);

        reset();

        return files;
    }

    private void addFiles(File dir, List<File> files)
    {
        for (File file : dir.listFiles())
        {
            if (!includeFile(file))
                continue;

            if (file.isDirectory())
            {
                if (_includeDirs)
                    files.add(file);

                addFiles(file, files);
            }
            else if (_includeFiles)
                files.add(file);
        }
    }

    public FileTask copyToFile(String path, boolean forceWrite)
    {
        if (_source.isDirectory())
            throw new Error(String.format("Cannot copy directory %s to file %s.", _source, path));

        File target = new File(path);

        if (target.isDirectory())
            throw new Error(String.format("Cannot create file with same name as a directory: %s.", path));

        _forceWrite = forceWrite;

        copy(_source, target);

        return reset();
    }

    public FileTask copyToDir(String path, boolean forceWrite)
    {
        File target = new File(path);

        if (target.isFile())
            throw new Error(String.format("Cannot create directory with same name as a file: %s.", path));

        _forceWrite = forceWrite;

        if (_source.isFile())
            target = new File(target, _source.getName());

        copy(_source, target);

        return reset();
    }

    private void copy(File srcFile, File destFile)
    {
        if (!srcFile.exists())
            return;

        if (srcFile.isDirectory())
        {
            if (destFile.isFile())
                throw new Error("Destination path " + destFile + " must be a directory.");

            String[] fileNames = srcFile.list();
            String srcPath = srcFile.getPath(), destPath = destFile.getPath();

            for (String fileName : fileNames)
                copy(new File(srcPath, fileName), new File(destPath, fileName));
        }
        else
        {
            if (!includeFile(srcFile))
                return;

            try
            {
                File parent = destFile.getParentFile();

                if (parent != null && !parent.exists() && !parent.mkdirs())
                    throw new Error("Unable to create directories for: " + destFile);

                // If overwrite or the destination file does not exist or it is older than the source file, 
                // then copy
                if (_forceWrite || destFile.createNewFile() || destFile.lastModified() < srcFile.lastModified())
                    Utils.writeFile(destFile, Utils.readFile(srcFile));
            }
            catch (IOException e)
            {
                throw new Error("Unable to copy file " + _source + " to " + destFile, e);
            }
        }
    }

    public FileTask delete()
    {
        delete(_source);

        return reset();
    }

    private void delete(File file)
    {
        if (!file.exists())
            return;

        if (file.isDirectory())
        {
            for (String fileName : file.list())
                delete(new File(file.getPath(), fileName));
        }

        if (!includeFile(file))
            return;

        if (!file.delete())
            throw new Error("Unable to delete " + file.getPath());
    }

    private boolean includeFile(File file)
    {
        String path = file.getPath().replace('\\', '/');

        if (file.isDirectory())
            path += '/';

        Pattern includes = includes(), excludes = excludes();

        return (includes == null || includes.matcher(path).matches())
            && (excludes == null || !excludes.matcher(path).matches());
    }
}
