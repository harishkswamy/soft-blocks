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
package gwtBlocks.client.models;

import gwtBlocks.generators.BindingClass;
import gwtBlocks.generators.BindingProperty;

/**
 * This model is a collection of {@link PropertyBindingModel}s each of which binds to a property in the binding object
 * wrapped by this model. The binding enables automatic convertion, validation and transfer of user input to the domain
 * object.
 * <p>
 * This is a buffered model, meaning values stored in this model are not transferred to the domain object until
 * {@link #commit()} is called. However, buffering can be turned off by turning auto commit on via
 * {@link #setAutoCommit(boolean)}.
 * <p>
 * This model is automatically generated based on the conventions outlined below.
 * <ul>
 * <li>The model class must declare a no args constructor.</li>
 * <li>The model class must declare the binding class name via the {@link BindingClass} annotation. For example, the
 * following declaration binds the {@link PropertyBindingModel} properties in <code>DomainModel</code> to the
 * properties in <code>com.xyz.Domain</code>.
 * 
 * <pre><code>
 * &#064;BindingClass(com.xyz.Domain.class) 
 * public abstract DomainModel extends BeanBindingModel&lt;Domain&gt;
 * {
 *     &#064;BindingProperty(&quot;child.name&quot;)
 *     public abstract PropertyBindingModel&lt;String&gt; getChildNameModel();
 *     
 *     public abstract PropertyBindingModel&lt;String&gt; getNameModel();
 * }
 * </code></pre>
 * 
 * <li>The binding class must declare a no args constructor.</li>
 * <li>The model class must declare the properties to be bound as abstract getter methods that return
 * {@link PropertyBindingModel} as shown in the example above. The getter methods must declare the binding property via
 * the {@link BindingProperty} annotation. Simple properties can follow the convention shown in the above example for
 * <code>getNameModel()</code> and skip the annotation.</li>
 * </ul>
 * 
 * @author hkrishna
 */
public abstract class BeanBindingModel<V> extends CompositeModel<V>
{
}
