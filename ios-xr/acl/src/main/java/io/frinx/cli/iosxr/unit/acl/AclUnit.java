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

package io.frinx.cli.iosxr.unit.acl;

import static io.frinx.cli.iosxr.IosXrDevices.IOS_XR_ALL;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.unit.acl.handler.AclEntryIpv6ConfigReader;
import io.frinx.cli.iosxr.unit.acl.handler.AclEntryTransportConfigReader;
import io.frinx.cli.iosxr.unit.acl.handler.AclInterfaceConfigReader;
import io.frinx.cli.iosxr.unit.acl.handler.AclInterfaceReader;
import io.frinx.cli.iosxr.unit.acl.handler.AclEntryIpv4ConfigReader;
import io.frinx.cli.iosxr.unit.acl.handler.AclEntryActionsConfigReader;
import io.frinx.cli.iosxr.unit.acl.handler.AclSetConfigReader;
import io.frinx.cli.iosxr.unit.acl.handler.AclSetReader;
import io.frinx.cli.iosxr.unit.acl.handler.AclEntryConfigReader;
import io.frinx.cli.iosxr.unit.acl.handler.AclEntryReader;
import io.frinx.cli.iosxr.unit.acl.handler.AclSetConfigReader;
import io.frinx.cli.iosxr.unit.acl.handler.EgressAclSetConfigReader;
import io.frinx.cli.iosxr.unit.acl.handler.EgressAclSetConfigWriter;
import io.frinx.cli.iosxr.unit.acl.handler.EgressAclSetReader;
import io.frinx.cli.iosxr.unit.acl.handler.IngressAclSetConfigReader;
import io.frinx.cli.iosxr.unit.acl.handler.IngressAclSetConfigWriter;
import io.frinx.cli.iosxr.unit.acl.handler.IngressAclSetReader;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.acl.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.EgressAclSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.IngressAclSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.AclEntriesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.AclSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.top.AclBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.ActionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.Ipv4Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.Ipv6Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.transport.fields.top.TransportBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.$YangModuleInfoImpl;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class AclUnit implements TranslateUnit {

    private static final InstanceIdentifier<Config> IPV4_CONFIG=
            IIDs.AC_AC_AC_AC_AC_IPV4.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.Config.class);
    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.Config> IPV6_CONFIG=
            IIDs.AC_AC_AC_AC_AC_IPV6.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields.top.ipv6.Config.class);


    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public AclUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        reg = registry.registerTranslateUnit(IOS_XR_ALL, this);
    }

    public void close() {
        if (reg != null) {
            reg.close();
        }
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet($YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.$YangModuleInfoImpl.getInstance());
    }

    @Override
    public Set<RpcService<?, ?>> getRpcs(@Nonnull final Context context) {
        return Sets.newHashSet();
    }

    @Override
    public void provideHandlers(@Nonnull final ModifiableReaderRegistryBuilder rRegistry,
                                @Nonnull final ModifiableWriterRegistryBuilder wRegistry,
                                @Nonnull final Context context) {
        Cli cli = context.getTransport();
        provideReaders(rRegistry, cli);
        provideWriters(wRegistry, cli);
    }

    private void provideWriters(ModifiableWriterRegistryBuilder wRegistry, Cli cli) {
        wRegistry.add(new GenericWriter<>(IIDs.ACL, new NoopCliWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.AC_IN_INTERFACE, new NoopCliListWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.AC_IN_IN_CONFIG, new NoopCliWriter<>()));

        // ingress
        wRegistry.add(new GenericWriter<>(IIDs.AC_IN_IN_IN_INGRESSACLSET, new NoopCliListWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.AC_IN_IN_IN_IN_CONFIG, new IngressAclSetConfigWriter(cli)));

        // egress
        wRegistry.add(new GenericWriter<>(IIDs.AC_IN_IN_EG_EGRESSACLSET, new NoopCliListWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.AC_IN_IN_EG_EG_CONFIG, new EgressAclSetConfigWriter(cli)));
    }

    private void provideReaders(@Nonnull ModifiableReaderRegistryBuilder rRegistry, Cli cli) {
        rRegistry.addStructuralReader(IIDs.ACL, AclBuilder.class);

        rRegistry.addStructuralReader(IIDs.AC_INTERFACES, InterfacesBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(IIDs.AC_IN_INTERFACE, new AclInterfaceReader(cli)));
        rRegistry.add(new GenericConfigReader<>(IIDs.AC_IN_IN_CONFIG, new AclInterfaceConfigReader()));

        // ingress
        rRegistry.addStructuralReader(IIDs.AC_IN_IN_INGRESSACLSETS, IngressAclSetsBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(IIDs.AC_IN_IN_IN_INGRESSACLSET, new IngressAclSetReader(cli)));
        rRegistry.add(new GenericConfigReader<>(IIDs.AC_IN_IN_IN_IN_CONFIG, new IngressAclSetConfigReader(cli)));

        // egress
        rRegistry.addStructuralReader(IIDs.AC_IN_IN_EGRESSACLSETS, EgressAclSetsBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(IIDs.AC_IN_IN_EG_EGRESSACLSET, new EgressAclSetReader(cli)));
        rRegistry.add(new GenericConfigReader<>(IIDs.AC_IN_IN_EG_EG_CONFIG, new EgressAclSetConfigReader(cli)));

        // sets
        rRegistry.addStructuralReader(IIDs.AC_ACLSETS, AclSetsBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(IIDs.AC_AC_ACLSET, new AclSetReader(cli)));
        // acl-set/config
        rRegistry.add(new GenericConfigReader<>(IIDs.AC_AC_AC_CONFIG, new AclSetConfigReader()));
        // Access list entries inside acl-set
        rRegistry.addStructuralReader(IIDs.AC_AC_AC_ACLENTRIES, AclEntriesBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(IIDs.AC_AC_AC_AC_ACLENTRY, new AclEntryReader(cli)));
        // access list entry/config
        rRegistry.add(new GenericConfigReader<>(IIDs.AC_AC_AC_AC_AC_CONFIG, new AclEntryConfigReader()));
        // access list entry/actions
        rRegistry.addStructuralReader(IIDs.AC_AC_AC_AC_AC_ACTIONS, ActionsBuilder.class);
        rRegistry.add(new GenericConfigReader<>(IIDs.AC_AC_AC_AC_AC_AC_CONFIG, new AclEntryActionsConfigReader(cli)));
        // access list entry/ipv4
        rRegistry.addStructuralReader(IIDs.AC_AC_AC_AC_AC_IPV4, Ipv4Builder.class);
        rRegistry.add(new GenericConfigReader<>(IPV4_CONFIG, new AclEntryIpv4ConfigReader(cli)));
        // access list entry/ipv6
        rRegistry.addStructuralReader(IIDs.AC_AC_AC_AC_AC_IPV6, Ipv6Builder.class);
        rRegistry.add(new GenericConfigReader<>(IPV6_CONFIG, new AclEntryIpv6ConfigReader(cli)));
        // access list entry/transport
        rRegistry.addStructuralReader(IIDs.AC_AC_AC_AC_AC_TRANSPORT, TransportBuilder.class);
        // access list entry/transport/config
        rRegistry.add(new GenericConfigReader<>(IIDs.AC_AC_AC_AC_AC_TR_CONFIG, new AclEntryTransportConfigReader(cli)));

    }

    @Override
    public String toString() {
        return "IOS XR ACL unit";
    }
}
