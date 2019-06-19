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

package io.frinx.cli.unit.iosxr.bfd;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.iosxr.bfd.handler.ConfigReader;
import io.frinx.cli.unit.iosxr.bfd.handler.ConfigWriter;
import io.frinx.cli.unit.iosxr.bfd.handler.InterfaceReader;
import io.frinx.cli.unit.iosxr.init.IosXrDevices;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.bfd.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class IosXRBfdUnit extends AbstractUnit {

    public IosXRBfdUnit(@Nonnull final TranslationUnitCollector translationRegistry) {
        super(translationRegistry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return IosXrDevices.IOS_XR_ALL;
    }

    @Override
    protected String getUnitName() {
        return "IOS XR BFD (Openconfig) translation unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_OPENCONFIG_BFD,
                IIDs.FRINX_BFD_EXTENSION);
    }

    @Override
    public void provideHandlers(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull CustomizerAwareWriteRegistryBuilder writeRegistry, @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.addNoop(IIDs.BFD);
        writeRegistry.addNoop(IIDs.BF_INTERFACES);
        writeRegistry.addNoop(IIDs.BF_IN_INTERFACE);
        writeRegistry.subtreeAdd(IIDs.BF_IN_IN_CONFIG, new ConfigWriter(cli),
                Sets.newHashSet(IIDs.BF_IN_IN_CO_AUG_IFBFDEXTAUG));
    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.BF_IN_INTERFACE, new InterfaceReader(cli));
        readRegistry.subtreeAdd(IIDs.BF_IN_IN_CONFIG, new ConfigReader(cli),
                Sets.newHashSet(IIDs.BF_IN_IN_CO_AUG_IFBFDEXTAUG));
    }
}
