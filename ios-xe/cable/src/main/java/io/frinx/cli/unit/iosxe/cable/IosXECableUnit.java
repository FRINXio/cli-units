/*
 * Copyright © 2022 Frinx and others.
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
package io.frinx.cli.unit.iosxe.cable;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.iosxe.cable.handler.downstream.CableDownstreamConfigReader;
import io.frinx.cli.unit.iosxe.cable.handler.downstream.CableDownstreamReader;
import io.frinx.cli.unit.iosxe.cable.handler.downstream.DownstreamRfChannelConfigReader;
import io.frinx.cli.unit.iosxe.cable.handler.downstream.DownstreamRfChannelReader;
import io.frinx.cli.unit.iosxe.cable.handler.downstream.RfChannelOfdmConfigReader;
import io.frinx.cli.unit.iosxe.cable.handler.downstream.RfChannelTypeConfigReader;
import io.frinx.cli.unit.iosxe.cable.handler.fiber.node.CableChannelConfigReader;
import io.frinx.cli.unit.iosxe.cable.handler.fiber.node.CableChannelConfigWriter;
import io.frinx.cli.unit.iosxe.cable.handler.fiber.node.CableChannelReader;
import io.frinx.cli.unit.iosxe.cable.handler.fiber.node.FiberNodeConfigReader;
import io.frinx.cli.unit.iosxe.cable.handler.fiber.node.FiberNodeReader;
import io.frinx.cli.unit.iosxe.cable.handler.fiber.node.FiberNodeWriter;
import io.frinx.cli.unit.iosxe.cable.handler.rpd.CableRpdConfigReader;
import io.frinx.cli.unit.iosxe.cable.handler.rpd.CableRpdConfigWriter;
import io.frinx.cli.unit.iosxe.cable.handler.rpd.CableRpdCoreInterfaceConfigReader;
import io.frinx.cli.unit.iosxe.cable.handler.rpd.CableRpdCoreInterfaceConfigWriter;
import io.frinx.cli.unit.iosxe.cable.handler.rpd.CableRpdDownstreamConfigReader;
import io.frinx.cli.unit.iosxe.cable.handler.rpd.CableRpdDownstreamConfigWriter;
import io.frinx.cli.unit.iosxe.cable.handler.rpd.CableRpdDownstreamReader;
import io.frinx.cli.unit.iosxe.cable.handler.rpd.CableRpdReader;
import io.frinx.cli.unit.iosxe.cable.handler.rpd.CableRpdUpstreamConfigReader;
import io.frinx.cli.unit.iosxe.cable.handler.rpd.CableRpdUpstreamConfigWriter;
import io.frinx.cli.unit.iosxe.cable.handler.rpd.CableRpdUpstreamReader;
import io.frinx.cli.unit.iosxe.cable.handler.rpd.CoreInterfaceDownstreamConfigReader;
import io.frinx.cli.unit.iosxe.cable.handler.rpd.CoreInterfaceDownstreamReader;
import io.frinx.cli.unit.iosxe.cable.handler.rpd.CoreInterfaceUpstreamConfigReader;
import io.frinx.cli.unit.iosxe.cable.handler.rpd.CoreInterfaceUpstreamReader;
import io.frinx.cli.unit.iosxe.init.IosXeDevices;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.cable.IIDs;
import java.util.Collections;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class IosXECableUnit extends AbstractUnit {
    public IosXECableUnit(@NotNull final TranslationUnitCollector translationRegistry) {
        super(translationRegistry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return Collections.singleton(IosXeDevices.IOS_XE_16);
    }

    @Override
    protected String getUnitName() {
        return "IOS XE Cable (Openconfig) translation unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_OPENCONFIG_CABLE);
    }

    @Override
    public void provideHandlers(@NotNull CustomizerAwareReadRegistryBuilder readRegistry,
                                @NotNull CustomizerAwareWriteRegistryBuilder writeRegistry, @NotNull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.addNoop(IIDs.CABLE);
        writeRegistry.addNoop(IIDs.CA_FIBERNODES);
        writeRegistry.addNoop(IIDs.CA_FI_FIBERNODE);
        writeRegistry.add(IIDs.CA_FI_FI_CONFIG, new FiberNodeWriter(cli));
        writeRegistry.addNoop(IIDs.CA_FI_FI_CABLECHANNELS);
        writeRegistry.addNoop(IIDs.CA_FI_FI_CA_CABLECHANNEL);
        writeRegistry.add(IIDs.CA_FI_FI_CA_CA_CONFIG, new CableChannelConfigWriter(cli));

        writeRegistry.addNoop(IIDs.CA_RPDS);
        writeRegistry.addNoop(IIDs.CA_RP_RPD);
        writeRegistry.add(IIDs.CA_RP_RP_CONFIG, new CableRpdConfigWriter(cli));
        writeRegistry.subtreeAddAfter(IIDs.CA_RP_RP_COREINTERFACE, new CableRpdCoreInterfaceConfigWriter(cli),
                Sets.newHashSet(
                        IIDs.CA_RP_RP_CO_CONFIG,
                        IIDs.CA_RP_RP_CO_IF_DOWNSTREAMPORTS,
                        IIDs.CA_RP_RP_CO_IF_DO_CONFIG,
                        IIDs.CA_RP_RP_CO_IF_UPSTREAMPORTS,
                        IIDs.CA_RP_RP_CO_IF_UP_CONFIG));
        writeRegistry.addNoop(IIDs.CA_RP_RP_RPDDS);
        writeRegistry.addNoop(IIDs.CA_RP_RP_RP_DOWNSTREAMCOMMANDS);
        writeRegistry.add(IIDs.CA_RP_RP_RP_DO_CONFIG, new CableRpdDownstreamConfigWriter(cli));
        writeRegistry.addNoop(IIDs.CA_RP_RP_RPDUS);
        writeRegistry.addNoop(IIDs.CA_RP_RP_RP_UPSTREAMCOMMANDS);
        writeRegistry.add(IIDs.CA_RP_RP_RP_UP_CONFIG, new CableRpdUpstreamConfigWriter(cli));

        writeRegistry.addNoop(IIDs.CA_DOWNSTREAMS);
        writeRegistry.addNoop(IIDs.CA_DO_DOWNSTREAMCABLEPROFILE);
        writeRegistry.addNoop(IIDs.CA_DO_DO_CONFIG);
        writeRegistry.addNoop(IIDs.CA_DO_DO_RFCHANNELS);
        writeRegistry.addNoop(IIDs.CA_DO_DO_RF_RFCHANNEL);
        writeRegistry.addNoop(IIDs.CA_DO_DO_RF_RF_OFDM);
        writeRegistry.addNoop(IIDs.CA_DO_DO_RF_RF_RFCHANTYPE);
        writeRegistry.addNoop(IIDs.CA_DO_DO_RF_RF_CONFIG);
        writeRegistry.addNoop(IIDs.CA_DO_DO_RF_RF_RF_CONFIG);
        writeRegistry.addNoop(IIDs.CA_DO_DO_RF_RF_OF_CONFIG);
    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.CA_FI_FIBERNODE, new FiberNodeReader(cli));
        readRegistry.add(IIDs.CA_FI_FI_CONFIG, new FiberNodeConfigReader(cli));
        readRegistry.add(IIDs.CA_FI_FI_CA_CABLECHANNEL, new CableChannelReader(cli));
        readRegistry.add(IIDs.CA_FI_FI_CA_CA_CONFIG, new CableChannelConfigReader(cli));

        readRegistry.add(IIDs.CA_RP_RPD, new CableRpdReader(cli));
        readRegistry.add(IIDs.CA_RP_RP_CONFIG, new CableRpdConfigReader(cli));
        readRegistry.add(IIDs.CA_RP_RP_RP_DOWNSTREAMCOMMANDS, new CableRpdDownstreamReader(cli));
        readRegistry.add(IIDs.CA_RP_RP_RP_UPSTREAMCOMMANDS, new CableRpdUpstreamReader(cli));
        readRegistry.add(IIDs.CA_RP_RP_RP_DO_CONFIG, new CableRpdDownstreamConfigReader(cli));
        readRegistry.add(IIDs.CA_RP_RP_RP_UP_CONFIG, new CableRpdUpstreamConfigReader(cli));

        readRegistry.add(IIDs.CA_RP_RP_COREINTERFACE, new CableRpdCoreInterfaceConfigReader(cli));
        readRegistry.add(IIDs.CA_RP_RP_CO_IF_UPSTREAMPORTS, new CoreInterfaceUpstreamReader(cli));
        readRegistry.add(IIDs.CA_RP_RP_CO_IF_UP_CONFIG, new CoreInterfaceUpstreamConfigReader(cli));
        readRegistry.add(IIDs.CA_RP_RP_CO_IF_DOWNSTREAMPORTS, new CoreInterfaceDownstreamReader(cli));
        readRegistry.add(IIDs.CA_RP_RP_CO_IF_DO_CONFIG, new CoreInterfaceDownstreamConfigReader(cli));

        readRegistry.add(IIDs.CA_DO_DOWNSTREAMCABLEPROFILE, new CableDownstreamReader(cli));
        readRegistry.add(IIDs.CA_DO_DO_CONFIG, new CableDownstreamConfigReader(cli));
        readRegistry.add(IIDs.CA_DO_DO_RF_RF_CONFIG, new DownstreamRfChannelConfigReader(cli));
        readRegistry.add(IIDs.CA_DO_DO_RF_RFCHANNEL, new DownstreamRfChannelReader(cli));
        readRegistry.add(IIDs.CA_DO_DO_RF_RF_OF_CONFIG, new RfChannelOfdmConfigReader(cli));
        readRegistry.add(IIDs.CA_DO_DO_RF_RF_RF_CONFIG, new RfChannelTypeConfigReader(cli));
    }
}