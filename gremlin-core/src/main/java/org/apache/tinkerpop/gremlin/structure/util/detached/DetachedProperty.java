/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.gremlin.structure.util.detached;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;

import java.io.Serializable;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class DetachedProperty<V> implements Property, Serializable, Attachable<Property<V>> {

    private String key;
    private V value;
    private transient DetachedElement element;

    private DetachedProperty() {
    }

    protected DetachedProperty(final Property<V> property) {
        this.key = property.key();
        this.value = property.value();
        this.element = DetachedFactory.detach(property.element(), false);
    }

    public DetachedProperty(final String key, final V value, final Element element) {
        this.key = key;
        this.value = value;
        this.element = DetachedFactory.detach(element, false);
    }


    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public String key() {
        return this.key;
    }

    @Override
    public V value() {
        return this.value;
    }

    @Override
    public Element element() {
        return this.element;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Detached properties are readonly: " + this.toString());
    }

    @Override
    public String toString() {
        return StringFactory.propertyString(this);
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(final Object object) {
        return ElementHelper.areEqual(this, object);
    }

    @Override
    public int hashCode() {
        return ElementHelper.hashCode(this);
    }

    @Override
    public Property<V> attach(final Vertex hostVertex) {
        final Element element = (Element) this.element.attach(hostVertex);
        final Property<V> property = element.property(this.key);
        if (property.isPresent())
            return property;
        else
            throw new IllegalStateException("The detached property could not be be found at the provided vertex: " + this);
    }

    @Override
    public Property<V> attach(final Graph hostGraph) {
        final Element hostElement = (Element) this.element.attach(hostGraph);
        final Property<V> property = hostElement.property(this.key);
        if (property.isPresent())
            return property;
        else
            throw new IllegalStateException("The detached property could not be be found at the provided vertex: " + this);
    }
}
