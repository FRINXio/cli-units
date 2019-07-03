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

package io.frinx.cli.ios.unit.acl;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.unit.acl.handler.AclEntryReader;
import io.frinx.cli.ios.unit.acl.handler.AclEntryWriter;
import io.frinx.cli.ios.unit.acl.handler.AclInterfaceConfigReader;
import io.frinx.cli.ios.unit.acl.handler.AclInterfaceReader;
import io.frinx.cli.ios.unit.acl.handler.AclSetConfigReader;
import io.frinx.cli.ios.unit.acl.handler.AclSetReader;
import io.frinx.cli.ios.unit.acl.handler.EgressAclSetConfigReader;
import io.frinx.cli.ios.unit.acl.handler.EgressAclSetConfigWriter;
import io.frinx.cli.ios.unit.acl.handler.EgressAclSetReader;
import io.frinx.cli.ios.unit.acl.handler.IngressAclSetConfigReader;
import io.frinx.cli.ios.unit.acl.handler.IngressAclSetConfigWriter;
import io.frinx.cli.ios.unit.acl.handler.IngressAclSetReader;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.ios.init.IosDevices;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.acl.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntry;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class AclUnit extends AbstractUnit {

    private static final InstanceIdentifier<AclEntry> ACL_ENTRY_TREE_BASE = InstanceIdentifier.create(AclEntry.class);

    public AclUnit(@Nonnull TranslationUnitCollector registry) {
        super(registry);
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
    public void provideHandlers(@Nonnull final CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull final CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull final Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.addNoop(IIDs.ACL);
        writeRegistry.addNoop(IIDs.AC_IN_INTERFACE);
        writeRegistry.addNoop(IIDs.AC_IN_IN_CONFIG);

        // ingress
        writeRegistry.addNoop(IIDs.AC_IN_IN_IN_INGRESSACLSET);
        writeRegistry.add(IIDs.AC_IN_IN_IN_IN_CONFIG, new IngressAclSetConfigWriter(cli));

        // egress
        writeRegistry.addNoop(IIDs.AC_IN_IN_EG_EGRESSACLSET);
        writeRegistry.add(IIDs.AC_IN_IN_EG_EG_CONFIG, new EgressAclSetConfigWriter(cli));

        writeRegistry.addNoop(IIDs.AC_ACLSETS);
        writeRegistry.addNoop(IIDs.AC_AC_ACLSET);
        writeRegistry.addNoop(IIDs.AC_AC_AC_CONFIG);
        writeRegistry.addNoop(IIDs.AC_AC_AC_ACLENTRIES);
        writeRegistry.subtreeAdd(IIDs.AC_AC_AC_AC_ACLENTRY,new AclEntryWriter(cli),Sets.newHashSet(
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
                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_AUG_ACLENTRY1_IC_CONFIG, ACL_ENTRY_TREE_BASE)));
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.AC_IN_INTERFACE, new AclInterfaceReader(cli));
        readRegistry.add(IIDs.AC_IN_IN_CONFIG, new AclInterfaceConfigReader());

        // ingress
        readRegistry.add(IIDs.AC_IN_IN_IN_INGRESSACLSET, new IngressAclSetReader(cli));
        readRegistry.add(IIDs.AC_IN_IN_IN_IN_CONFIG, new IngressAclSetConfigReader(cli));

        // egress
        readRegistry.add(IIDs.AC_IN_IN_EG_EGRESSACLSET, new EgressAclSetReader(cli));
        readRegistry.add(IIDs.AC_IN_IN_EG_EG_CONFIG, new EgressAclSetConfigReader(cli));

        // sets
        readRegistry.add(IIDs.AC_AC_ACLSET, new AclSetReader(cli));
        // acl-set/config
        readRegistry.add(IIDs.AC_AC_AC_CONFIG, new AclSetConfigReader());

        // ACL Entry subtree
        readRegistry.subtreeAdd(IIDs.AC_AC_AC_AC_ACLENTRY,new AclEntryReader(cli),Sets.newHashSet(
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
                RWUtils.cutIdFromStart(IIDs.AC_AC_AC_AC_AC_AUG_ACLENTRY1_IC_CONFIG, ACL_ENTRY_TREE_BASE)));
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return IosDevices.IOS_ALL;
    }

    @Override
    protected String getUnitName() {
        return "IOS ACL unit";
    }
}
