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

import io.frinx.binding.ids.CodecTranslator;
import io.frinx.binding.ids.TranslationUnitMetadata;
import io.frinx.binding.ids.TranslationUnitMetadataHandler;
import io.frinx.binding.ids.UnitopoTranslationUnitCollector;
import io.frinx.binding.ids.YangDocsWriter;
import io.frinx.translate.unit.commons.utils.CapturingReaderRegistryBuilder;
import io.frinx.translate.unit.commons.utils.CapturingWriterRegistryBuilder;
import io.frinx.unitopo.registry.spi.TranslateUnit;
import io.frinx.unitopo.registry.spi.UnderlayAccess;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
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
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * This is maven-plugin endpoint class which manages the creation of YangDocs.
 */
public final class YangDocsGenerator implements BasicCodeGenerator, BuildContextAware, MavenProjectAware {

    private MavenProject project;
    private final Log log = new SystemStreamLog();
    private Set<YangModuleInfo> yangModuleInfos = Collections.emptySet();
    private Optional<Object> reflectionObject;

    @Override
    public Collection<File> generateSources(SchemaContext context, File outputBaseDir, Set<Module> currentModules)
            throws IOException {
        throw new UnsupportedOperationException("Deprecated method");
    }

    @Override
    public Collection<File> generateSources(SchemaContext context,
                                            File outputBaseDir,
                                            Set<Module> currentModules,
                                            Function<Module, Optional<String>> moduleResourcePathResolver)
            throws IOException {


        UnitopoTranslationUnitCollector unitCollector = new UnitopoTranslationUnitCollector();
        UnitLoader unitLoader = new UnitLoader(project);
        try {
            reflectionObject = unitLoader.getReflectionObject(unitCollector);
            yangModuleInfos = unitLoader.getYangModuleInfos();
        } catch (ClassNotFoundException e) {
            log.warn(e);
            return Collections.emptyList();
        }

        if (!reflectionObject.isPresent()) {
            return Collections.emptyList();
        }
        unitLoader.callInit(reflectionObject.get());

        //Creating object to hold all the data we have collected.
        TranslationUnitMetadata metadata = provideMetadataObject(context, unitCollector);
        TranslationUnitMetadataHandler metadataHandler = new TranslationUnitMetadataHandler(metadata);

        YangDocsWriter yangDocsWriter = new YangDocsWriter();

        return Collections.singleton(yangDocsWriter.writeOut(outputBaseDir, metadataHandler, context));
    }

    @Override
    public void setAdditionalConfig(Map<String, String> additionalConfiguration) {
    }

    @Override
    public void setResourceBaseDir(File resourceBaseDir) {
    }

    /**
     * This method is used to create TranslationUnitMetadata class
     *
     * <p>
     * It calls provideHandlers on the reflection and prepares the codec for CodecTranslator.
     */
    private TranslationUnitMetadata provideMetadataObject(SchemaContext context,
                                                          UnitopoTranslationUnitCollector unitCollector) {

        TranslateUnit translateUnitObject = (TranslateUnit) reflectionObject.get();

        CapturingReaderRegistryBuilder readerRegistryBuilder = new CapturingReaderRegistryBuilder();
        CapturingWriterRegistryBuilder writerRegistryBuilder = new CapturingWriterRegistryBuilder();
        UnderlayAccess transportContext = UnderlayAccess.NOOP;

        translateUnitObject.provideHandlers(readerRegistryBuilder, writerRegistryBuilder, transportContext);

        Set<InstanceIdentifier<?>> implementedReaders = readerRegistryBuilder.getReaders();
        Set<InstanceIdentifier<?>> implementedWriters = writerRegistryBuilder.getWriters();

        ModuleInfoBackedContext mib = CodecTranslator.getModuleInfoBackedContext(yangModuleInfos);
        BindingToNormalizedNodeCodec codec = CodecTranslator.getCodec(mib, context);

        //Creating object to hold all the data we have collected.
        return new TranslationUnitMetadata(unitCollector,
                implementedWriters,
                implementedReaders,
                reflectionObject.get().getClass(),
                context,
                codec, readerRegistryBuilder,
                writerRegistryBuilder);
    }

    @Override
    public void setBuildContext(BuildContext buildContext) {
    }

    @Override
    public void setMavenProject(MavenProject project) {
        this.project = project;
    }


}