/*
 * Copyright © 2018 Frinx and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.frinx.binding.ids.unitopo;

import io.frinx.binding.ids.UnitopoTranslationUnitCollector;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;


/**
 * This class is responsible for loading the unit from classPath and creating reflection object.
 */
public class UnitLoader {

    private static final String INIT = "init";
    private static final String UNIT_CLASS = "Unit.class";
    private static final String DOT_CLASS = ".class";
    private final MavenProject project;
    private Set<YangModuleInfo> yangModuleInfos = Collections.emptySet();
    private final Log log = new SystemStreamLog();

    UnitLoader(MavenProject project) {

        this.project = project;
    }

    public Optional<Object> getReflectionObject(UnitopoTranslationUnitCollector unitCollector)
            throws ClassNotFoundException {
        Object[] intArgs = new Object[]{unitCollector};
        return createObject(tryGetConstructor(), intArgs);
    }

    public Set<YangModuleInfo> getYangModuleInfos() {
        return yangModuleInfos;
    }

    private Constructor<?> tryGetConstructor() throws ClassNotFoundException {

        Class<?> classReflection;
        try {
            classReflection = findUnitClass();
        } catch (DependencyResolutionRequiredException | ClassNotFoundException | IOException
                | IllegalArgumentException e) {
            throw new ClassNotFoundException("Class has not been found. Skipping this unit no docs will be generated.");
        }

        Constructor<?>[] classConstructor;

        //Trying to get constructor.
        classConstructor = classReflection.getConstructors();

        if (classConstructor.length == 0) {
            throw new ClassNotFoundException("Constructor has not been found, skipping this unit.");
        }
        return classConstructor[0];
    }

    private Class<?> findUnitClass() throws IllegalArgumentException, DependencyResolutionRequiredException,
            ClassNotFoundException, IOException {

        String unitPath;
        List runtimeClasspathElements;

        runtimeClasspathElements = project.getRuntimeClasspathElements();
        URL[] runtimeUrls = new URL[runtimeClasspathElements.size()];
        for (int i = 0; i < runtimeClasspathElements.size(); i++) {
            String element = (String) runtimeClasspathElements.get(i);
            runtimeUrls[i] = new File(element).toURI().toURL();
        }

        unitPath = parseUnitPath(runtimeClasspathElements.iterator().next().toString());

        log.debug(unitPath + " has been found and will be used");

        URLClassLoader newLoader = new URLClassLoader(runtimeUrls,
                Thread.currentThread().getContextClassLoader());

        Class<?> classReflection;
        classReflection = newLoader.loadClass(unitPath);
        yangModuleInfos = BindingReflections.loadModuleInfos(newLoader);

        return classReflection;
    }

    /**
     * It parses the path from files to be used for the classloader.
     * This method is looking for class ending with Unit.class
     * then we parse the PATH to the package specific path in project to be used in classloader,
     */
    private String parseUnitPath(String dir) throws IOException, IllegalArgumentException {

        List<String> listPaths = Files.find(Paths.get(dir), 100, (path, bfa) -> bfa.isRegularFile())
                .map(path -> path.normalize().toString())
                .filter(path -> path.endsWith(UNIT_CLASS))
                .map(path -> path.replace(dir + "/", ""))
                .map(path -> path.replace(DOT_CLASS, ""))
                .map(path -> path.replace("/", "."))
                .collect(Collectors.toList());
        if (listPaths.isEmpty() || listPaths.size() >= 2) {
            throw new IllegalArgumentException();
        }
        return listPaths.iterator().next();
    }

    private Optional<Object> createObject(@Nonnull Constructor constructor, @Nonnull Object[] arguments) {
        try {
            return Optional.of(constructor.newInstance(arguments));
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            log.warn("Could not create reflection from classloader, docs wont be generated for this unit.", e);
        }
        return Optional.empty();
    }

    public void callInit(@Nonnull Object reflectionObject) {

        try {
            Method method = reflectionObject.getClass().getMethod(
                    INIT);
            method.invoke(reflectionObject);
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            log.warn("Could not invoke method init() in " + reflectionObject.getClass().getName()
                    + "yang docs wont be generated for this unit", e);
        }
    }
}
