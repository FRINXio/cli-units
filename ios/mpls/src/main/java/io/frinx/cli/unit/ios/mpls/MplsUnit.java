/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.ios.mpls;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.CheckRegistry;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.ios.init.IosDevices;
import io.frinx.cli.unit.ios.mpls.handler.LdpInterfaceConfigReader;
import io.frinx.cli.unit.ios.mpls.handler.LdpInterfaceConfigWriter;
import io.frinx.cli.unit.ios.mpls.handler.TunnelConfigReader;
import io.frinx.cli.unit.ios.mpls.handler.TunnelConfigWriter;
import io.frinx.cli.unit.ios.mpls.handler.TunnelReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.common.rev131028.$YangModuleInfoImpl;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;


public class MplsUnit extends AbstractUnit {

    public MplsUnit(@Nonnull TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return IosDevices.IOS_ALL;
    }

    @Override
    protected String getUnitName() {
        return "IOS MPLS (Openconfig) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet($YangModuleInfoImpl.getInstance(),
                IIDs.FRINX_MPLS_RSVP_EXTENSION,
                IIDs.FRINX_CISCO_MPLS_TE_EXTENSION,
                io.frinx.openconfig.openconfig.mpls.IIDs.FRINX_OPENCONFIG_MPLS_LDP,
                IIDs.FRINX_MPLS_LDP_EXTENSION);
    }

    private static final CheckRegistry CHECK_REGISTRY;

    static {
        CheckRegistry.Builder builder = new CheckRegistry.Builder();
        builder.add(IIDs.NE_NE_MPLS,
                BasicCheck.checkData(ChecksMap.DataCheck.NetworkInstanceConfig.IID_TRANSFORMATION,
                        ChecksMap.DataCheck.NetworkInstanceConfig.TYPE_DEFAULTINSTANCE));
        CHECK_REGISTRY = builder.build();
    }

    @Override
    public void provideHandlers(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();
        readRegistry.addCheckRegistry(CHECK_REGISTRY);
        provideReaders(readRegistry,cli);
        writeRegistry.addCheckRegistry(CHECK_REGISTRY);
        provideWriters(writeRegistry,cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.addNoop(IIDs.NE_NE_MPLS);

        // LDP
        writeRegistry.addNoop(IIDs.NE_NE_MP_SI_LDP);
        writeRegistry.addNoop(IIDs.NE_NE_MP_SI_LD_GLOBAL);
        writeRegistry.addNoop(IIDs.NE_NE_MP_SI_LD_GL_CONFIG);
        writeRegistry.addNoop(IIDs.NE_NE_MP_SI_LD_INTERFACEATTRIBUTES);
        writeRegistry.addNoop(IIDs.NE_NE_MP_SI_LD_IN_INTERFACES);
        writeRegistry.addNoop(IIDs.NE_NE_MP_SI_LD_IN_IN_INTERFACE);
        writeRegistry.addAfter(IIDs.NE_NE_MP_SI_LD_IN_IN_IN_CONFIG,
                new LdpInterfaceConfigWriter(cli),IIDs.NE_NE_MP_SI_LD_GL_CO_AUG_NIMPLSLDPGLOBALAUG);

        // Tunnel
        writeRegistry.addNoop(IIDs.NE_NE_MP_LSPS);
        writeRegistry.addNoop(IIDs.NE_NE_MP_LS_CONSTRAINEDPATH);
        writeRegistry.addNoop(IIDs.NE_NE_MP_LS_CO_TU_TUNNEL);
        writeRegistry.add(IIDs.NE_NE_MP_LS_CO_TU_TU_CONFIG, new TunnelConfigWriter(cli));
    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        // LDP
        readRegistry.add(IIDs.NE_NE_MP_SI_LD_IN_IN_IN_CONFIG, new LdpInterfaceConfigReader());

        // Tunnel
        readRegistry.add(IIDs.NE_NE_MP_LS_CO_TU_TUNNEL, new TunnelReader(cli));
        readRegistry.add(IIDs.NE_NE_MP_LS_CO_TU_TU_CONFIG, new TunnelConfigReader(cli));
    }
}
