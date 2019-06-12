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

package io.frinx.cli.iosxr.hsrp;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.hsrp.handler.HsrpGroupConfigReader;
import io.frinx.cli.iosxr.hsrp.handler.HsrpGroupConfigWriter;
import io.frinx.cli.iosxr.hsrp.handler.HsrpGroupReader;
import io.frinx.cli.iosxr.hsrp.handler.HsrpInterfaceConfigReader;
import io.frinx.cli.iosxr.hsrp.handler.HsrpInterfaceConfigWriter;
import io.frinx.cli.iosxr.hsrp.handler.HsrpInterfaceReader;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.iosxr.init.IosXrDevices;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.hsrp.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class HsrpUnit extends AbstractUnit {

    public HsrpUnit(@Nonnull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return IosXrDevices.IOS_XR_ALL;
    }

    @Override
    protected String getUnitName() {
        return "IOS XR Hsrp unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814.$YangModuleInfoImpl
                        .getInstance());
    }

    @Override
    public void provideHandlers(@Nonnull final CustomizerAwareReadRegistryBuilder readRegistry,
            @Nonnull final CustomizerAwareWriteRegistryBuilder writeRegistry, @Nonnull final Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.addNoop(IIDs.HSRP);
        writeRegistry.addNoop(IIDs.HS_INTERFACES);
        writeRegistry.addNoop(IIDs.HS_IN_INTERFACE);
        writeRegistry.add(IIDs.HS_IN_IN_CONFIG, new HsrpInterfaceConfigWriter(cli));

        writeRegistry.addNoop(IIDs.HS_IN_IN_HSRPGROUP);
        writeRegistry.add(IIDs.HS_IN_IN_HS_CONFIG, new HsrpGroupConfigWriter(cli));
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.HS_IN_INTERFACE, new HsrpInterfaceReader(cli));
        readRegistry.add(IIDs.HS_IN_IN_CONFIG, new HsrpInterfaceConfigReader(cli));

        readRegistry.add(IIDs.HS_IN_IN_HSRPGROUP, new HsrpGroupReader(cli));
        readRegistry.add(IIDs.HS_IN_IN_HS_CONFIG, new HsrpGroupConfigReader(cli));
    }
}
