/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.unit.acl;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.read.GenericListReader;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.unit.acl.handler.*;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.acl.IIDs;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.EgressAclSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.IngressAclSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.AclSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.top.AclBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

import javax.annotation.Nonnull;
import java.util.Set;

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
        rRegistry.add(new GenericListReader<>(IIDs.AC_IN_INTERFACE, new AclInterfaceReader(cli)));
        rRegistry.add(new GenericConfigReader<>(IIDs.AC_IN_IN_CONFIG, new AclInterfaceConfigReader()));

        // ingress
        rRegistry.addStructuralReader(IIDs.AC_IN_IN_INGRESSACLSETS, IngressAclSetsBuilder.class);
        rRegistry.add(new GenericListReader<>(IIDs.AC_IN_IN_IN_INGRESSACLSET, new IngressAclSetReader(cli)));
        rRegistry.add(new GenericConfigReader<>(IIDs.AC_IN_IN_IN_IN_CONFIG, new IngressAclSetConfigReader()));

        // egress
        rRegistry.addStructuralReader(IIDs.AC_IN_IN_EGRESSACLSETS, EgressAclSetsBuilder.class);
        rRegistry.add(new GenericListReader<>(IIDs.AC_IN_IN_EG_EGRESSACLSET, new EgressAclSetReader(cli)));
        rRegistry.add(new GenericConfigReader<>(IIDs.AC_IN_IN_EG_EG_CONFIG, new EgressAclSetConfigReader()));

        // sets
        rRegistry.addStructuralReader(IIDs.AC_ACLSETS, AclSetsBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(IIDs.AC_AC_ACLSET, new AclAllReader(cli)));
    }

    @Override
    public String toString() {
        return "IOS XR ACL unit";
    }
}
