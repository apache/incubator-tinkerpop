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
package org.apache.tinkerpop.gremlin.osgi;

import static org.junit.Assert.assertEquals;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

import java.util.Arrays;
import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OsgiIntegrateTest
{
    @Inject
    public BundleContext bundleContext;

    @Configuration
    public Option[] config() {

        return options(
                junitBundles(),
                // commons-lang.commons-lang:2.6 needed by commons-configuration
                mavenBundle()
                        .groupId("commons-lang")
                        .artifactId("commons-lang")
                        .version("2.6"),
                // commons-configuration.commons-configuration:1.10 needed by gremlin-core
                mavenBundle()
                        .groupId("commons-configuration")
                        .artifactId("commons-configuration")
                        .version("1.10"),
                // org.yaml.snakeyaml:1.15 needed by gremlin-core
                mavenBundle()
                        .groupId("org.yaml")
                        .artifactId("snakeyaml")
                        .version("1.15"),
                // com.github.gdelafosse:gremlin-osgi-deps needed until all dependencies are OSGIfied
                mavenBundle()
                        .groupId("com.github.gdelafosse")
                        .artifactId("gremlin-osgi-deps")
                        .version("3.1.1-1"),
                // gremlin-shaded needed by gremlin-core
                mavenBundle()
                        .groupId(System.getProperty("project.groupId"))
                        .artifactId("gremlin-shaded")
                        .version(System.getProperty("project.version")),
                // gremlin-core
                mavenBundle()
                        .groupId(System.getProperty("project.groupId"))
                        .artifactId(System.getProperty("project.artifactId"))
                        .version(System.getProperty("project.version"))
        );
    }

    @Test
    public void testAllBundlesAreValid() {
        Arrays.stream(bundleContext.getBundles())
                .forEach(b -> assertEquals(String.format("Bundle [%s] must be ACTIVE", b.getSymbolicName()), Bundle.ACTIVE, b.getState() ));
    }
}
