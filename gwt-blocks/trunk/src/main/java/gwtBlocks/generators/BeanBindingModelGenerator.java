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

import gwtBlocks.shared.models.PropertyBindingModel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;

public class BeanBindingModelGenerator extends AbstractClassGenerator
{
    private class PropertyBindingModelGenerator
    {
        private final StringBuilder _propertyModelTemplate = new StringBuilder();

        PropertyBindingModelGenerator() throws Exception
        {
            if (_propertyModelTemplate.length() == 0)
                loadTemplate();
        }

        private void loadTemplate() throws Exception
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(
                "PropertyBindingModel.ftl")));

            try
            {
                String line = null;

                while ((line = reader.readLine()) != null)
                    _propertyModelTemplate.append(line).append('\n');
            }
            finally
            {
                reader.close();
            }
        }

        void generate(JMethod domainGetter, String propModelGetterName, String propPath, String propGetterPath)
        {
            String propName = domainGetter.getName().replaceFirst("get", "");
            String propTypeName = domainGetter.getReturnType().getQualifiedSourceName();

            String template = _propertyModelTemplate.toString();
            template = template.replaceAll("\\$\\{propertyTypeName\\}", propTypeName);
            template = template.replaceAll("\\$\\{propertyModelGetterName\\}", propModelGetterName);
            template = template.replaceAll("\\$\\{propertyPath\\}", propPath == null ? propName : propPath);
            template = template.replaceAll("\\$\\{propertyName\\}", propName);
            template = template.replaceAll("\\$\\{domainModelTypeName\\}", _genClass.getName());
            template = template.replaceAll("\\$\\{propertyGetterPath\\}", propGetterPath);

            _sourceWriter.println(template);
        }
    }

    private BeanBindingModelGeneratorHook _hook;

    @Override
    protected void initGenerator() throws UnableToCompleteException
    {
        try
        {
            URL propFileUrl = getClass().getResource("BeanBindingModelGenerator.properties");

            if (propFileUrl == null)
                return;

            Properties props = new Properties();
            props.load(propFileUrl.openStream());
            _hook = (BeanBindingModelGeneratorHook) Class.forName(props.getProperty("hook-class-name")).newInstance();
        }
        catch (Exception e)
        {
            _logger.log(TreeLogger.ERROR, "Unable to load BeanBindingModelGeneratorHook.", e);
            throw new UnableToCompleteException();
        }
    }

    @Override
    protected void addImports(ClassSourceFileComposerFactory composerFactory)
    {
        composerFactory.addImport("gwtBlocks.shared.models.PropertyBindingModel");

        if (_hook == null)
            return;

        String[] imports = _hook.getImports();

        if (imports == null)
            return;

        for (String imp : imports)
            composerFactory.addImport(imp);
    }

    @Override
    protected void generateSource(String packageName, String genClassName) throws Exception
    {
        String domainTypeName = _genClass.getAnnotation(BindingClass.class).value().getName();

        JClassType domainClass = getType(domainTypeName);

        generatePropertyModels(_genClass, domainClass, new PropertyBindingModelGenerator());
    }

    private void generatePropertyModels(JClassType modelClass, JClassType domainClass,
        PropertyBindingModelGenerator propGenerator) throws UnableToCompleteException
    {
        JMethod[] methods = modelClass.getMethods();
        JType[] nullArg = new JType[0];

        for (int i = 0; i < methods.length; i++)
        {
            if (methods[i].isAbstract()
                && methods[i].getReturnType().getSimpleSourceName().equals(PropertyBindingModel.class.getSimpleName()))
            {
                String propertyPath = getPropertyPath(methods[i]), propGetterPath = "";
                JMethod getterMethod = null;

                if (propertyPath == null)
                {
                    String getter = methods[i].getName().substring(0, methods[i].getName().lastIndexOf("Model"));
                    getterMethod = getMethod(domainClass, getter, nullArg);
                }
                else
                {
                    String[] props = propertyPath.split("\\.");
                    JClassType propClass = domainClass;

                    for (int j = 0; j < props.length; j++)
                    {
                        String getter = "get" + props[j].substring(0, 1).toUpperCase() + props[j].substring(1);

                        if (j < props.length - 1)
                            propGetterPath += getter + "().";

                        getterMethod = getMethod(propClass, getter, nullArg);
                        propClass = getterMethod.getReturnType().isClassOrInterface();
                    }
                }

                propGenerator.generate(getterMethod, methods[i].getName(), propertyPath, propGetterPath);
            }
        }
    }

    private String getPropertyPath(JMethod method)
    {
        BindingProperty annotation = method.getAnnotation(BindingProperty.class);

        return annotation == null ? null : annotation.value();
    }

    private JMethod getMethod(JClassType classType, String methodName, JType[] args) throws UnableToCompleteException
    {
        try
        {
            return classType.getMethod(methodName, args);
        }
        catch (NotFoundException e)
        {
            if (classType.getSuperclass() != null)
                return getMethod(classType.getSuperclass(), methodName, args);
            else
            {
                _logger.log(TreeLogger.ERROR, "Method not found. Unable to find method " + methodName + " in "
                    + classType + ". Check if property models conform to the naming conventions.", e);
                throw new UnableToCompleteException();
            }
        }
    }
}
