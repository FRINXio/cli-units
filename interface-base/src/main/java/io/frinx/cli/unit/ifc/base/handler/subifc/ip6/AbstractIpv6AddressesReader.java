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

package io.frinx.cli.unit.ifc.base.handler.subifc.ip6;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.subifc.AbstractSubinterfaceReader;
import io.frinx.cli.unit.ifc.base.util.NetUtils;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.AddressBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.AddressKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6AddressNoZone;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public abstract class AbstractIpv6AddressesReader implements CliConfigListReader<Address, AddressKey, AddressBuilder> {

    protected final Cli cli;

    public AbstractIpv6AddressesReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<AddressKey> getAllIds(@NotNull InstanceIdentifier<Address> instanceIdentifier,
                                      @NotNull ReadContext ctx) throws ReadFailedException {
        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        Long subId = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();

        // Only subinterface with ID ZERO_SUBINTERFACE_ID can have IP
        if (subId == AbstractSubinterfaceReader.ZERO_SUBINTERFACE_ID) {
            return parseAddressIds(blockingRead(getReadCommand(ifcName), cli, instanceIdentifier, ctx));
        } else {
            return Collections.emptyList();
        }
    }

    protected abstract String getReadCommand(String ifcName);

    @VisibleForTesting
    public List<AddressKey> parseAddressIds(String output) {
        List<AddressKey> addressKeys = new ArrayList<>();
        addressKeys.addAll(ParsingUtils.parseFields(output, 0,
            getLocalIpLine()::matcher,
            m -> m.group("ipv6local"),
            addr -> new AddressKey(new Ipv6AddressNoZone(addr))));
        addressKeys.addAll(ParsingUtils.parseFields(output, 0,
            getUnicastIpLine()::matcher,
            m -> m.group("ipv6unicast"),
            addr -> new AddressKey(new Ipv6AddressNoZone(addr))));
        return addressKeys;
    }

    protected abstract Pattern getLocalIpLine();

    protected Pattern getUnicastIpLine() {
        return NetUtils.NO_MATCH;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Address> instanceIdentifier,
                                      @NotNull AddressBuilder addressBuilder,
                                      @NotNull ReadContext ctx) {
        Long subId = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();

        // Only subinterface with ID ZERO_SUBINTERFACE_ID can have IP
        if (subId == AbstractSubinterfaceReader.ZERO_SUBINTERFACE_ID) {
            addressBuilder.setIp(instanceIdentifier.firstKeyOf(Address.class).getIp());
        }
    }
}