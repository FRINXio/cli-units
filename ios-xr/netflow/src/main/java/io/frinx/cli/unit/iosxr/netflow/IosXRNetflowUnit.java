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

package io.frinx.cli.unit.iosxr.netflow;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.iosxr.init.IosXrDevices;
import io.frinx.cli.unit.iosxr.netflow.handler.EgressFlowConfigReader;
import io.frinx.cli.unit.iosxr.netflow.handler.EgressFlowConfigWriter;
import io.frinx.cli.unit.iosxr.netflow.handler.EgressFlowReader;
import io.frinx.cli.unit.iosxr.netflow.handler.IngressFlowConfigReader;
import io.frinx.cli.unit.iosxr.netflow.handler.IngressFlowConfigWriter;
import io.frinx.cli.unit.iosxr.netflow.handler.IngressFlowReader;
import io.frinx.cli.unit.iosxr.netflow.handler.NetflowInterfaceConfigReader;
import io.frinx.cli.unit.iosxr.netflow.handler.NetflowInterfaceReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.netflow.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public final class IosXRNetflowUnit extends AbstractUnit {

    public IosXRNetflowUnit(@Nonnull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return IosXrDevices.IOS_XR_ALL;
    }

    @Override
    protected String getUnitName() {
        return "IOS XR Netflow translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_NETFLOW);
    }

    @Override
    public void provideHandlers(@Nonnull final CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull final CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull final Context context) {
        Cli cli = context.getTransport();

        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(final CustomizerAwareWriteRegistryBuilder writeRegistry, final Cli cli) {
        writeRegistry.addNoop(IIDs.NE_INTERFACES);
        writeRegistry.addNoop(IIDs.NE_IN_INTERFACE);
        writeRegistry.addNoop(IIDs.NE_IN_IN_CONFIG);

        writeRegistry.addNoop(IIDs.NE_IN_IN_INGRESSFLOWS);
        writeRegistry.addNoop(IIDs.NE_IN_IN_IN_INGRESSFLOW);
        writeRegistry.add(IIDs.NE_IN_IN_IN_IN_CONFIG, new IngressFlowConfigWriter(cli));

        writeRegistry.addNoop(IIDs.NE_IN_IN_EGRESSFLOWS);
        writeRegistry.addNoop(IIDs.NE_IN_IN_EG_EGRESSFLOW);
        writeRegistry.add(IIDs.NE_IN_IN_EG_EG_CONFIG, new EgressFlowConfigWriter(cli));
    }

    private void provideReaders(final CustomizerAwareReadRegistryBuilder readRegistry, final Cli cli) {
        readRegistry.add(IIDs.NE_IN_INTERFACE, new NetflowInterfaceReader(cli));
        readRegistry.add(IIDs.NE_IN_IN_CONFIG, new NetflowInterfaceConfigReader());

        readRegistry.add(IIDs.NE_IN_IN_IN_INGRESSFLOW, new IngressFlowReader(cli));
        readRegistry.add(IIDs.NE_IN_IN_IN_IN_CONFIG, new IngressFlowConfigReader(cli));

        readRegistry.add(IIDs.NE_IN_IN_EG_EGRESSFLOW, new EgressFlowReader(cli));
        readRegistry.add(IIDs.NE_IN_IN_EG_EG_CONFIG, new EgressFlowConfigReader(cli));
    }
}
