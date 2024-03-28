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

package io.frinx.cli.unit.huawei.snmp;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.huawei.snmp.handler.CommunityConfigReader;
import io.frinx.cli.unit.huawei.snmp.handler.CommunityConfigWriter;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.snmp.IIDs;
import java.util.Collections;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class SnmpUnit extends AbstractUnit {

    public SnmpUnit(@NotNull final TranslationUnitCollector registry) {
        super(registry);
    }

    private static final Device HUAWEI = new DeviceIdBuilder()
            .setDeviceType("vrp")
            .setDeviceVersion("*")
            .build();

    @Override
    protected String getUnitName() {
        return "HUAWEI SNMP unit";
    }

    @Override
    public void provideHandlers(@NotNull CustomizerAwareReadRegistryBuilder readRegistry,
                                @NotNull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @NotNull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.addNoop(IIDs.SN_AUG_SNMPHUAWEIAUG);
        writeRegistry.add(IIDs.SN_AUG_SNMPHUAWEIAUG_CONFIG, new CommunityConfigWriter(cli));
    }

    private void provideReaders(@NotNull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.SN_AUG_SNMPHUAWEIAUG_CONFIG, new CommunityConfigReader(cli));
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet($YangModuleInfoImpl.getInstance(), IIDs.FRINX_SNMP, IIDs.FRINX_HUAWEI_SNMP_EXTENSION);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return Collections.singleton(HUAWEI);
    }
}