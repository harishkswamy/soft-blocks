// Copyright 2008 Harish Krishnaswamy
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package gwtBlocks.generators;

import java.io.PrintWriter;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

/**
 * @author hkrishna
 */
public abstract class AbstractClassGenerator extends Generator
{
    protected TreeLogger     _logger;
    protected SourceWriter   _sourceWriter;
    protected JClassType     _genClass;

    private GeneratorContext _context;

    @Override
    public String generate(TreeLogger logger, GeneratorContext context, String genClassName)
        throws UnableToCompleteException
    {
        _logger = logger;
        _context = context;

        try
        {
            _genClass = getType(genClassName);

            initGenerator();

            String packageName = _genClass.getPackage().getName();
            String proxyClassName = _genClass.getSimpleSourceName() + "_Proxy";
            String proxyClassFullName = packageName + "." + proxyClassName;

            _sourceWriter = getSourceWriter(packageName, genClassName, proxyClassName);

            if (_sourceWriter != null)
            {
                generateSource(packageName, proxyClassName);
                _sourceWriter.commit(logger);
            }

            return proxyClassFullName;
        }
        catch (UnableToCompleteException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            logger.log(TreeLogger.ERROR, "Unable to generate source due to unexpected error.", e);
            throw new UnableToCompleteException();
        }
    }

    protected JClassType getType(String typeName) throws UnableToCompleteException
    {
        try
        {
            return _context.getTypeOracle().getType(typeName);
        }
        catch (NotFoundException e)
        {
            _logger.log(TreeLogger.ERROR, "Class not found. Check if " + typeName + " is available in the class path.",
                e);
            throw new UnableToCompleteException();
        }
    }

    private SourceWriter getSourceWriter(String packageName, String genClassName, String proxyClassName)
    {
        PrintWriter printWriter = _context.tryCreate(_logger, packageName, proxyClassName);

        if (printWriter == null)
            return null;

        ClassSourceFileComposerFactory composerFactory = new ClassSourceFileComposerFactory(packageName, proxyClassName);

        if (_genClass.isInterface() != null)
            composerFactory.addImplementedInterface(genClassName);
        else
            composerFactory.setSuperclass(genClassName);

        addImports(composerFactory);

        return composerFactory.createSourceWriter(_context, printWriter);
    }

    protected abstract void initGenerator() throws Exception;

    protected abstract void addImports(ClassSourceFileComposerFactory composerFactory);

    protected abstract void generateSource(String packageName, String genClassName) throws Exception;
}
