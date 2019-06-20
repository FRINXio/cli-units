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

package io.frinx.cli.unit.iosxr.lacp;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.iosxr.init.IosXrDevices;
import io.frinx.cli.unit.iosxr.lacp.handler.BundleConfigReader;
import io.frinx.cli.unit.iosxr.lacp.handler.BundleConfigWriter;
import io.frinx.cli.unit.iosxr.lacp.handler.BundleReader;
import io.frinx.cli.unit.iosxr.lacp.handler.MemberConfigReader;
import io.frinx.cli.unit.iosxr.lacp.handler.MemberConfigWriter;
import io.frinx.cli.unit.iosxr.lacp.handler.MemberReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.lacp.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class IosXRLacpUnit extends AbstractUnit {

    public IosXRLacpUnit(@Nonnull final TranslationUnitCollector translationRegistry) {
        super(translationRegistry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return IosXrDevices.IOS_XR_ALL;
    }

    @Override
    protected String getUnitName() {
        return "IOS XR LACP (Openconfig) translation unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_OPENCONFIG_LACP);
    }

    @Override
    public void provideHandlers(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull CustomizerAwareWriteRegistryBuilder writeRegistry, @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
       // LACP root
        writeRegistry.addNoop(IIDs.LACP);

        // bundle interface
        writeRegistry.addNoop(IIDs.LA_INTERFACES);
        writeRegistry.addNoop(IIDs.LA_IN_INTERFACE);
        writeRegistry.add(IIDs.LA_IN_IN_CONFIG, new BundleConfigWriter(cli));

        // member's interface
        writeRegistry.addNoop(IIDs.LA_IN_IN_MEMBERS);
        writeRegistry.addNoop(IIDs.LA_IN_IN_ME_MEMBER);
        writeRegistry.add(IIDs.LA_IN_IN_ME_ME_CONFIG, new MemberConfigWriter(cli));
    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {

        // bundle interface
        readRegistry.add(IIDs.LA_IN_INTERFACE, new BundleReader(cli));
        readRegistry.add(IIDs.LA_IN_IN_CONFIG, new BundleConfigReader(cli));

        // member's interface
        readRegistry.add(IIDs.LA_IN_IN_ME_MEMBER, new MemberReader(cli));
        readRegistry.add(IIDs.LA_IN_IN_ME_ME_CONFIG, new MemberConfigReader(cli));
    }
}
