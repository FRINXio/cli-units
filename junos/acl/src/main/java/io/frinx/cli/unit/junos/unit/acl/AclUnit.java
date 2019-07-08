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

package io.frinx.cli.unit.junos.unit.acl;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.junos.init.JunosDevices;
import io.frinx.cli.unit.junos.unit.acl.handler.AclInterfaceConfigReader;
import io.frinx.cli.unit.junos.unit.acl.handler.AclInterfaceReader;
import io.frinx.cli.unit.junos.unit.acl.handler.AclInterfaceWriter;
import io.frinx.cli.unit.junos.unit.acl.handler.IngressAclSetConfigReader;
import io.frinx.cli.unit.junos.unit.acl.handler.IngressAclSetConfigWriter;
import io.frinx.cli.unit.junos.unit.acl.handler.IngressAclSetReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.acl.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class AclUnit extends AbstractUnit {

    public AclUnit(@Nonnull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return JunosDevices.JUNOS_ALL;
    }

    @Override
    protected String getUnitName() {
        return "Junos CLI ACL unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_OPENCONFIG_ACL);
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

        // interface
        writeRegistry.add(IIDs.AC_IN_INTERFACE, new AclInterfaceWriter());
        writeRegistry.addAfter(IIDs.AC_IN_IN_CONFIG, new NoopCliWriter<>(),
            io.frinx.openconfig.openconfig.interfaces.IIDs.IN_IN_SU_SU_CONFIG);

        // ingress
        writeRegistry.add(IIDs.AC_IN_IN_INGRESSACLSETS, new NoopCliWriter<>());
        writeRegistry.add(IIDs.AC_IN_IN_IN_INGRESSACLSET, new NoopCliListWriter<>());
        writeRegistry.addAfter(IIDs.AC_IN_IN_IN_IN_CONFIG, new IngressAclSetConfigWriter(cli),
            IIDs.AC_IN_IN_CONFIG);
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        // interface
        readRegistry.add(IIDs.AC_IN_INTERFACE, new AclInterfaceReader(cli));
        readRegistry.add(IIDs.AC_IN_IN_CONFIG, new AclInterfaceConfigReader());

        // ingress
        readRegistry.add(IIDs.AC_IN_IN_IN_INGRESSACLSET, new IngressAclSetReader(cli));
        readRegistry.add(IIDs.AC_IN_IN_IN_IN_CONFIG, new IngressAclSetConfigReader());
    }
}
