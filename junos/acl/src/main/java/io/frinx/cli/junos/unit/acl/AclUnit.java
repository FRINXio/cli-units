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

package io.frinx.cli.junos.unit.acl;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.write.GenericListWriter;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.junos.JunosDevices;
import io.frinx.cli.junos.unit.acl.handler.AclInterfaceConfigReader;
import io.frinx.cli.junos.unit.acl.handler.AclInterfaceReader;
import io.frinx.cli.junos.unit.acl.handler.AclInterfaceWriter;
import io.frinx.cli.junos.unit.acl.handler.IngressAclSetConfigReader;
import io.frinx.cli.junos.unit.acl.handler.IngressAclSetConfigWriter;
import io.frinx.cli.junos.unit.acl.handler.IngressAclSetReader;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.acl.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.IngressAclSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.top.AclBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class AclUnit implements TranslateUnit {
    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public AclUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        reg = registry.registerTranslateUnit(JunosDevices.JUNOS_ALL, this);
    }

    public void close() {
        if (reg != null) {
            reg.close();
        }
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526
                        .$YangModuleInfoImpl.getInstance()
        );
    }

    @Override
    public Set<RpcService<?, ?>> getRpcs(@Nonnull final Context context) {
        return Sets.newHashSet();
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
        writeRegistry.add(new GenericWriter<>(IIDs.ACL, new NoopCliWriter<>()));

        // interface
        writeRegistry.add(new GenericListWriter<>(IIDs.AC_IN_INTERFACE, new AclInterfaceWriter()));
        writeRegistry.addAfter(new GenericWriter<>(IIDs.AC_IN_IN_CONFIG, new NoopCliWriter<>()),
            io.frinx.openconfig.openconfig.interfaces.IIDs.IN_IN_SU_SU_CONFIG);

        // ingress
        writeRegistry.add(new GenericWriter<>(IIDs.AC_IN_IN_INGRESSACLSETS, new NoopCliWriter<>()));
        writeRegistry.add(new GenericListWriter<>(IIDs.AC_IN_IN_IN_INGRESSACLSET, new NoopCliListWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(IIDs.AC_IN_IN_IN_IN_CONFIG, new IngressAclSetConfigWriter(cli)),
            IIDs.AC_IN_IN_CONFIG);
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.addStructuralReader(IIDs.ACL, AclBuilder.class);

        // interface
        readRegistry.addStructuralReader(IIDs.AC_INTERFACES, InterfacesBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.AC_IN_INTERFACE, new AclInterfaceReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.AC_IN_IN_CONFIG, new AclInterfaceConfigReader()));

        // ingress
        readRegistry.addStructuralReader(IIDs.AC_IN_IN_INGRESSACLSETS, IngressAclSetsBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.AC_IN_IN_IN_INGRESSACLSET, new IngressAclSetReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.AC_IN_IN_IN_IN_CONFIG, new IngressAclSetConfigReader()));
    }

    @Override
    public String toString() {
        return "Junos CLI ACL unit";
    }
}
