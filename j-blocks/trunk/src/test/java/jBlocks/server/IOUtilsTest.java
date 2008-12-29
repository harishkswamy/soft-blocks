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

import jBlocks.server.IOUtils;

import java.io.File;

import org.junit.Test;

public class IOUtilsTest
{
    @Test
    public final void testCopyFile()
    {
        try
        {
            IOUtils.copyFile(new File(IOUtilsTest.class.getResource("Small-utf-8.txt").getPath()), new File("src/test/resources/Small-utf-8-Copy.txt"), true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
