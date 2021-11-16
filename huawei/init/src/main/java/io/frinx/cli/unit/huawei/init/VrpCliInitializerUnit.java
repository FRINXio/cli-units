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

package io.frinx.cli.unit.huawei.init;

import com.google.common.collect.ImmutableList;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.CliFlavour;
import io.frinx.cli.io.OutputFunction;
import io.frinx.cli.io.Session;
import io.frinx.cli.io.SessionException;
import io.frinx.cli.io.SessionInitializationStrategy;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.topology.RemoteDeviceId;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VrpCliInitializerUnit extends AbstractUnit {

    private static final Logger LOG = LoggerFactory.getLogger(VrpCliInitializerUnit.class);

    private static final Device HUAWEI = new DeviceIdBuilder()
            .setDeviceType("vrp")
            .setDeviceVersion("*")
            .build();

    public VrpCliInitializerUnit(@Nonnull TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Collections.emptySet();
    }

    @Override
    public SessionInitializationStrategy getInitializer(@Nonnull final RemoteDeviceId id,
                                                        @Nonnull final CliNode cliNodeConfiguration) {
        return new VrpCliInitializer(id);
    }

    @Override
    public void provideHandlers(@Nonnull final CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull final CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull final TranslateUnit.Context context) {
        readRegistry.addCheckRegistry(ChecksMap.OPENCONFIG_REGISTRY);
        writeRegistry.addCheckRegistry(ChecksMap.OPENCONFIG_REGISTRY);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return Collections.singleton(HUAWEI);
    }

    @Override
    protected String getUnitName() {
        return "VRP cli init (FRINX) translate unit";
    }

    public static final class VrpCliInitializer implements SessionInitializationStrategy {
        private static final String SET_TERMINAL_LENGTH_COMMAND = "screen-length 0 temporary";
        private static final int READ_TIMEOUT_SECONDS = 1;

        private final RemoteDeviceId id;

        public VrpCliInitializer(RemoteDeviceId id) {
            this.id = id;
        }

        @Override
        public void accept(@Nonnull Session session, @Nonnull String newline) {
            try {

                // Set terminal length to 0 to prevent "--More--" situation
                LOG.debug("{}: Setting terminal length to 0 to prevent \"--More--\" situation", id);
                String ss = session.readUntilTimeout(READ_TIMEOUT_SECONDS);
                write(session, newline, SET_TERMINAL_LENGTH_COMMAND);
                ss += session.readUntilTimeout(READ_TIMEOUT_SECONDS);

                LOG.info("{}: VRP cli session initialized successfully", id);
                LOG.debug("{}: VRP cli session initialization output: {}", id, ss);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } catch (SessionException | ExecutionException | TimeoutException e) {
                LOG.warn("{}: Unable to initialize device", id, e);
                throw new IllegalStateException(id + ": Unable to initialize device", e);
            }
        }
    }

    public CliFlavour getCliFlavour() {
        return new CliFlavour(
                Pattern.compile("display current-configuration (configuration)?"),
                " ",
                "",
                "#",
                Pattern.compile("\\|"),
                OutputFunction.ALL,
                "#",
                ImmutableList.of(),
                "",
                Cli.NEWLINE,
                "",
                Collections.emptySet());
    }


}
