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

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.write.GenericListWriter;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.IosXrDevices;
import io.frinx.cli.iosxr.unit.acl.handler.AclEntryReader;
import io.frinx.cli.iosxr.unit.acl.handler.AclEntryWriter;
import io.frinx.cli.iosxr.unit.acl.handler.AclInterfaceConfigReader;
import io.frinx.cli.iosxr.unit.acl.handler.AclInterfaceReader;
import io.frinx.cli.iosxr.unit.acl.handler.AclSetConfigReader;
import io.frinx.cli.iosxr.unit.acl.handler.AclSetReader;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntry;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.AclSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.top.AclBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.$YangModuleInfoImpl;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class AclUnit implements TranslateUnit {

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    private static final InstanceIdentifier<AclEntry> ACL_ENTRY_TREE_BASE = InstanceIdentifier.create(AclEntry.class);

    public AclUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        reg = registry.registerTranslateUnit(IosXrDevices.IOS_XR_ALL, this);
    }

    public void close() {
        if (reg != null) {
            reg.close();
        }
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet($YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526
                        .$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314
                        .$YangModuleInfoImpl.getInstance()
        );
    }

    @Override
    public Set<RpcService<?, ?>> getRpcs(@Nonnull final Context context) {
        return Sets.newHashSet();
    }

    @Override
    public void provideHandlers(@Nonnull final ModifiableReaderRegistryBuilder readRegistry,
                                @Nonnull final ModifiableWriterRegistryBuilder writeRegistry,
                                @Nonnull final Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(ModifiableWriterRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.add(new GenericWriter<>(IIDs.ACL, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.AC_IN_INTERFACE, new NoopCliListWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.AC_IN_IN_CONFIG, new NoopCliWriter<>()));

        // ingress
        writeRegistry.add(new GenericWriter<>(IIDs.AC_IN_IN_IN_INGRESSACLSET, new NoopCliListWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.AC_IN_IN_IN_IN_CONFIG, new IngressAclSetConfigWriter(cli)));

        // egress
        writeRegistry.add(new GenericWriter<>(IIDs.AC_IN_IN_EG_EGRESSACLSET, new NoopCliListWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.AC_IN_IN_EG_EG_CONFIG, new EgressAclSetConfigWriter(cli)));

        writeRegistry.add(new GenericWriter<>(IIDs.AC_ACLSETS, new NoopCliWriter<>()));
        writeRegistry.add(new GenericListWriter<>(IIDs.AC_AC_ACLSET, new NoopCliListWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.AC_AC_AC_CONFIG, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.AC_AC_AC_ACLENTRIES, new NoopCliWriter<>()));
        writeRegistry.subtreeAdd(Sets.newHashSet(
                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_CONFIG, ACL_ENTRY_TREE_BASE),
                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_IPV4, ACL_ENTRY_TREE_BASE),
                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_IP_CONFIG, ACL_ENTRY_TREE_BASE),
                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_IP_CO_AUG_CONFIG3, ACL_ENTRY_TREE_BASE),
                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_IP_CO_AUG_ACLSETACLENTRYIPV4WILDCARDEDAUG,
                        ACL_ENTRY_TREE_BASE),
                RWUtils.cutIdFromStart(
                        IIDs.AC_AC_AC_AC_AC_IP_CO_AUG_ACLSETACLENTRYIPV4WILDCARDEDAUG_SOURCEADDRESSWILDCARDED,
                        ACL_ENTRY_TREE_BASE),
                RWUtils.cutIdFromStart(
                        IIDs.AC_AC_AC_AC_AC_IP_CO_AUG_ACLSETACLENTRYIPV4WILDCARDEDAUG_DESTINATIONADDRESSWILDCARDED,
                        ACL_ENTRY_TREE_BASE),

                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_IPV6, ACL_ENTRY_TREE_BASE),
                RWUtils.cutIdFromStart(IIDs.ACL_ACL_ACL_ACL_ACL_IPV_CONFIG, ACL_ENTRY_TREE_BASE),
                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_IP_CO_AUG_CONFIG4, ACL_ENTRY_TREE_BASE),
                RWUtils.cutIdFromStart(IIDs.NE_TO_NO_CO_AC_AC_AC_AC_AC_IP_CO_AUG_ACLSETACLENTRYIPV6WILDCARDEDAUG,
                        ACL_ENTRY_TREE_BASE),
                RWUtils.cutIdFromStart(
                    IIDs.NE_TO_NO_CO_AC_AC_AC_AC_AC_IP_CO_AUG_ACLSETACLENTRYIPV6WILDCARDEDAUG_SOURCEADDRESSWILDCARDED,
                    ACL_ENTRY_TREE_BASE),
                RWUtils.cutIdFromStart(
                IIDs.NE_TO_NO_CO_AC_AC_AC_AC_AC_IP_CO_AUG_ACLSETACLENTRYIPV6WILDCARDEDAUG_DESTINATIONADDRESSWILDCARDED,
                        ACL_ENTRY_TREE_BASE),

                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_TRANSPORT, ACL_ENTRY_TREE_BASE),
                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_TR_CONFIG, ACL_ENTRY_TREE_BASE),
                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_TR_CONFIG, ACL_ENTRY_TREE_BASE),
                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_TR_CO_AUG_ACLSETACLENTRYTRANSPORTPORTNAMEDAUG,
                        ACL_ENTRY_TREE_BASE),

                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_ACTIONS, ACL_ENTRY_TREE_BASE),
                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_AC_CONFIG, ACL_ENTRY_TREE_BASE),
                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_AUG_ACLENTRY1, ACL_ENTRY_TREE_BASE),
                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_AUG_ACLENTRY1_ICMP, ACL_ENTRY_TREE_BASE),
                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_AUG_ACLENTRY1_IC_CONFIG, ACL_ENTRY_TREE_BASE)),
                new GenericWriter<>(IIDs.AC_AC_AC_AC_ACLENTRY, new AclEntryWriter(cli)));
    }

    private void provideReaders(@Nonnull ModifiableReaderRegistryBuilder readRegistry, Cli cli) {
        readRegistry.addStructuralReader(IIDs.ACL, AclBuilder.class);

        readRegistry.addStructuralReader(IIDs.AC_INTERFACES, InterfacesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.AC_IN_INTERFACE, new AclInterfaceReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.AC_IN_IN_CONFIG, new AclInterfaceConfigReader()));

        // ingress
        readRegistry.addStructuralReader(IIDs.AC_IN_IN_INGRESSACLSETS, IngressAclSetsBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.AC_IN_IN_IN_INGRESSACLSET, new IngressAclSetReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.AC_IN_IN_IN_IN_CONFIG, new IngressAclSetConfigReader(cli)));

        // egress
        readRegistry.addStructuralReader(IIDs.AC_IN_IN_EGRESSACLSETS, EgressAclSetsBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.AC_IN_IN_EG_EGRESSACLSET, new EgressAclSetReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.AC_IN_IN_EG_EG_CONFIG, new EgressAclSetConfigReader(cli)));

        // sets
        readRegistry.addStructuralReader(IIDs.AC_ACLSETS, AclSetsBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.AC_AC_ACLSET, new AclSetReader(cli)));
        // acl-set/config
        readRegistry.add(new GenericConfigReader<>(IIDs.AC_AC_AC_CONFIG, new AclSetConfigReader()));
        // Access list entries inside acl-set
        readRegistry.addStructuralReader(IIDs.AC_AC_AC_ACLENTRIES, AclEntriesBuilder.class);

        // ACL Entry subtree
        readRegistry.subtreeAdd(Sets.newHashSet(
                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_CONFIG, ACL_ENTRY_TREE_BASE),

                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_ACTIONS, ACL_ENTRY_TREE_BASE),
                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_AC_CONFIG, ACL_ENTRY_TREE_BASE),

                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_IPV4, ACL_ENTRY_TREE_BASE),
                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_IP_CONFIG, ACL_ENTRY_TREE_BASE),
                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_IP_CO_AUG_CONFIG3, ACL_ENTRY_TREE_BASE),

                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_IPV6, ACL_ENTRY_TREE_BASE),
                RWUtils.cutIdFromStart(IIDs.ACL_ACL_ACL_ACL_ACL_IPV_CONFIG, ACL_ENTRY_TREE_BASE),
                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_IP_CO_AUG_CONFIG4 , ACL_ENTRY_TREE_BASE),

                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_TRANSPORT, ACL_ENTRY_TREE_BASE),
                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_TR_CONFIG, ACL_ENTRY_TREE_BASE),
                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_TR_CO_AUG_ACLSETACLENTRYTRANSPORTPORTNAMEDAUG,
                        ACL_ENTRY_TREE_BASE),

                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_AUG_ACLENTRY1, ACL_ENTRY_TREE_BASE),
                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_AUG_ACLENTRY1_ICMP, ACL_ENTRY_TREE_BASE),
                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_AUG_ACLENTRY1_IC_CONFIG, ACL_ENTRY_TREE_BASE)
        ), new GenericConfigListReader<>(IIDs.AC_AC_AC_AC_ACLENTRY, new AclEntryReader(cli)));
    }

    @Override
    public String toString() {
        return "IOS XR ACL unit";
    }
}
