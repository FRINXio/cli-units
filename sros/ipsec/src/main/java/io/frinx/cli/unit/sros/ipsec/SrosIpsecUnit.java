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

package io.frinx.cli.unit.sros.ipsec;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.sros.init.SrosDevices;
import io.frinx.cli.unit.sros.ipsec.handler.clientgroup.ClientConfigReader;
import io.frinx.cli.unit.sros.ipsec.handler.clientgroup.ClientConfigWriter;
import io.frinx.cli.unit.sros.ipsec.handler.clientgroup.ClientGroupConfigReader;
import io.frinx.cli.unit.sros.ipsec.handler.clientgroup.ClientGroupConfigWriter;
import io.frinx.cli.unit.sros.ipsec.handler.clientgroup.ClientGroupReader;
import io.frinx.cli.unit.sros.ipsec.handler.clientgroup.ClientIdentificationConfigReader;
import io.frinx.cli.unit.sros.ipsec.handler.clientgroup.ClientIdentificationConfigWriter;
import io.frinx.cli.unit.sros.ipsec.handler.clientgroup.ClientReader;
import io.frinx.cli.unit.sros.ipsec.handler.clientgroup.TunnelingConfigReader;
import io.frinx.cli.unit.sros.ipsec.handler.clientgroup.TunnelingConfigWriter;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.ipsec.IIDs;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class SrosIpsecUnit extends AbstractUnit {
    public SrosIpsecUnit(@NotNull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return SrosDevices.SROS_ALL;
    }

    @Override
    protected String getUnitName() {
        return "SROS IPsec unit";
    }

    @Override
    public void provideHandlers(
        @NotNull CustomizerAwareReadRegistryBuilder readRegistry,
        @NotNull CustomizerAwareWriteRegistryBuilder writeRegistry,
        @NotNull Context context) {

        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideReaders(@NotNull CustomizerAwareReadRegistryBuilder rreg, Cli cli) {
        rreg.add(IIDs.IP_CL_CLIENTGROUP, new ClientGroupReader(cli));
        rreg.add(IIDs.IP_CL_CL_CONFIG, new ClientGroupConfigReader());
        rreg.add(IIDs.IP_CL_CL_CL_CLIENT, new ClientReader(cli));
        rreg.add(IIDs.IP_CL_CL_CL_CL_CONFIG, new ClientConfigReader(cli));
        rreg.add(IIDs.IP_CL_CL_CL_CL_CL_CONFIG, new ClientIdentificationConfigReader(cli));
        rreg.add(IIDs.IP_CL_CL_CL_CL_SE_TU_CONFIG, new TunnelingConfigReader(cli));
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder wreg, Cli cli) {
        wreg.addNoop(IIDs.IPSEC);
        wreg.addNoop(IIDs.IP_CLIENTGROUPS);
        wreg.addNoop(IIDs.IP_CL_CLIENTGROUP);
        wreg.add(IIDs.IP_CL_CL_CONFIG, new ClientGroupConfigWriter(cli));
        wreg.addNoop(IIDs.IP_CL_CL_CL_CLIENT);
        wreg.addAfter(IIDs.IP_CL_CL_CL_CL_CONFIG, new ClientConfigWriter(cli), IIDs.IP_CL_CL_CONFIG);
        wreg.addNoop(IIDs.IP_CL_CL_CL_CL_CLIENTIDENTIFICATION);
        wreg.addAfter(IIDs.IP_CL_CL_CL_CL_CL_CONFIG, new ClientIdentificationConfigWriter(cli),
            IIDs.IP_CL_CL_CL_CL_CONFIG);
        wreg.addNoop(IIDs.IP_CL_CL_CL_CL_SECURITYASSOCIATION);
        wreg.addNoop(IIDs.IP_CL_CL_CL_CL_SE_TUNNELING);
        wreg.addAfter(IIDs.IP_CL_CL_CL_CL_SE_TU_CONFIG, new TunnelingConfigWriter(cli),
            IIDs.IP_CL_CL_CL_CL_CONFIG);
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_IPSEC);
    }
}