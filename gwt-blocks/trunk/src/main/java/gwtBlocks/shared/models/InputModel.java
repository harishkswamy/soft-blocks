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
package gwtBlocks.shared.models;

/**
 * @author hkrishna
 */
public class InputModel<T> extends ValidatableModel<T>
{
    public InputModel()
    {

    }

    /**
     * Instatiates and registers itself as a child in the provided parent.
     * 
     * @param key
     *            The key that identifies this model in the parent.
     * @param parent
     *            The parent model.
     */
    public InputModel(String key, CompositeModel<?> parent)
    {
        setParent(key, parent);
    }
}
