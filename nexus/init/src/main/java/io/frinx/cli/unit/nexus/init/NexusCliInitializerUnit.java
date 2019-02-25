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

package io.frinx.cli.unit.nexus.init;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.SessionInitializationStrategy;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.topology.RemoteDeviceId;
import io.frinx.cli.unit.ios.init.IosCliInitializerUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Translate unit that does not actually translate anything.
 * This translate unit's only responsibility is to properly initialize IOS cli
 * session. That is, upon establishing connection to IOS device, enter privileged
 * EXEC mode by issuing the 'enable' command and filling in the secret.
 */
public class NexusCliInitializerUnit  implements TranslateUnit {

    private static final Logger LOG = LoggerFactory.getLogger(NexusCliInitializerUnit.class);

    // TODO This is reused all over the units. Move this to som Util class so
    // we can reuse it.

    private static final Device IOS = new DeviceIdBuilder()
            .setDeviceType("nexus")
            .setDeviceVersion("*")
            .build();

    private TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration iosReg;


    public NexusCliInitializerUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        iosReg = registry.registerTranslateUnit(IOS, this);
    }

    public void close() {
        if (iosReg != null) {
            iosReg.close();
        }
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Collections.emptySet();
    }

    @Override
    public SessionInitializationStrategy getInitializer(@Nonnull final RemoteDeviceId id,
                                                        @Nonnull final CliNode cliNodeConfiguration) {
        return new IosCliInitializerUnit.IosCliInitializer(cliNodeConfiguration, id) {
            @Override
            protected String getOsNameForLogging() {
                return "NEXUS";
            }
        };
    }

    @Override
    public Set<RpcService<?, ?>> getRpcs(@Nonnull final TranslateUnit.Context context) {
        return Sets.newHashSet();
    }

    @Override
    public void provideHandlers(@Nonnull final CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull final CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull final TranslateUnit.Context context) {
        // NO-OP
    }

    @Override
    public Set<Pattern> getErrorPatterns() {
        return Sets.newLinkedHashSet(Arrays.asList(
                Pattern.compile("^\\s+\\^.*", Pattern.DOTALL),
                Pattern.compile("% (?i)invalid input(?-i).*", Pattern.DOTALL),
                Pattern.compile("% (?i)Incomplete command(?-i).*", Pattern.DOTALL)
        ));
    }

    @Override
    public String toString() {
        return "Nexus cli init (FRINX) translate unit";
    }

}

