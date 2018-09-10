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

package io.frinx.binding.ids;

import io.frinx.cli.utils.CapturingReaderRegistryBuilder;
import io.frinx.cli.utils.CapturingWriterRegistryBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.opendaylight.controller.md.sal.binding.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;


/**
 * This class holds all the information we have about unit. Its only responsibility is to hold all data and should
 * contain only simple get() method. All complex operations above data is one in TranslationUnitMetadataHandler.
 */
public class TranslationUnitMetadata {

    private final Set<InstanceIdentifier<?>> writersSet;
    private final Set<InstanceIdentifier<?>> readersSet;
    private final Class<?> classObject;
    private final UnitCollectorHandler unitCollectorHandler;
    private final CapturingReaderRegistryBuilder readerRegistryBuilder;
    private final CapturingWriterRegistryBuilder writerRegistryBuilder;
    private final CodecTranslator codecTranslator;

    public TranslationUnitMetadata(CliUnitCollector cliUnitCollector, Set<InstanceIdentifier<?>> writersSet,
                                   Set<InstanceIdentifier<?>> readersSet, Class<?> classObject, SchemaContext context,
                                   BindingToNormalizedNodeCodec bindingCodec,
                                   CapturingReaderRegistryBuilder readerRegistryBuilder,
                                   CapturingWriterRegistryBuilder writerRegistryBuilder
    ) {

        this.classObject = classObject;
        this.readersSet = readersSet;
        this.writersSet = writersSet;
        this.unitCollectorHandler = new UnitCollectorHandler(cliUnitCollector);
        this.readerRegistryBuilder = readerRegistryBuilder;
        this.writerRegistryBuilder = writerRegistryBuilder;
        this.codecTranslator = new CodecTranslator(bindingCodec, context);

    }

    public TranslationUnitMetadata(UnitopoTranslationUnitCollector unitopoTranslationUnitCollector,
                                   Set<InstanceIdentifier<?>> writersSet,
                                   Set<InstanceIdentifier<?>> readersSet,
                                   Class<?> classObject, SchemaContext context,
                                   BindingToNormalizedNodeCodec bindingCodec,
                                   CapturingReaderRegistryBuilder readerRegistryBuilder,
                                   CapturingWriterRegistryBuilder writerRegistryBuilder
    ) {
        this.classObject = classObject;
        this.readersSet = readersSet;
        this.writersSet = writersSet;
        this.unitCollectorHandler = new UnitCollectorHandler(unitopoTranslationUnitCollector);
        this.readerRegistryBuilder = readerRegistryBuilder;
        this.writerRegistryBuilder = writerRegistryBuilder;
        this.codecTranslator = new CodecTranslator(bindingCodec, context);

    }


    public Set<InstanceIdentifier<?>> getReadersSet() {
        return readersSet;
    }

    public Stream<String> getReadersAsStrings() {
        return getReadersSet().stream()
                .map(codecTranslator::toBindingIndependent)
                .filter(Objects::nonNull)
                .map(codecTranslator::toStringId);
    }

    public Stream<String> getWritersAsStrings() {
        return getWritersSet().stream()
                .map(codecTranslator::toBindingIndependent)
                .filter(Objects::nonNull)
                .map(codecTranslator::toStringId);
    }

    public CapturingWriterRegistryBuilder getWriterRegistryBuilder() {
        return writerRegistryBuilder;
    }

    public CapturingReaderRegistryBuilder getReaderRegistryBuilder() {
        return readerRegistryBuilder;
    }

    public Set<InstanceIdentifier<?>> getWritersSet() {
        return writersSet;
    }

    public List<String> getDevicesVersion() {
        if (unitCollectorHandler.isUnitopo()) {
            return Stream.of(parseUnitopoUnitDeviceVersion()).collect(Collectors.toList());
        } else {
            return unitCollectorHandler
                    .getCliUnitCollector()
                    .getDevicesIds()
                    .stream()
                    .map(Device::getDeviceVersion)
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    public String getDeviceType() {
        if (unitCollectorHandler.isUnitopo()) {
            return parseUnitopoUnitDeviceType();
        } else {
            return unitCollectorHandler
                    .getCliUnitCollector()
                    .getDevicesIds()
                    .stream()
                    .map(Device::getDeviceType)
                    .distinct()
                    .map(type -> type.replace(" ", "-"))
                    .collect(Collectors.joining("-"));
        }
    }

    public String getName() {
        return classObject.getName().toLowerCase();
    }

    public String getSimpleName() {
        if (unitCollectorHandler.isUnitopo()) {
            return parseUnitopoUnitSimpleName().toLowerCase();
        }
        return classObject.getSimpleName().toLowerCase();
    }

    private String parseUnitopoUnitDeviceType() {
        return classObject
                .getName()
                .replace("io.frinx.unitopo.unit.", "")
                .replace(".Unit", "")
                .split("\\.")[0]
                .split("[0-9]+", 2)[0];
    }

    private String parseUnitopoUnitSimpleName() {
        return classObject
                .getName()
                .replace("io.frinx.unitopo.unit.", "")
                .replace(".Unit", "")
                .split("\\.")[1]
                .concat("unit");
    }

    private String parseUnitopoUnitDeviceVersion() {
        return unitCollectorHandler.getUnitopoUnitCollector()
                .getUnit()
                .toString()
                .split(" ")[1];
    }
}
