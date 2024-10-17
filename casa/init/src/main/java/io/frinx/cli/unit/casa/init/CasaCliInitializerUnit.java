/*
 * Copyright © 2024 Frinx and others.
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

package io.frinx.cli.unit.casa.init;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Session;
import io.frinx.cli.io.SessionException;
import io.frinx.cli.io.SessionInitializationStrategy;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.topology.RemoteDeviceId;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CasaCliInitializerUnit extends AbstractUnit {

    private static final Logger LOG = LoggerFactory.getLogger(CasaCliInitializerUnit.class);

    public CasaCliInitializerUnit(@NotNull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return Collections.singleton(CasaDevices.CASA_GENERIC);
    }

    @Override
    protected String getUnitName() {
        return "CASA cli init (FRINX) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Collections.emptySet();
    }

    @Override
    public SessionInitializationStrategy getInitializer(@NotNull final RemoteDeviceId id,
                                                        @NotNull final CliNode cliNodeConfiguration) {
        return new CasaCliInitializer(cliNodeConfiguration, id);
    }

    @Override
    public void provideHandlers(@NotNull final CustomizerAwareReadRegistryBuilder readRegistry,
                                @NotNull final CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @NotNull final TranslateUnit.Context context) {
        readRegistry.addCheckRegistry(ChecksMap.OPENCONFIG_REGISTRY);
        writeRegistry.addCheckRegistry(ChecksMap.OPENCONFIG_REGISTRY);
    }

    @Override
    public Set<Pattern> getErrorPatterns() {
        return Sets.newLinkedHashSet(Arrays.asList(
                Pattern.compile("(^|\\n)^\\s+\\^.*", Pattern.DOTALL),
                Pattern.compile("(^|\\n)% (?i)invalid input(?-i).*", Pattern.DOTALL),
                Pattern.compile("(^|\\n)% (?i)Incomplete command(?-i).*", Pattern.DOTALL),
                Pattern.compile("% .* overlaps with \\w")
        ));
    }

    /**
     * Initialize CASA CLI session to be usable by various CRUD and RPC handlers.
     */
    public static class CasaCliInitializer implements SessionInitializationStrategy {

        private static final String OS_NAME_ID = "CASA";
        private static final String SET_PAGE_OFF_COMMAND = "page off";

        private final CliNode context;
        private final RemoteDeviceId id;

        public CasaCliInitializer(CliNode context, RemoteDeviceId id) {
            this.context = context;
            this.id = id;
        }

        @Override
        public void accept(@NotNull Session session, @NotNull String newline) {
            try {
                // Set page off to prevent ":" situation
                LOG.debug("{}: Setting page off to prevent \":\" situation", id);
                write(session, newline, SET_PAGE_OFF_COMMAND);

                LOG.info("{}: {} cli session initialized successfully", id, OS_NAME_ID);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } catch (SessionException | ExecutionException | TimeoutException e) {
                LOG.warn("{}: Unable to initialize device", id, e);
                throw new IllegalStateException(id + ": Unable to initialize device", e);
            }
        }
    }
}