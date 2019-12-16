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

package io.frinx.cli.unit.iosxr.unit.acl;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.iosxr.init.IosXrDevices;
import io.frinx.cli.unit.iosxr.unit.acl.handler.AclEntryReader;
import io.frinx.cli.unit.iosxr.unit.acl.handler.AclEntryWriter;
import io.frinx.cli.unit.iosxr.unit.acl.handler.AclInterfaceConfigReader;
import io.frinx.cli.unit.iosxr.unit.acl.handler.AclInterfaceReader;
import io.frinx.cli.unit.iosxr.unit.acl.handler.AclSetConfigReader;
import io.frinx.cli.unit.iosxr.unit.acl.handler.AclSetReader;
import io.frinx.cli.unit.iosxr.unit.acl.handler.EgressAclSetConfigReader;
import io.frinx.cli.unit.iosxr.unit.acl.handler.EgressAclSetConfigWriter;
import io.frinx.cli.unit.iosxr.unit.acl.handler.EgressAclSetReader;
import io.frinx.cli.unit.iosxr.unit.acl.handler.IngressAclSetConfigReader;
import io.frinx.cli.unit.iosxr.unit.acl.handler.IngressAclSetConfigWriter;
import io.frinx.cli.unit.iosxr.unit.acl.handler.IngressAclSetReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.acl.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class AclUnit extends AbstractUnit {

    public AclUnit(@Nonnull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return IosXrDevices.IOS_XR_ALL;
    }

    @Override
    protected String getUnitName() {
        return "IOS XR ACL unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet($YangModuleInfoImpl.getInstance(),
                IIDs.FRINX_OPENCONFIG_ACL,
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
        writeRegistry.addNoop(IIDs.AC_IN_INTERFACE);
        writeRegistry.addNoop(IIDs.AC_IN_IN_CONFIG);

        writeRegistry.addNoop(IIDs.AC_ACLSETS);
        writeRegistry.addNoop(IIDs.AC_AC_ACLSET);
        writeRegistry.addNoop(IIDs.AC_AC_AC_CONFIG);
        writeRegistry.addNoop(IIDs.AC_AC_AC_ACLENTRIES);
        writeRegistry.subtreeAdd(IIDs.AC_AC_AC_AC_ACLENTRY, new AclEntryWriter(cli),
                Sets.newHashSet(IIDs.AC_AC_AC_AC_AC_CONFIG,
                IIDs.AC_AC_AC_AC_AC_CO_AUG_CONFIG2,

                IIDs.AC_AC_AC_AC_AC_IPV4,
                IIDs.AC_AC_AC_AC_AC_IP_CONFIG,
                IIDs.AC_AC_AC_AC_AC_IP_CO_AUG_CONFIG3,
                IIDs.AC_AC_AC_AC_AC_IP_CO_AUG_ACLSETACLENTRYIPV4WILDCARDEDAUG,
                IIDs.AC_AC_AC_AC_AC_IP_CO_AUG_ACLSETACLENTRYIPV4WILDCARDEDAUG_SOURCEADDRESSWILDCARDED,
                IIDs.AC_AC_AC_AC_AC_IP_CO_AUG_ACLSETACLENTRYIPV4WILDCARDEDAUG_DESTINATIONADDRESSWILDCARDED,

                IIDs.AC_AC_AC_AC_AC_IPV6,
                IIDs.ACL_ACL_ACL_ACL_ACL_IPV_CONFIG,
                IIDs.AC_AC_AC_AC_AC_IP_CO_AUG_CONFIG4,
                IIDs.AC_AC_AC_AC_AC_IP_CO_AUG_ACLSETACLENTRYIPV6WILDCARDEDAUG,
                IIDs.AC_AC_AC_AC_AC_IP_CO_AUG_ACLSETACLENTRYIPV6WILDCARDEDAUG_SOURCEADDRESSWILDCARDED,
                IIDs.AC_AC_AC_AC_AC_IP_CO_AUG_ACLSETACLENTRYIPV6WILDCARDEDAUG_DESTINATIONADDRESSWILDCARDED,

                IIDs.AC_AC_AC_AC_AC_TRANSPORT,
                IIDs.AC_AC_AC_AC_AC_TR_CONFIG,
                IIDs.AC_AC_AC_AC_AC_TR_CONFIG,
                IIDs.AC_AC_AC_AC_AC_TR_CO_AUG_ACLSETACLENTRYTRANSPORTPORTNAMEDAUG,

                IIDs.AC_AC_AC_AC_AC_ACTIONS,
                IIDs.AC_AC_AC_AC_AC_AC_CONFIG,
                IIDs.AC_AC_AC_AC_AC_AUG_ACLENTRY1,
                IIDs.AC_AC_AC_AC_AC_AUG_ACLENTRY1_ICMP,
                IIDs.AC_AC_AC_AC_AC_AUG_ACLENTRY1_IC_CONFIG));

        // ingress
        writeRegistry.addNoop(IIDs.AC_IN_IN_IN_INGRESSACLSET);
        writeRegistry.addAfter(IIDs.AC_IN_IN_IN_IN_CONFIG, new IngressAclSetConfigWriter(cli),
                io.frinx.openconfig.openconfig.interfaces.IIDs.IN_IN_SU_SU_CONFIG);

        // egress
        writeRegistry.addNoop(IIDs.AC_IN_IN_EG_EGRESSACLSET);
        writeRegistry.addAfter(IIDs.AC_IN_IN_EG_EG_CONFIG, new EgressAclSetConfigWriter(cli),
                io.frinx.openconfig.openconfig.interfaces.IIDs.IN_IN_SU_SU_CONFIG);
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
        readRegistry.subtreeAdd(IIDs.AC_AC_AC_AC_ACLENTRY, new AclEntryReader(cli),
                Sets.newHashSet(IIDs.AC_AC_AC_AC_AC_CONFIG,
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
