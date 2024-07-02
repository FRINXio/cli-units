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

package io.frinx.cli.unit.cubro.unit.acl;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.cubro.init.CubroDevices;
import io.frinx.cli.unit.cubro.unit.acl.handler.AclEntryWriter;
import io.frinx.cli.unit.cubro.unit.acl.handler.AclInterfaceConfigReader;
import io.frinx.cli.unit.cubro.unit.acl.handler.AclInterfaceReader;
import io.frinx.cli.unit.cubro.unit.acl.handler.AclSetConfigWriter;
import io.frinx.cli.unit.cubro.unit.acl.handler.AclSetReader;
import io.frinx.cli.unit.cubro.unit.acl.handler.IngressAclSetConfigReader;
import io.frinx.cli.unit.cubro.unit.acl.handler.IngressAclSetConfigWriter;
import io.frinx.cli.unit.cubro.unit.acl.handler.IngressAclSetReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.acl.IIDs;
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
        return CubroDevices.CUBRO_ALL;
    }

    @Override
    protected String getUnitName() {
        return "CUBRO ACL unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet($YangModuleInfoImpl.getInstance(),
                IIDs.FRINX_OPENCONFIG_ACL,
                IIDs.FRINX_ACL_EXTENSION,
                IIDs.FRINX_CUBRO_ACL_TYPE_EXTENSION);
    }

    @Override
    public void provideHandlers(@NotNull CustomizerAwareReadRegistryBuilder readRegistry,
                                @NotNull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @NotNull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.AC_IN_INTERFACE, new AclInterfaceReader(cli));
        readRegistry.add(IIDs.AC_IN_IN_CONFIG, new AclInterfaceConfigReader());

        // ingress
        readRegistry.add(IIDs.AC_IN_IN_IN_INGRESSACLSET, new IngressAclSetReader(cli));
        readRegistry.add(IIDs.AC_IN_IN_IN_IN_CONFIG, new IngressAclSetConfigReader(cli));

        // ACL Entry subtree
        readRegistry.subtreeAdd(IIDs.AC_AC_ACLSET, new AclSetReader(cli),
                Sets.newHashSet(IIDs.AC_AC_AC_CONFIG,
                        IIDs.AC_AC_AC_AC_ACLENTRY,
                        IIDs.AC_AC_AC_AC_AC_CONFIG,
                        IIDs.AC_AC_AC_AC_AC_ACTIONS,
                        IIDs.AC_AC_AC_AC_AC_AC_CONFIG,

                        IIDs.AC_AC_AC_AC_AC_IPV4,
                        IIDs.AC_AC_AC_AC_AC_IP_CONFIG,
                        IIDs.AC_AC_AC_AC_AC_IP_CO_AUG_CONFIG3,
                        IIDs.AC_AC_AC_AC_AC_AC_CO_AUG_ACLCUBROAUG));
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.addNoop(IIDs.ACL);
        writeRegistry.addNoop(IIDs.AC_IN_INTERFACE);
        writeRegistry.addNoop(IIDs.AC_IN_IN_CONFIG);

        // sets
        writeRegistry.addNoop(IIDs.AC_ACLSETS);
        writeRegistry.add(IIDs.AC_AC_ACLSET, new AclSetConfigWriter(cli));

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

                        IIDs.AC_AC_AC_AC_AC_ACTIONS,
                        IIDs.AC_AC_AC_AC_AC_AC_CONFIG,
                        IIDs.AC_AC_AC_AC_AC_AC_CO_AUG_ACLCUBROAUG));

        // ingress
        writeRegistry.addNoop(IIDs.AC_IN_IN_IN_INGRESSACLSET);
        writeRegistry.add(IIDs.AC_IN_IN_IN_IN_CONFIG, new IngressAclSetConfigWriter(cli));
    }
}