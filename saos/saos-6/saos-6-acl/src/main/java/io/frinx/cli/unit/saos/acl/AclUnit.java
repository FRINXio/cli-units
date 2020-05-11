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

package io.frinx.cli.unit.saos.acl;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.saos.acl.handler.AclEntryActionsConfigReader;
import io.frinx.cli.unit.saos.acl.handler.AclEntryConfigReader;
import io.frinx.cli.unit.saos.acl.handler.AclEntryReader;
import io.frinx.cli.unit.saos.acl.handler.AclEntryWriter;
import io.frinx.cli.unit.saos.acl.handler.AclSetConfigReader;
import io.frinx.cli.unit.saos.acl.handler.AclSetConfigWriter;
import io.frinx.cli.unit.saos.acl.handler.AclSetReader;
import io.frinx.cli.unit.saos.init.SaosDevices;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.acl.IIDs;

import java.util.Collections;
import java.util.HashSet;
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
        return new HashSet<Device>() {
            {
                add(SaosDevices.SAOS_6);
            }
        };
    }

    @Override
    protected String getUnitName() {
        return "Saos-6 Acl (Openconfig) translate unit";
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
        writeRegistry.addNoop(IIDs.AC_ACLSETS);
        writeRegistry.addNoop(IIDs.AC_AC_ACLSET);
        writeRegistry.subtreeAdd(IIDs.AC_AC_AC_CONFIG, new AclSetConfigWriter(cli),
                Collections.singleton(IIDs.AC_AC_AC_CO_AUG_SAOS6ACLSETAUG));
        writeRegistry.addNoop(IIDs.AC_AC_AC_ACLENTRIES);
        writeRegistry.subtreeAdd(IIDs.AC_AC_AC_AC_ACLENTRY, new AclEntryWriter(cli),
                Sets.newHashSet(IIDs.AC_AC_AC_AC_AC_CONFIG,
                        IIDs.AC_AC_AC_AC_AC_CO_AUG_CONFIG2,
                        IIDs.AC_AC_AC_AC_AC_ACTIONS,
                        IIDs.AC_AC_AC_AC_AC_AC_CONFIG));
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.AC_AC_ACLSET, new AclSetReader(cli));
        readRegistry.add(IIDs.AC_AC_AC_CONFIG, new AclSetConfigReader(cli));
        readRegistry.add(IIDs.AC_AC_AC_AC_ACLENTRY, new AclEntryReader(cli));
        readRegistry.add(IIDs.AC_AC_AC_AC_AC_CONFIG, new AclEntryConfigReader(cli));
        readRegistry.add(IIDs.AC_AC_AC_AC_AC_AC_CONFIG, new AclEntryActionsConfigReader(cli));
    }
}
