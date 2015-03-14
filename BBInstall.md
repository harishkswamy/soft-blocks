# Instructions #

  1. Download the binary distribution from [Google Code](http://code.google.com/p/soft-blocks/downloads/list).
  1. Unzip the distribution to the local file system (eg. `C:\`). This will create a directory - `build-blocks` (eg. `C:\build-blocks`) - which we will call "BB\_HOME".
  1. Set the environment variables shown in the images below
> > ![http://soft-blocks.googlecode.com/svn/wiki/java_home_sys_var.gif](http://soft-blocks.googlecode.com/svn/wiki/java_home_sys_var.gif)
> > ![http://soft-blocks.googlecode.com/svn/wiki/home_sys_var.gif](http://soft-blocks.googlecode.com/svn/wiki/home_sys_var.gif)
> > ![http://soft-blocks.googlecode.com/svn/wiki/path_sys_var.gif](http://soft-blocks.googlecode.com/svn/wiki/path_sys_var.gif)
  1. Open in the `setting.properties` file under `BB_HOME\lib` and update the `local.repository.path` (default is `<user_home>\.m2\repository`) and `remote.repository.urls` variables to the Maven repository locations. The remote URLs can be a comma-separated list of URLs that the builder will search for dependencies.

That's it! Now open a command window and type `bbw` and you should see the help info as shown in the image below, if all went well.


> ![http://soft-blocks.googlecode.com/svn/wiki/bbw_help.gif](http://soft-blocks.googlecode.com/svn/wiki/bbw_help.gif)