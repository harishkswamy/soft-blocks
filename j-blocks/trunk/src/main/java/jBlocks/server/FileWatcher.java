package jBlocks.server;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Pattern;

/**
 * @author hkrishna
 */
public class FileWatcher
{
    public static final Comparator<File> fileNameComparator = new Comparator<File>()
                                                            {
                                                                public int compare(File o1, File o2)
                                                                {
                                                                    return o1.getName().compareTo(o2.getName());
                                                                }
                                                            };
    public static final Comparator<File> fileAgeComparator  = new Comparator<File>()
                                                            {
                                                                public int compare(File f1, File f2)
                                                                {
                                                                    long diff = f1.lastModified() - f2.lastModified();
                                                                    return diff > 0 ? 1 : diff < 0 ? -1 : 0;
                                                                }
                                                            };

    private File                         _watchDir;
    private int                          _fileAge;
    private Comparator<File>             _comparator;
    private FileFilter                   _filter;

    public FileWatcher(String watchDirPath)
    {
        _watchDir = new File(watchDirPath);
    }

    public FileWatcher fileAge(int age)
    {
        _fileAge = age;

        return this;
    }

    public FileWatcher comparator(Comparator<File> comparator)
    {
        _comparator = comparator;

        return this;
    }

    public FileWatcher fileFilterPattern(final String patternStr)
    {
        _filter = new FileFilter()
        {
            Pattern _pattern = Pattern.compile(patternStr == null ? ".*" : patternStr);

            public boolean accept(File f)
            {
                if (!f.isFile() || !f.canRead())
                    return false;

                if ((System.currentTimeMillis() - f.lastModified()) < _fileAge * 1000L)
                    return false;

                if (!_pattern.matcher(f.getName()).matches())
                    return false;

                return true;
            }
        };

        return this;
    }

    public File[] getFiles()
    {
        if (_filter == null)
            fileFilterPattern(null);

        File[] files = _watchDir.listFiles(_filter);

        if (files != null && _comparator != null)
            Arrays.sort(files, _comparator);

        return files;
    }
}
