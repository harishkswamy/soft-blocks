package buildBlocks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author hkrishna
 */
class TaskManager
{
    private class TaskSet
    {
        private class Task
        {
            private Method   _method;
            private String   _id;
            private String   _desc;
            private String[] _deps;
            private boolean  _done;

            private Task(Method method)
            {
                TaskInfo info = method.getAnnotation(TaskInfo.class);

                if (info == null)
                    throw new Error("Tasks must be annotated with " + TaskInfo.class);

                _method = method;
                _id = _container.qid() + ':' + _method.getName();
                _desc = info.desc();
                _deps = info.deps();
            }

            private void execute()
            {
                if (_done)
                    return;

                TaskSet.this.execute(_deps);

                try
                {
                    System.out.println(String.format("Executing %s...", _id));

                    _method.setAccessible(true);
                    _method.invoke(_container, (Object[]) null);
                    _done = true;

                    System.out.println(String.format("%s done.", _id));
                    System.out.println();
                }
                catch (InvocationTargetException e)
                {
                    throw new Error("Unable to execute task " + _id, e.getCause());
                }
                catch (Exception e)
                {
                    throw new Error("Unable to execute task " + _id, e);
                }
            }

            private void printHelp()
            {
                if (_method.getName().length() > 16)
                    System.out.println(String.format("    %s%n%-20s : %s", _method.getName(), "", _desc));
                else
                    System.out.println(String.format("    %-16s : %s", _method.getName(), _desc));
            }

            @Override
            public String toString()
            {
                return String.format("%s : %s", _id, _desc);
            }
        }

        private TaskContainer           _container;
        private SortedMap<String, Task> _tasks;

        private TaskSet(TaskContainer container)
        {
            _container = container;
        }

        private SortedMap<String, Task> tasks()
        {
            if (_tasks == null)
                load();

            return _tasks;
        }

        private void load()
        {
            _tasks = new TreeMap<String, Task>();

            Method[] methods = _container.getClass().getMethods();

            for (Method m : methods)
            {
                TaskInfo taskInfo = m.getAnnotation(TaskInfo.class);

                if (taskInfo == null)
                    continue;

                _tasks.put(m.getName(), new Task(m));
            }
        }

        private Task get(String name)
        {
            return tasks().get(name);
        }

        private void execute(String... taskNames)
        {
            for (String taskName : taskNames)
            {
                if (taskName.indexOf(':') > -1)
                    TaskManager.this.execute(taskName);
                else
                    get(taskName).execute();
            }
        }

        private void printHelp()
        {
            System.out.println();
            System.out.println(String.format("    Tasks from %s:", _container.qid()));
            System.out.println();

            for (Task task : tasks().values())
                task.printHelp();
        }
    }

    private List<TaskSet> _taskSets = new ArrayList<TaskSet>();

    TaskManager()
    {
    }

    void register(TaskContainer... containers)
    {
        for (TaskContainer container : containers)
            _taskSets.add(new TaskSet(container));
    }

    void unregister(TaskContainer... containers)
    {
        for (TaskContainer container : containers)
        {
            for (int i = 0; i < _taskSets.size(); i++)
            {
                if (_taskSets.get(i)._container.equals(container))
                    _taskSets.remove(i);
            }
        }
    }

    void execute(String... taskIds)
    {
        for (String taskId : taskIds)
        {
            int idx = taskId.lastIndexOf(':');

            String cid = idx == -1 ? null : taskId.substring(0, idx);
            String taskName = taskId.substring(idx + 1);

            TaskSet.Task task = null;

            for (TaskSet taskSet : _taskSets)
            {
                if (cid == null || taskSet._container.qid().endsWith(cid))
                {
                    TaskSet.Task tTask = taskSet.get(taskName);

                    if (tTask == null)
                        continue;

                    if (task != null)
                        throw new BuildError(
                            "More than one task match the request, please qualify the task name further.", false, true);

                    task = tTask;
                }
            }

            if (task == null)
                throw new BuildError(String.format("Task %s does not match any task.", taskId), false, true);

            task.execute();
        }
    }

    void printHelp()
    {
        for (TaskSet taskSet : _taskSets)
            taskSet.printHelp();
    }
}
