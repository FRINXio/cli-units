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

package io.frinx.cli.unit.junos.ifc.handler.subifc.ip4;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.ifc.base.handler.subifc.ip4.AbstractIpv4AddressesReader;
import io.frinx.cli.io.Cli;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.AddressBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.AddressKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class Ipv4AddressReader extends AbstractIpv4AddressesReader {

    static final String SH_RUN_INT_IP =
        "show configuration interfaces %s unit %d | display set | match \" inet address \"";

    static final Pattern INTERFACE_IP_LINE = Pattern
        .compile("set interfaces (?<ifcId>.+) unit (?<subifcIndex>[0-9]+) family "
            + "inet address (?<ip>[^/]+)/(?<prefix>[0-9]+)");

    private final Cli cli;

    public Ipv4AddressReader(Cli cli) {
        super(cli);
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<AddressKey> getAllIds(@Nonnull InstanceIdentifier<Address> instanceIdentifier,
        @Nonnull ReadContext ctx) throws ReadFailedException {

        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        Long subId = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();

        return parseAddressIds(blockingRead(f(SH_RUN_INT_IP, ifcName, subId), cli, instanceIdentifier, ctx));
    }

    @Override
    protected String getReadCommand(String ifcName) {
        throw new UnsupportedOperationException("getAllIds method from parent should never be used.");
    }

    @Override
    protected Pattern getIpLine() {
        return INTERFACE_IP_LINE;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Address> instanceIdentifier,
        @Nonnull AddressBuilder addressBuilder, @Nonnull ReadContext readContext) {
        addressBuilder.setIp(instanceIdentifier.firstKeyOf(Address.class).getIp());
    }
}
