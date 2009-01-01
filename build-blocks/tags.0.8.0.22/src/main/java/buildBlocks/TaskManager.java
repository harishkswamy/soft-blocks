package buildBlocks;

import static buildBlocks.Context.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author hkrishna
 */
public class TaskManager
{
    private class TaskSet
    {
        private class Task
        {
            private String  _id;
            private Method  _method;
            private boolean _done;

            private Task(Method method)
            {
                _id = _container.prefix().length() == 0 ? method.getName() : _container.prefix() + ':'
                    + method.getName();
                _method = method;
            }

            private TaskInfo taskInfo()
            {
                return _method.getAnnotation(TaskInfo.class);
            }

            private String id()
            {
                return _id;
            }

            private String desc()
            {
                return taskInfo().desc();
            }

            private void execute()
            {
                if (_done)
                    return;

                TaskSet.this.execute(taskInfo().deps());

                try
                {
                    System.out.println(String.format("Executing %s...", _id));

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
                if (id().length() > 16)
                    System.out.println(String.format("    %s%n%-20s : %s", id(), "", desc()));
                else
                    System.out.println(String.format("    %-16s : %s", id(), desc()));
            }

            @Override
            public String toString()
            {
                return String.format("%s : %s", _id, taskInfo().desc());
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
            Task task = tasks().get(name);

            if (task == null)
            {
                if (_container.prefix().length() == 0)
                    throw new BuildError(String.format("Task %s is not defined in the project.", name, _container
                        .prefix()), false, true);
                else
                    throw new BuildError(String.format("Task %s is not defined in module %s.", name, _container
                        .prefix()), false, true);
            }

            return task;
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
            System.out.println(String.format("    Tasks from %s:", _container.getClass().getSimpleName()));
            System.out.println();

            for (Task task : tasks().values())
                task.printHelp();
        }
    }

    private SortedMap<String, TaskSet> _taskSets = new TreeMap<String, TaskSet>();

    TaskManager(TaskContainer container)
    {
        addContainer(container);
    }

    void register(TaskContainer... containers)
    {
        for (TaskContainer container : containers)
            addContainer(container);
    }

    private void addContainer(TaskContainer container)
    {
        TaskSet oldTaskSet = _taskSets.put(container.prefix(), new TaskSet(container));

        if (ctx().traceOn() && oldTaskSet != null)
        {
            String fmt = "Task container %s is being replaced by %s.%nMake sure to use a unique prefix if this is unintentional.";
            System.out.println(String.format(fmt, oldTaskSet._container, container));
        }
    }

    void execute(String... taskIds)
    {
        for (String taskId : taskIds)
        {
            String[] taskSpec = taskId.split(":");

            String prefix = taskSpec.length > 1 ? taskSpec[0] : "";

            TaskSet taskSet = _taskSets.get(prefix);

            if (taskSet == null)
                throw new BuildError(String.format("%s module has not been registered.", prefix), false, true);

            taskSet.execute(taskSpec[taskSpec.length - 1]);
        }
    }

    void printHelp()
    {
        for (TaskSet taskSet : _taskSets.values())
            taskSet.printHelp();
    }
}
