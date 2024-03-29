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

package io.frinx.cli.unit.dasan.ifc.handler.subifc.ip4;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.dasan.ifc.handler.subifc.SubinterfaceReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.AddressBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv4AddressConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private Cli cli;

    public Ipv4AddressConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull ConfigBuilder configBuilder,
            @NotNull ReadContext readContext) throws ReadFailedException {
        String name = id.firstKeyOf(Interface.class).getName();
        if (checkSubId(id)) {
            parseAddressConfig(configBuilder,
                    blockingRead(String.format(Ipv4AddressReader.DISPLAY_IP_INT_BRIEF, name.replace("Vlan", "br")), cli,
                            id, readContext));
        }
    }

    static boolean checkSubId(@NotNull InstanceIdentifier<?> id) {
        Long subId = id.firstKeyOf(Subinterface.class).getIndex();
        return subId == SubinterfaceReader.ZERO_SUBINTERFACE_ID;
    }

    @VisibleForTesting
    static void parseAddressConfig(ConfigBuilder configBuilder, String output) {
        ParsingUtils.parseField(output, Ipv4AddressReader.INTERFACE_IP_LINE::matcher,
            m -> new Ipv4AddressNoZone(m.group("ip")), configBuilder::setIp);

        ParsingUtils.parseField(output, Ipv4AddressReader.INTERFACE_IP_LINE::matcher,
            m -> Short.parseShort(m.group("prefix")), configBuilder::setPrefixLength);
    }

    @Override
    public void merge(@NotNull Builder<? extends DataObject> builder, @NotNull Config config) {
        ((AddressBuilder) builder).setConfig(config);
    }
}