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

package io.frinx.cli.unit.ios.ifc.handler.subifc.ip6;

import static io.frinx.cli.unit.utils.ParsingUtils.parseFields;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.ifc.handler.subifc.SubinterfaceReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.AddressesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.AddressBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.AddressKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6AddressNoZone;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv6AddressReader implements CliConfigListReader<Address, AddressKey, AddressBuilder> {

    private final Cli cli;

    public Ipv6AddressReader(Cli cli) {
        this.cli = cli;
    }

    static final String SH_INTERFACE_IP = "sh ipv6 inter %s";
    static final Pattern IPV6_LOCAL_ADDRESS = Pattern.compile(".*?link-local address is (?<ipv6local>[^\\s]+).*");
    static final Pattern IPV6_UNICAST_ADDRESS = Pattern.compile(".*?(?<ipv6unicast>[a-fA-F0-9:]+), subnet is [^/]+.*/(?<prefix>[^\\s]+).*");

    @Nonnull
    @Override
    public List<AddressKey> getAllIds(@Nonnull InstanceIdentifier<Address> instanceIdentifier,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String id = instanceIdentifier.firstKeyOf(Interface.class).getName();
        Long subId = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();

        // Only subinterface with ID ZERO_SUBINTERFACE_ID can have IP
        if (subId == SubinterfaceReader.ZERO_SUBINTERFACE_ID) {
            final String input = blockingRead(String.format(SH_INTERFACE_IP, id), cli, instanceIdentifier, readContext);
            return parseAddressIds(input);
        } else {
            return Collections.emptyList();
        }
    }

    @VisibleForTesting
    static List<AddressKey> parseAddressIds(String output) {
        List<AddressKey> addressKeys = new ArrayList<>();
        addressKeys.addAll(parseFields(output, 0,
                IPV6_LOCAL_ADDRESS::matcher,
                m -> m.group("ipv6local"),
                addr -> new AddressKey(new Ipv6AddressNoZone(addr))));
        addressKeys.addAll(parseFields(output, 0,
                IPV6_UNICAST_ADDRESS::matcher,
                m -> m.group("ipv6unicast"),
                addr -> new AddressKey(new Ipv6AddressNoZone(addr))));
        return addressKeys;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder,
                      @Nonnull List<Address> list) {
        ((AddressesBuilder) builder).setAddress(list);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Address> instanceIdentifier,
                                      @Nonnull AddressBuilder addressBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        Long subId = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();

        // Only subinterface with ID ZERO_SUBINTERFACE_ID can have IP
        if (subId == SubinterfaceReader.ZERO_SUBINTERFACE_ID) {
            addressBuilder.setIp(instanceIdentifier.firstKeyOf(Address.class).getIp());
        }
    }
}
