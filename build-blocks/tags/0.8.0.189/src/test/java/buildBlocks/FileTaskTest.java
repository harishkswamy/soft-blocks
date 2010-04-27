package buildBlocks;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.Test;

/**
 * @author hkrishna
 */
public class FileTaskTest
{
    @Test
    public void getFilesFromSubDirsTharMatchAGivenPattern()
    {
        List<File> files = new FileTask(".").select(".*buildBlocks.*\\.class").exclude(".*/build|.*/work").getFiles(false);

        for (File file : files)
            System.out.println(file);
    }

    @Test
    public void deleteFiles()
    {
        FileTask fileTask = new FileTask("target/main").select(".*Module.*\\.class").copyToDir("target/test/work",
            false);

        File work = new File("target/test/work");

        assertTrue(work.exists());

        fileTask.reset("target/test/work").delete();

        assertTrue(!work.exists());
    }

    @Test
    public void copyFiles()
    {
        FileTask fileTask = new FileTask("target/test/work").delete();

        File work = new File("target/test/work");

        assertTrue(!work.exists());

        fileTask.reset("target/main").select(".*Module.*\\.class").copyToDir("target/test/work", false);

        assertTrue(work.exists());
    }
}
