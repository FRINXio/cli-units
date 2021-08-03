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

package io.frinx.cli.unit.huawei.aaa;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.huawei.aaa.handler.accounting.AccountingSchemasConfigReader;
import io.frinx.cli.unit.huawei.aaa.handler.accounting.AccountingSchemasConfigWriter;
import io.frinx.cli.unit.huawei.aaa.handler.accounting.AccountingSchemasReader;
import io.frinx.cli.unit.huawei.aaa.handler.authentication.AuthenticationSchemasConfigReader;
import io.frinx.cli.unit.huawei.aaa.handler.authentication.AuthenticationSchemasConfigWriter;
import io.frinx.cli.unit.huawei.aaa.handler.authentication.AuthenticationSchemasReader;
import io.frinx.cli.unit.huawei.aaa.handler.domain.DomainListConfigReader;
import io.frinx.cli.unit.huawei.aaa.handler.domain.DomainListConfigWriter;
import io.frinx.cli.unit.huawei.aaa.handler.domain.DomainListReader;
import io.frinx.cli.unit.huawei.aaa.handler.users.UsersListConfigReader;
import io.frinx.cli.unit.huawei.aaa.handler.users.UsersListConfigWriter;
import io.frinx.cli.unit.huawei.aaa.handler.users.UsersListReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.aaa.IIDs;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class VrpAaaUnit extends AbstractUnit {

    private static final Device HUAWEI = new DeviceIdBuilder()
            .setDeviceType("vrp")
            .setDeviceVersion("*")
            .build();

    public VrpAaaUnit(@Nonnull TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_OPENCONFIG_AAA, IIDs.FRINX_OPENCONFIG_EXTENSIONS,
                IIDs.FRINX_HUAWEI_AAA_EXTENSION, $YangModuleInfoImpl.getInstance());
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
        writeRegistry.addNoop(IIDs.AA_AU_US_USER);
        writeRegistry.subtreeAddAfter(IIDs.AA_AU_US_US_CONFIG, new UsersListConfigWriter(cli),
                Sets.newHashSet(IIDs.AA_AU_US_US_CO_AUG_AAAHUAWEIUSERAUG));

        writeRegistry.addNoop(IIDs.AA_AUG_AAAHUAWEISCHEMASAUG);
        writeRegistry.addNoop(IIDs.AA_AUG_AAAHUAWEISCHEMASAUG_DO_DOMAIN);
        writeRegistry.add(IIDs.AA_AUG_AAAHUAWEISCHEMASAUG_DO_DO_CONFIG, new DomainListConfigWriter(cli));

        writeRegistry.addNoop(IIDs.AA_AUG_AAAHUAWEISCHEMASAUG_AU_AUTHENTICATION);
        writeRegistry.subtreeAddAfter(IIDs.AA_AUG_AAAHUAWEISCHEMASAUG_AU_AU_CONFIG,
                new AuthenticationSchemasConfigWriter(cli),
                Sets.newHashSet(IIDs.AA_AUG_AAAHUAWEISCHEMASAUG_AU_AU_CO_AU_CONFIG));

        writeRegistry.addNoop(IIDs.AA_AUG_AAAHUAWEISCHEMASAUG_AC_ACCOUNT);
        writeRegistry.subtreeAddAfter(IIDs.AA_AUG_AAAHUAWEISCHEMASAUG_AC_AC_CONFIG,
                new AccountingSchemasConfigWriter(cli),
                Sets.newHashSet(IIDs.AA_AUG_AAAHUAWEISCHEMASAUG_AC_AC_CO_AC_CONFIG));
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.AA_AU_US_USER, new UsersListReader(cli));
        readRegistry.add(IIDs.AA_AU_US_US_CONFIG, new UsersListConfigReader(cli));
        readRegistry.add(IIDs.AA_AUG_AAAHUAWEISCHEMASAUG_DO_DOMAIN, new DomainListReader(cli));
        readRegistry.add(IIDs.AA_AUG_AAAHUAWEISCHEMASAUG_DO_DO_CONFIG, new DomainListConfigReader(cli));
        readRegistry.add(IIDs.AA_AUG_AAAHUAWEISCHEMASAUG_AU_AUTHENTICATION, new AuthenticationSchemasReader(cli));
        readRegistry.add(IIDs.AA_AUG_AAAHUAWEISCHEMASAUG_AU_AU_CONFIG, new AuthenticationSchemasConfigReader(cli));
        readRegistry.add(IIDs.AA_AUG_AAAHUAWEISCHEMASAUG_AC_ACCOUNT, new AccountingSchemasReader(cli));
        readRegistry.add(IIDs.AA_AUG_AAAHUAWEISCHEMASAUG_AC_AC_CONFIG, new AccountingSchemasConfigReader(cli));
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return Collections.singleton(HUAWEI);
    }

    @Override
    protected String getUnitName() {
        return "VRP AAA (Openconfig) translate unit";
    }
}
