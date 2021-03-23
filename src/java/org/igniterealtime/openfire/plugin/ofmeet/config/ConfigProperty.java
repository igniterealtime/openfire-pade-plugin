/*
 * Copyright (C) 2018 Ignite Realtime Foundation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.igniterealtime.openfire.plugin.ofmeet.config;

import org.jivesoftware.util.JiveGlobals;

import java.util.Objects;

public abstract class ConfigProperty<T>
{
    protected final String propertyName;
    protected final T defaultValue;
    private final T valueAtConstruction;

    public ConfigProperty( String propertyName, T defaultValue )
    {
        if (propertyName == null || propertyName.isEmpty() )
        {
            throw new IllegalArgumentException( "Argument 'propertyName' cannot be null or an empty String." );
        }
        this.propertyName = propertyName;
        this.defaultValue = defaultValue;

        valueAtConstruction = get();
    }

    public boolean wasChanged() {
        return Objects.equals( valueAtConstruction, get() );
    }

    public abstract void set( T value );
    public abstract T get();

    public void reset()
    {
        JiveGlobals.deleteProperty( propertyName );
    }

    @Override
    public String toString()
    {
        // This allows one to use the config property without explicitly calling the getter.
        return get().toString();
    }
}
