/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.mikrotik.init;

import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.CliFlavour;
import io.frinx.cli.io.Session;
import io.frinx.cli.io.SessionInitializationStrategy;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.topology.RemoteDeviceId;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MikrotikCliInitializerUnit extends AbstractUnit {

    private static final Logger LOG = LoggerFactory.getLogger(MikrotikCliInitializerUnit.class);

    public MikrotikCliInitializerUnit(@Nonnull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return Collections.singleton(MikrotikDevices.MIKROTIK_GENERIC);
    }

    @Override
    protected String getUnitName() {
        return "Mikrotik cli init (FRINX) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Collections.emptySet();
    }

    @Override
    public SessionInitializationStrategy getInitializer(@Nonnull final RemoteDeviceId id,
                                                        @Nonnull final CliNode cliNodeConfiguration) {
        return new MikrotikCliInitializer(cliNodeConfiguration, id);
    }

    @Override
    public void provideHandlers(@Nonnull final CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull final CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull final TranslateUnit.Context context) {
        readRegistry.addCheckRegistry(ChecksMap.OPENCONFIG_REGISTRY);
        writeRegistry.addCheckRegistry(ChecksMap.OPENCONFIG_REGISTRY);
    }

    @Override
    public Set<Pattern> getErrorPatterns() {
        return Collections.emptySet();
    }

    @Override
    public CliFlavour getCliFlavour() {
        return new CliFlavour(
                Pattern.compile("^UNSUPPORTED$"),
                " ",
                "UNSUPPORTED",
                "UNSUPPORTED",
                Pattern.compile("\\|"),
                Collections.emptySet(),
                "UNSUPPORTED",
                Collections.emptyList(),
                "",
                // Mikrotik uses \r internally (unlike \n used by everyone)
                "\r",
                // This sets no-color mode and turns off auto detection
                //  basically without these options, Mikrotik's CLI cannot be talked to
                //  https://wiki.mikrotik.com/wiki/Manual:Console_login_process
                // tested with version 5.25, 6.45 and 7.0
                "+tc",
                Collections.emptySet());
    }

    public static class MikrotikCliInitializer implements SessionInitializationStrategy {

        private final CliNode context;
        private final RemoteDeviceId id;

        public MikrotikCliInitializer(CliNode context, RemoteDeviceId id) {
            this.context = context;
            this.id = id;
        }

        @Override
        public void accept(@Nonnull Session session, @Nonnull String newline) {
            // nothing so far
        }
    }
}

