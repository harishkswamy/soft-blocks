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
    private abstract class FileVisitor
    {
        protected void enterDir(File file)
        {
        }

        protected void exitDir(File file)
        {
        }

        protected void visitFile(File file)
        {
        }
    }

    private static String _defaultExcludes = ".*/\\.svn(/.*)?";

    public static void defaultExcludes(String expr)
    {
        _defaultExcludes = expr;
    }

    private File        _basePath;
    private String      _includeExpr;
    private String      _excludeExpr;

    private Pattern     _include;
    private Pattern     _exclude;

    private boolean     _includeDirs;
    private boolean     _includeFiles;
    private boolean     _forceWrite;

    private FileVisitor _fileVisitor;

    public FileTask(String basePath)
    {
        source(basePath);
    }

    public FileTask reset(String basePath)
    {
        reset();

        return source(basePath);
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

        _fileVisitor = null;

        return this;
    }

    private FileTask source(String path)
    {
        _basePath = new File(path);

        return this;
    }

    public FileTask select(String regExpr)
    {
        _includeExpr = regExpr;

        return this;
    }

    public FileTask exclude(String regExpr)
    {
        if (regExpr == null)
            _excludeExpr = _defaultExcludes;
        else if (_defaultExcludes == null)
            _excludeExpr = regExpr;
        else
            _excludeExpr = new StringBuilder(regExpr).append('|').append(_defaultExcludes).toString();

        return this;
    }

    public FileTask excludeOnly(String regExpr)
    {
        _excludeExpr = regExpr;

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
        final List<File> files = new ArrayList<File>();

        if (_basePath.isDirectory())
        {
            _fileVisitor = new FileVisitor()
            {
                @Override
                protected void enterDir(File file)
                {
                    if (_includeDirs)
                        files.add(file);
                }

                @Override
                protected void visitFile(File file)
                {
                    if (_includeFiles)
                        files.add(file);
                }
            };

            traverse(_basePath);
        }

        reset();

        return files;
    }

    public FileTask copyToFile(String path, boolean forceWrite)
    {
        if (_basePath.isDirectory())
            throw new Error(String.format("Cannot copy directory %s to file %s.", _basePath, path));

        File target = new File(path);

        if (target.isDirectory())
            throw new Error(String.format("Cannot create file with same name as a directory: %s.", path));

        _forceWrite = forceWrite;

        if (_basePath.exists())
            copy(_basePath, target);

        return reset();
    }

    public FileTask copyToDir(String path, boolean forceWrite)
    {
        File target = new File(path);

        if (target.isFile())
            throw new Error(String.format("Cannot create directory with same name as a file: %s.", path));

        _forceWrite = forceWrite;

        if (_basePath.isFile())
            target = new File(target, _basePath.getName());

        if (_basePath.exists())
            copy(_basePath, target);

        return reset();
    }

    private void copy(final File src, final File dest)
    {
        _fileVisitor = new FileVisitor()
        {
            @Override
            protected void visitFile(File srcFile)
            {
                File destFile = new File(dest, srcFile.getPath().substring(src.getPath().length()));

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
                    throw new Error("Unable to copy " + srcFile + " to " + destFile, e);
                }
            }
        };

        traverse(src);
    }

    public FileTask delete()
    {
        if (!_basePath.exists())
            return this;

        _fileVisitor = new FileVisitor()
        {
            @Override
            protected void exitDir(File file)
            {
                if (!file.delete())
                    throw new Error("Unable to delete directory " + file.getPath());
            }

            @Override
            protected void visitFile(File file)
            {
                if (!file.delete())
                    throw new Error("Unable to delete file " + file.getPath());
            }
        };

        traverse(_basePath);

        return reset();
    }

    private void traverse(File file)
    {
        String path = file.getPath().replace('\\', '/');

        if (excludes() != null && excludes().matcher(path).matches())
            return;

        boolean includeFile = includes() == null || includes().matcher(path).matches();

        if (file.isDirectory())
        {
            if (includeFile)
                _fileVisitor.enterDir(file);

            for (String fileName : file.list())
                traverse(new File(file.getPath(), fileName));

            if (includeFile)
                _fileVisitor.exitDir(file);
        }
        else if (includeFile)
            _fileVisitor.visitFile(file);
    }
}
