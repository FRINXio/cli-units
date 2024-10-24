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

package io.frinx.cli.unit.dasan.ifc.handler.ethernet;

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.dasan.ifc.handler.ethernet.lacpadminkey.BundleEtherLacpAdminkeyConfigReader;
import io.frinx.cli.unit.dasan.ifc.handler.ethernet.lacpinterval.BundleEtherLacpIntervalConfigReader;
import io.frinx.cli.unit.dasan.ifc.handler.ethernet.lacpmember.BundleEtherLacpMemberConfigReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.EthernetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.ConfigBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class EthernetConfigReader extends CompositeReader<Config, ConfigBuilder>
        implements CliConfigReader<Config, ConfigBuilder> {

    public EthernetConfigReader(Cli cli) {
        super(List.of(
            new BundleEtherLacpMemberConfigReader(cli),
            new BundleEtherLacpAdminkeyConfigReader(cli),
            new BundleEtherLacpIntervalConfigReader(cli)
        ));
    }

    @NotNull
    @Override
    public ConfigBuilder getBuilder(@NotNull InstanceIdentifier<Config> instanceIdentifier) {
        return new ConfigBuilder();
    }

    @Override
    public void merge(@NotNull final Builder<? extends DataObject> builder, @NotNull final Config value) {
        ((EthernetBuilder) builder).setConfig(value);
    }
}