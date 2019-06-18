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

package io.frinx.cli.ospf;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ospf.handler.AreaConfigReader;
import io.frinx.cli.ospf.handler.AreaInterfaceConfigReader;
import io.frinx.cli.ospf.handler.AreaInterfaceConfigWriter;
import io.frinx.cli.ospf.handler.AreaInterfaceReader;
import io.frinx.cli.ospf.handler.AreaStateReader;
import io.frinx.cli.ospf.handler.GlobalConfigReader;
import io.frinx.cli.ospf.handler.GlobalConfigWriter;
import io.frinx.cli.ospf.handler.GlobalStateReader;
import io.frinx.cli.ospf.handler.OspfAreaReader;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.ios.init.IosDevices;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class OspfUnit extends AbstractUnit {

    public OspfUnit(@Nonnull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return IosDevices.IOS_ALL;
    }

    @Override
    protected String getUnitName() {
        return "IOS OSPF unit";
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
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_OS_GL_TI_MA_CONFIG);

        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_OS_AR_AREA);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_OS_AR_AR_CONFIG);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_INTERFACE);
        writeRegistry.addAfter(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_CONFIG, new AreaInterfaceConfigWriter(cli),
                IIDs.NE_NE_PR_PR_OS_AR_AR_CONFIG, IIDs.NE_NE_IN_IN_CONFIG, IIDs.NE_NE_PR_PR_OS_GL_CONFIG);

        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_OSPFV2);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_OS_GLOBAL);
        writeRegistry.addAfter(IIDs.NE_NE_PR_PR_OS_GL_CONFIG, new GlobalConfigWriter(cli), IIDs.NE_NE_PR_PR_CONFIG);

        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_INTERFACEREF);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_IN_CONFIG);
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.NE_NE_PR_PR_OS_GL_CONFIG, new GlobalConfigReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_OS_GL_STATE, new GlobalStateReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_OS_AR_AREA, new OspfAreaReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_OS_AR_AR_CONFIG, new AreaConfigReader());
        readRegistry.add(IIDs.NE_NE_PR_PR_OS_AR_AR_STATE, new AreaStateReader());
        readRegistry.add(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_INTERFACE, new AreaInterfaceReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_CONFIG, new AreaInterfaceConfigReader(cli));
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(io.frinx.openconfig.openconfig.ospf.IIDs.FRINX_OPENCONFIG_OSPFV2,
                $YangModuleInfoImpl.getInstance());
    }
}
