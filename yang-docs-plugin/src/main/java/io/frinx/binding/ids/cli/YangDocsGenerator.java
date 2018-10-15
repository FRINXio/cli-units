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

package io.frinx.binding.ids.cli;

import io.frinx.binding.ids.CliUnitCollector;
import io.frinx.binding.ids.CodecTranslator;
import io.frinx.binding.ids.TranslationUnitMetadata;
import io.frinx.binding.ids.TranslationUnitMetadataHandler;
import io.frinx.binding.ids.YangDocsWriter;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.registry.spi.TranslateUnit.Context;
import io.frinx.translate.unit.commons.utils.CapturingReaderRegistryBuilder;
import io.frinx.translate.unit.commons.utils.CapturingWriterRegistryBuilder;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.apache.maven.project.MavenProject;
import org.opendaylight.controller.md.sal.binding.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang2sources.spi.BasicCodeGenerator;
import org.opendaylight.yangtools.yang2sources.spi.BuildContextAware;
import org.opendaylight.yangtools.yang2sources.spi.MavenProjectAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * This is maven-plugin endpoint class which manages the creation of YangDocs.
 */
public final class YangDocsGenerator implements BasicCodeGenerator, BuildContextAware, MavenProjectAware {
    private static final Logger LOG = LoggerFactory.getLogger(YangDocsGenerator.class);

    private MavenProject project;
    private Set<YangModuleInfo> yangModuleInfos = Collections.emptySet();
    private Object reflectionObject;

    @Override
    public Collection<File> generateSources(SchemaContext context,
                                            File outputBaseDir,
                                            Set<Module> currentModules,
                                            Function<Module, Optional<String>> moduleResourcePathResolver) {


        CliUnitCollector unitCollector = new CliUnitCollector();
        UnitLoader unitLoader = new UnitLoader(project);
        try {
            reflectionObject = unitLoader.getReflectionObject(unitCollector);
            yangModuleInfos = unitLoader.getYangModuleInfos();
        } catch (ClassNotFoundException e) {
            LOG.warn("Class has not been found, will not generate documentation for this unit", e);
            return Collections.emptyList();
        }

        unitLoader.callInit(reflectionObject);

        // Creating object to hold all the data we have collected.
        TranslationUnitMetadata metadata = provideMetadataObject(context, unitCollector);
        TranslationUnitMetadataHandler metadataHandler = new TranslationUnitMetadataHandler(metadata);

        YangDocsWriter yangDocsWriter = new YangDocsWriter();

        return Collections.singleton(yangDocsWriter.writeOut(outputBaseDir, metadataHandler, context));
    }

    @Override
    public void setAdditionalConfig(Map<String, String> additionalConfiguration) {
        // no additional config utilized
    }

    @Override
    public void setResourceBaseDir(File resourceBaseDir) {
        // no resource processing necessary
    }

    /**
     * This method is used to create TranslationUnitMetadata class
     *
     * <p>
     * It calls provideHandlers on the reflection and prepares the codec for CodecTranslator.
     */
    private TranslationUnitMetadata provideMetadataObject(SchemaContext context, CliUnitCollector unitCollector) {

        TranslateUnit translateUnitObject = (TranslateUnit) reflectionObject;

        CapturingReaderRegistryBuilder readerRegistryBuilder = new CapturingReaderRegistryBuilder();
        CapturingWriterRegistryBuilder writerRegistryBuilder = new CapturingWriterRegistryBuilder();
        TranslateUnit.Context transportContext = Context.NOOP_CONTEXT;

        translateUnitObject.provideHandlers(readerRegistryBuilder, writerRegistryBuilder, transportContext);

        Set<InstanceIdentifier<?>> implementedReaders = readerRegistryBuilder.getReaders();
        Set<InstanceIdentifier<?>> implementedWriters = writerRegistryBuilder.getWriters();

        ModuleInfoBackedContext mib = CodecTranslator.getModuleInfoBackedContext(yangModuleInfos);
        BindingToNormalizedNodeCodec codec = CodecTranslator.getCodec(mib, context);

        // Creating object to hold all the data we have collected.
        return new TranslationUnitMetadata(unitCollector,
                implementedWriters,
                implementedReaders,
                reflectionObject.getClass(),
                context,
                codec, readerRegistryBuilder,
                writerRegistryBuilder);
    }

    @Override
    public void setBuildContext(BuildContext buildContext) {
        // build context is not utilized
    }

    @Override
    public void setMavenProject(MavenProject mavenProject) {
        this.project = mavenProject;
    }
}
