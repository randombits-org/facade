/**
 * This package provides support for the {@link org.randombits.facade} package by allowing
 * <a href="http://java.sun.com/j2se/1.5.0/docs/guide/apt/">APT</a> to check classes using {@link Facadable} for correct usage.
 * 
 * <p>
 * To check your project's source code, simply put the library containing this package on the classpath and run <code>apt</code>.
 * 
 * <p>
 * To use in a Maven 2 project, use the <a href="http://mojo.codehaus.org/apt-maven-plugin/index.html">Codehaus Apt Plugin</a>.
 * Add something like the following in your <code>pom.xml</code> file's <code>&lt;build&gt;</code> declarations:
 * 
 * <pre>
 * &lt;plugin&gt;
 *   &lt;groupId&gt;org.codehaus.mojo&lt;/groupId&gt;
 *   &lt;artifactId&gt;apt-maven-plugin&lt;/artifactId&gt;
 *   &lt;executions&gt;
 *     &lt;execution&gt;
 *       &lt;goals&gt;
 *         &lt;goal&gt;process&lt;/goal&gt;
 *         &lt;goal&gt;test-process&lt;/goal&gt;
 *       &lt;/goals&gt;
 *     &lt;/execution&gt;
 *   &lt;/executions&gt;
 * &lt;/plugin&gt;
 * </pre>
 *
 * <p>
 * You will also need to add the Codehaus Maven 2 plugin repository to either the <code>pom.xml</code> or
 * your <code>settings.xml</code> file.
 */
package org.randombits.facade.apt;

import org.randombits.facade.Facadable;

