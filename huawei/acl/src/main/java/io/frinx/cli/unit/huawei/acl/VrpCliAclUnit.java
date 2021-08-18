/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.huawei.acl;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.huawei.acl.handler.AclConfigWriter;
import io.frinx.cli.unit.huawei.acl.handler.AclEntryWriter;
import io.frinx.cli.unit.huawei.acl.handler.AclReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.acl.IIDs;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class VrpCliAclUnit extends AbstractUnit {

    private static final Device HUAWEI = new DeviceIdBuilder()
            .setDeviceType("vrp")
            .setDeviceVersion("*")
            .build();

    public VrpCliAclUnit(@Nonnull TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_OPENCONFIG_ACL,
                IIDs.FRINX_ACL_EXTENSION,
                $YangModuleInfoImpl.getInstance());
    }

    @Override
    public void provideHandlers(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.addNoop(IIDs.ACL);
        writeRegistry.addNoop(IIDs.AC_ACLSETS);
        writeRegistry.add(IIDs.AC_AC_ACLSET, new AclConfigWriter(cli));
        writeRegistry.addNoop(IIDs.AC_AC_AC_CONFIG);
        writeRegistry.subtreeAdd(IIDs.AC_AC_AC_AC_ACLENTRY, new AclEntryWriter(cli),
            Sets.newHashSet(IIDs.AC_AC_AC_AC_AC_CONFIG,
                IIDs.AC_AC_AC_AC_AC_CO_AUG_CONFIG2,
                IIDs.AC_AC_AC_AC_AC_IPV4,
                IIDs.AC_AC_AC_AC_AC_IP_CONFIG,
                IIDs.AC_AC_AC_AC_AC_IP_CO_AUG_ACLSETACLENTRYIPV4WILDCARDEDAUG,
                IIDs.AC_AC_AC_AC_AC_IP_CO_AUG_ACLSETACLENTRYIPV4WILDCARDEDAUG_SOURCEADDRESSWILDCARDED,
                IIDs.AC_AC_AC_AC_AC_IP_CO_AUG_ACLSETACLENTRYIPV4WILDCARDEDAUG_DESTINATIONADDRESSWILDCARDED,
                IIDs.ACL_ACL_ACL_ACL_ACL_IPV_CONFIG,
                IIDs.AC_AC_AC_AC_AC_TRANSPORT,
                IIDs.AC_AC_AC_AC_AC_TR_CONFIG,
                IIDs.AC_AC_AC_AC_AC_TR_CONFIG,
                IIDs.AC_AC_AC_AC_AC_TR_CO_AUG_ACLSETACLENTRYTRANSPORTPORTNAMEDAUG));
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.subtreeAdd(IIDs.AC_AC_ACLSET, new AclReader(cli),
            Sets.newHashSet(IIDs.AC_AC_AC_CONFIG,
                IIDs.AC_AC_AC_AC_ACLENTRY,
                IIDs.AC_AC_AC_AC_AC_CONFIG,
                IIDs.AC_AC_AC_CO_AUG_VRPACLSETAUG,
                IIDs.AC_AC_AC_AC_AC_ACTIONS,
                IIDs.AC_AC_AC_AC_AC_AC_CONFIG,

                IIDs.AC_AC_AC_AC_AC_IPV4,
                IIDs.AC_AC_AC_AC_AC_IP_CONFIG,

                IIDs.AC_AC_AC_AC_AC_AUG_ACLENTRY1,
                IIDs.AC_AC_AC_AC_AC_AUG_ACLENTRY1_IC_CONFIG
            ));
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return Collections.singleton(HUAWEI);
    }

    @Override
    protected String getUnitName() {
        return "VRP ACL (Openconfig) translate unit";
    }
}
