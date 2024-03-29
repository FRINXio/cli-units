/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos.acl;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.saos.acl.handler.AclEntryActionsConfigReader;
import io.frinx.cli.unit.saos.acl.handler.AclEntryConfigReader;
import io.frinx.cli.unit.saos.acl.handler.AclEntryReader;
import io.frinx.cli.unit.saos.acl.handler.AclEntryWriter;
import io.frinx.cli.unit.saos.acl.handler.AclInterfaceConfigReader;
import io.frinx.cli.unit.saos.acl.handler.AclInterfaceReader;
import io.frinx.cli.unit.saos.acl.handler.AclSetConfigReader;
import io.frinx.cli.unit.saos.acl.handler.AclSetConfigWriter;
import io.frinx.cli.unit.saos.acl.handler.AclSetReader;
import io.frinx.cli.unit.saos.acl.handler.IngressAclSetConfigReader;
import io.frinx.cli.unit.saos.acl.handler.IngressAclSetConfigWriter;
import io.frinx.cli.unit.saos.acl.handler.IngressAclSetReader;
import io.frinx.cli.unit.saos.init.SaosDevices;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.acl.IIDs;
import java.util.Collections;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class AclUnit extends AbstractUnit {

    public AclUnit(@NotNull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return Set.of(SaosDevices.SAOS_6);
    }

    @Override
    protected String getUnitName() {
        return "Saos-6 Acl (Openconfig) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet($YangModuleInfoImpl.getInstance(),
            IIDs.FRINX_OPENCONFIG_ACL,
            IIDs.FRINX_ACL_EXTENSION);
    }

    @Override
    public void provideHandlers(@NotNull final CustomizerAwareReadRegistryBuilder readRegistry,
                                @NotNull final CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @NotNull final Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.addNoop(IIDs.ACL);
        writeRegistry.addNoop(IIDs.AC_IN_INTERFACE);
        writeRegistry.addNoop(IIDs.AC_IN_IN_CONFIG);

        // sets
        writeRegistry.addNoop(IIDs.AC_ACLSETS);
        writeRegistry.addNoop(IIDs.AC_AC_ACLSET);
        writeRegistry.subtreeAddBefore(IIDs.AC_AC_AC_CONFIG, new AclSetConfigWriter(cli),
                Collections.singleton(IIDs.AC_AC_AC_CO_AUG_SAOS6ACLSETAUG),
                io.frinx.openconfig.openconfig.network.instance.IIDs.NE_NE_CONFIG);

        // entries
        writeRegistry.addNoop(IIDs.AC_AC_AC_ACLENTRIES);
        writeRegistry.subtreeAdd(IIDs.AC_AC_AC_AC_ACLENTRY, new AclEntryWriter(cli),
                Sets.newHashSet(IIDs.AC_AC_AC_AC_AC_CONFIG,
                        IIDs.AC_AC_AC_AC_AC_CO_AUG_CONFIG2,
                        IIDs.AC_AC_AC_AC_AC_ACTIONS,
                        IIDs.AC_AC_AC_AC_AC_AC_CONFIG));

        // ingress
        writeRegistry.addNoop(IIDs.AC_IN_IN_IN_INGRESSACLSET);
        writeRegistry.add(IIDs.AC_IN_IN_IN_IN_CONFIG, new IngressAclSetConfigWriter(cli));
    }

    private void provideReaders(@NotNull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.AC_IN_INTERFACE, new AclInterfaceReader(cli));
        readRegistry.add(IIDs.AC_IN_IN_CONFIG, new AclInterfaceConfigReader());

        // ingress
        readRegistry.add(IIDs.AC_IN_IN_IN_INGRESSACLSET, new IngressAclSetReader(cli));
        readRegistry.add(IIDs.AC_IN_IN_IN_IN_CONFIG, new IngressAclSetConfigReader());

        // sets
        readRegistry.add(IIDs.AC_AC_ACLSET, new AclSetReader(cli));
        readRegistry.add(IIDs.AC_AC_AC_CONFIG, new AclSetConfigReader(cli));

        // entries
        readRegistry.add(IIDs.AC_AC_AC_AC_ACLENTRY, new AclEntryReader(cli));
        readRegistry.add(IIDs.AC_AC_AC_AC_AC_CONFIG, new AclEntryConfigReader(cli));
        readRegistry.add(IIDs.AC_AC_AC_AC_AC_AC_CONFIG, new AclEntryActionsConfigReader(cli));
    }
}