/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.iosxe.bfd;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.iosxe.bfd.handler.BfdTemplateConfigReader;
import io.frinx.cli.unit.iosxe.bfd.handler.BfdTemplateConfigWriter;
import io.frinx.cli.unit.iosxe.bfd.handler.BfdTemplateReader;
import io.frinx.cli.unit.iosxe.bfd.handler.IntervalReader;
import io.frinx.cli.unit.iosxe.init.IosXeDevices;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.bfd.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class IosXEBfdUnit extends AbstractUnit {

    public IosXEBfdUnit(@Nonnull final TranslationUnitCollector translationRegistry) {
        super(translationRegistry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return IosXeDevices.IOS_XE_ALL;
    }

    @Override
    protected String getUnitName() {
        return "IOS XE BFD (Openconfig) translation unit";
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
        writeRegistry.addNoop(IIDs.BF_AUG_BFD1_BF_BFDTEMPLATE);
        writeRegistry.addNoop(IIDs.BF_INTERFACES);
        writeRegistry.addNoop(IIDs.BF_IN_INTERFACE);
        writeRegistry.subtreeAdd(IIDs.BF_AUG_BFDTEMPAUG, new BfdTemplateConfigWriter(cli),
                Sets.newHashSet(IIDs.BF_AUG_BFDTEMPAUG_BFDTEMPLATES,
                        IIDs.BF_AUG_BFDTEMPAUG_BF_BFDTEMPLATE,
                        IIDs.BF_AUG_BFDTEMPAUG_BF_BF_CONFIG,
                        IIDs.BF_AUG_BFDTEMPAUG_BF_BF_INTERVAL));

    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.BF_AUG_BFD1_BF_BFDTEMPLATE, new BfdTemplateReader(cli));
        readRegistry.add(IIDs.BF_AUG_BFD1_BF_BF_CONFIG, new BfdTemplateConfigReader(cli));
        readRegistry.add(IIDs.BF_AUG_BFD1_BF_BF_INTERVAL, new IntervalReader(cli));
    }
}
