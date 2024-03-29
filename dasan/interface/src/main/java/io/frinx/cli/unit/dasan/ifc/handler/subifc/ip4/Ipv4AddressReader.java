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
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.AddressesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.AddressBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.AddressKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv4AddressReader implements CliConfigListReader<Address, AddressKey, AddressBuilder> {

    private final Cli cli;
    static final String DISPLAY_IP_INT_BRIEF = "show running-config interface %s";
    static final Pattern INTERFACE_IP_LINE = Pattern.compile("^\\s*ip address (?<ip>[^/]+)/(?<prefix>[0-9]+)");

    public Ipv4AddressReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<AddressKey> getAllIds(@NotNull InstanceIdentifier<Address> instanceIdentifier,
            @NotNull ReadContext readContext) throws ReadFailedException {
        String name = instanceIdentifier.firstKeyOf(Interface.class).getName();
        if (Ipv4AddressConfigReader.checkSubId(instanceIdentifier)) {
            return parseAddressIds(blockingRead(String.format(DISPLAY_IP_INT_BRIEF, name.replace("Vlan", "br")), cli,
                    instanceIdentifier, readContext));
        }
        return Collections.emptyList();
    }

    @VisibleForTesting
    public List<AddressKey> parseAddressIds(String output) {
        return ParsingUtils.parseFields(output, 0, INTERFACE_IP_LINE::matcher, m -> m.group("ip"),
            addr -> new AddressKey(new Ipv4AddressNoZone(addr)));
    }

    @Override
    public void merge(@NotNull Builder<? extends DataObject> builder, @NotNull List<Address> list) {
        ((AddressesBuilder) builder).setAddress(list);
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Address> instanceIdentifier,
            @NotNull AddressBuilder addressBuilder, @NotNull ReadContext readContext) throws ReadFailedException {
        Long subId = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();

        if (subId == SubinterfaceReader.ZERO_SUBINTERFACE_ID) {
            addressBuilder.setIp(instanceIdentifier.firstKeyOf(Address.class).getIp());
        }
    }
}