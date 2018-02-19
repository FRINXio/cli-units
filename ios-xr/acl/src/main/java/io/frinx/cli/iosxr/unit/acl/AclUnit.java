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
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.AclSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.top.AclBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class AclUnit implements TranslateUnit {

    private static final Device IOS_ALL = new DeviceIdBuilder()
            .setDeviceType("ios xr")
            .setDeviceVersion("*")
            .build();

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public AclUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        reg = registry.registerTranslateUnit(IOS_ALL, this);
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
        rRegistry.add(new GenericConfigReader<>(IIDs.AC_AC_AC_CONFIG, new AclSetConfigReader()));
    }

    @Override
    public String toString() {
        return "IOS XR ACL unit";
    }
}
