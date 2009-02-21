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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class IOUtilsTest
{
    private class ReposModel
    {
        String type;
        String name;
        String url;
        String user;
        String password;

        ReposModel copy(String name)
        {
            ReposModel copy = new ReposModel();
            copy.name = name;
            copy.type = type;
            copy.url = url;
            copy.user = user;
            copy.password = password;

            return copy;
        }

        @Override
        public String toString()
        {
            return name + ":" + type;
        }
    }

    private class ProjectModel
    {
        String name;
        String repos;
        String reposPath;
        String reposRev;
        String javaVersion;
        String buildCmd;

        ProjectModel copy(String name)
        {
            ProjectModel prj = new ProjectModel();
            prj.name = name;
            prj.repos = repos;
            prj.reposPath = reposPath;
            prj.reposRev = reposRev;
            prj.javaVersion = javaVersion;
            prj.buildCmd = buildCmd;

            return prj;
        }

        @Override
        public String toString()
        {
            return name + ":" + reposPath + ":" + buildCmd;
        }
    }

    public final void testCopyFile()
    {
        try
        {
            IOUtils.copyFile(new File(getClass().getResource("Small-utf-8.txt").getPath()), new File(
                "src/test/resources/Small-utf-8-Copy.txt"), true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void testReadYaml()
    {
        IOUtils.readSyaml(getClass().getResource("test.syaml"), new IOUtils.SyamlHandler()
        {
            Map<String, ReposModel>   _reposMap   = new HashMap<String, ReposModel>();
            Map<String, ProjectModel> _projectMap = new HashMap<String, ProjectModel>();
            private boolean           _inRepos, _inProjects;
            private ReposModel        _defRepos   = new ReposModel();
            private ProjectModel      _defproject = new ProjectModel();
            private Object            _entity;
            private String            _key, _value;

            public void handleMapping(String key, String value, int level)
            {
                _key = key;
                _value = value;

                if (level == 0)
                    handleLevel0();
                else if (level == 1)
                    handleLevel1();
                else if (level == 2)
                    handleLevel2();
                else
                    System.out.println("Invalid level");

                //System.out.println(key + "=" + value + "=" + level);
            }

            private void handleLevel2()
            {
                try
                {
                    _entity.getClass().getDeclaredField(fieldName()).set(_entity, _value);
                }
                catch (Exception e)
                {
                    throw AggregateException.with(e);
                }
            }

            private String fieldName()
            {
                String[] parts = _key.split("-");

                if (parts.length <= 1)
                    return _key;

                StringBuilder fieldName = new StringBuilder(parts[0]);

                for (int i = 1; i < parts.length; i++)
                    fieldName.append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].substring(1));

                return fieldName.toString();
            }

            private void handleLevel1()
            {
                if ("defaults!".equals(_key))
                {
                    if (_inRepos)
                        _entity = _defRepos;

                    else if (_inProjects)
                        _entity = _defproject;
                }
                else if (_inRepos)
                    _entity = getReposModel();

                else if (_inProjects)
                    _entity = getProjectModel();
            }

            private void handleLevel0()
            {
                if ("repositories".equals(_key))
                    _inProjects = !(_inRepos = true);

                else if ("projects".equals(_key))
                    _inRepos = !(_inProjects = true);
                else
                    _inRepos = _inProjects = false;
            }

            private ReposModel getReposModel()
            {
                ReposModel repos = _reposMap.get(_key);

                if (repos == null)
                    _reposMap.put(_key, repos = _defRepos.copy(_key));

                return repos;
            }

            private ProjectModel getProjectModel()
            {
                ProjectModel prj = _projectMap.get(_key);

                if (prj == null)
                    _projectMap.put(_key, prj = _defproject.copy(_key));

                return prj;
            }

            public void endOfFile()
            {
                System.out.println(_reposMap);
                System.out.println(_projectMap);
            }
        });
    }
}
