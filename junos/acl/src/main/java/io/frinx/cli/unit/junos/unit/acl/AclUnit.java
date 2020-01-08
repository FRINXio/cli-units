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

package io.frinx.cli.unit.junos.unit.acl;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.junos.init.JunosDevices;
import io.frinx.cli.unit.junos.unit.acl.handler.AclEntriesWriter;
import io.frinx.cli.unit.junos.unit.acl.handler.AclInterfaceConfigReader;
import io.frinx.cli.unit.junos.unit.acl.handler.AclInterfaceReader;
import io.frinx.cli.unit.junos.unit.acl.handler.AclInterfaceWriter;
import io.frinx.cli.unit.junos.unit.acl.handler.AclSetConfigWriter;
import io.frinx.cli.unit.junos.unit.acl.handler.AclSetReader;
import io.frinx.cli.unit.junos.unit.acl.handler.EgressAclSetConfigReader;
import io.frinx.cli.unit.junos.unit.acl.handler.EgressAclSetConfigWriter;
import io.frinx.cli.unit.junos.unit.acl.handler.EgressAclSetReader;
import io.frinx.cli.unit.junos.unit.acl.handler.IngressAclSetConfigReader;
import io.frinx.cli.unit.junos.unit.acl.handler.IngressAclSetConfigWriter;
import io.frinx.cli.unit.junos.unit.acl.handler.IngressAclSetReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.acl.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class AclUnit extends AbstractUnit {

    public AclUnit(@Nonnull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return JunosDevices.JUNOS_ALL;
    }

    @Override
    protected String getUnitName() {
        return "Junos CLI ACL unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_OPENCONFIG_ACL,
                                IIDs.FRINX_ACL_EXTENSION);
    }

    @Override
    public void provideHandlers(@Nonnull final CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull final CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull final Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.addNoop(IIDs.ACL);

        // interface
        writeRegistry.add(IIDs.AC_IN_INTERFACE, new AclInterfaceWriter());
        writeRegistry.addAfter(IIDs.AC_IN_IN_CONFIG, new NoopCliWriter<>(),
            io.frinx.openconfig.openconfig.interfaces.IIDs.IN_IN_SU_SU_CONFIG);

        // ingress
        writeRegistry.addNoop(IIDs.AC_IN_IN_INGRESSACLSETS);
        writeRegistry.addNoop(IIDs.AC_IN_IN_IN_INGRESSACLSET);
        writeRegistry.addAfter(IIDs.AC_IN_IN_IN_IN_CONFIG, new IngressAclSetConfigWriter(cli),
            IIDs.AC_IN_IN_CONFIG);
        // egress
        writeRegistry.addNoop(IIDs.AC_IN_IN_EGRESSACLSETS);
        writeRegistry.addNoop(IIDs.AC_IN_IN_EG_EGRESSACLSET);
        writeRegistry.addAfter(IIDs.AC_IN_IN_EG_EG_CONFIG, new EgressAclSetConfigWriter(cli),
            IIDs.AC_IN_IN_CONFIG);

        // sets
        writeRegistry.addNoop(IIDs.AC_ACLSETS);
        writeRegistry.add(IIDs.AC_AC_ACLSET, new AclSetConfigWriter(cli));

        writeRegistry.addNoop(IIDs.AC_AC_AC_CONFIG);
        writeRegistry.subtreeAdd(IIDs.AC_AC_AC_ACLENTRIES, new AclEntriesWriter(cli),
                Sets.newHashSet(IIDs.AC_AC_AC_AC_ACLENTRY,
                        IIDs.AC_AC_AC_AC_AC_CONFIG,
                        IIDs.AC_AC_AC_AC_AC_CO_AUG_CONFIG2,

                        IIDs.AC_AC_AC_AC_AC_IPV4,
                        IIDs.AC_AC_AC_AC_AC_IP_CONFIG,
                        IIDs.AC_AC_AC_AC_AC_IP_CO_AUG_CONFIG3,

                        IIDs.AC_AC_AC_AC_AC_IPV6,
                        IIDs.ACL_ACL_ACL_ACL_ACL_IPV_CONFIG,
                        IIDs.AC_AC_AC_AC_AC_IP_CO_AUG_CONFIG4,

                        IIDs.AC_AC_AC_AC_AC_TRANSPORT,
                        IIDs.AC_AC_AC_AC_AC_TR_CONFIG,
                        IIDs.AC_AC_AC_AC_AC_TR_CONFIG,
                        IIDs.AC_AC_AC_AC_AC_TR_CO_AUG_ACLSETACLENTRYTRANSPORTPORTNAMEDAUG,

                        IIDs.AC_AC_AC_AC_AC_ACTIONS,
                        IIDs.AC_AC_AC_AC_AC_AC_CONFIG,
                        IIDs.AC_AC_AC_AC_AC_AUG_ACLENTRY1,
                        IIDs.AC_AC_AC_AC_AC_AUG_ACLENTRY1_ICMP,
                        IIDs.AC_AC_AC_AC_AC_AUG_ACLENTRY1_IC_CONFIG));
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        // interface
        readRegistry.add(IIDs.AC_IN_INTERFACE, new AclInterfaceReader(cli));
        readRegistry.add(IIDs.AC_IN_IN_CONFIG, new AclInterfaceConfigReader());

        // ingress
        readRegistry.add(IIDs.AC_IN_IN_IN_INGRESSACLSET, new IngressAclSetReader(cli));
        readRegistry.add(IIDs.AC_IN_IN_IN_IN_CONFIG, new IngressAclSetConfigReader());
        // egress
        readRegistry.add(IIDs.AC_IN_IN_EG_EGRESSACLSET, new EgressAclSetReader(cli));
        readRegistry.add(IIDs.AC_IN_IN_EG_EG_CONFIG, new EgressAclSetConfigReader());

        // ACL sets with ACL configs and ACL entries
        readRegistry.subtreeAdd(IIDs.AC_AC_ACLSET, new AclSetReader(cli),
                Sets.newHashSet(IIDs.AC_AC_ACLSET,
                        IIDs.AC_AC_AC_CONFIG,

                        IIDs.AC_AC_AC_AC_AC_CONFIG,
                        IIDs.AC_AC_AC_AC_AC_ACTIONS,
                        IIDs.AC_AC_AC_AC_AC_AC_CONFIG,

                        IIDs.AC_AC_AC_AC_AC_IPV4,
                        IIDs.AC_AC_AC_AC_AC_IP_CONFIG,
                        IIDs.AC_AC_AC_AC_AC_IP_CO_AUG_CONFIG3,

                        IIDs.AC_AC_AC_AC_AC_IPV6,
                        IIDs.ACL_ACL_ACL_ACL_ACL_IPV_CONFIG,
                        IIDs.AC_AC_AC_AC_AC_IP_CO_AUG_CONFIG4,

                        IIDs.AC_AC_AC_AC_AC_TRANSPORT,
                        IIDs.AC_AC_AC_AC_AC_TR_CONFIG,
                        IIDs.AC_AC_AC_AC_AC_TR_CO_AUG_ACLSETACLENTRYTRANSPORTPORTNAMEDAUG,

                        IIDs.AC_AC_AC_AC_AC_AUG_ACLENTRY1,
                        IIDs.AC_AC_AC_AC_AC_AUG_ACLENTRY1_ICMP,
                        IIDs.AC_AC_AC_AC_AC_AUG_ACLENTRY1_IC_CONFIG));
    }
}
