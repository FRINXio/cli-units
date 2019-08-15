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

package io.frinx.cli.unit.ifc.base.handler.subifc.ip4;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.subifc.AbstractSubinterfaceReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
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

public abstract class AbstractIpv4AddressesReader implements CliConfigListReader<Address, AddressKey, AddressBuilder> {

    private final Cli cli;

    public AbstractIpv4AddressesReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<AddressKey> getAllIds(@Nonnull InstanceIdentifier<Address> instanceIdentifier,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        Long subId = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();

        if (isSupportedInterface(instanceIdentifier)) {
            return parseAddressIds(blockingRead(getReadCommand(getInterfaceName(ifcName, subId)),
                cli, instanceIdentifier, ctx));
        } else {
            return Collections.emptyList();
        }
    }

    protected String getInterfaceName(String ifcName, Long subId) {
     // Typically interface(not sub-interface) has IP.
        return ifcName;
    }

    protected abstract String getReadCommand(String ifcName);

    @VisibleForTesting
    public List<AddressKey> parseAddressIds(String output) {
        return ParsingUtils.parseFields(output, 0,
            getIpLine()::matcher,
            m -> m.group("ip"),
            addr -> new AddressKey(new Ipv4AddressNoZone(addr)));
    }

    protected abstract Pattern getIpLine();

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder,
                      @Nonnull List<Address> list) {
        ((AddressesBuilder) builder).setAddress(list);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Address> instanceIdentifier,
                                      @Nonnull AddressBuilder addressBuilder,
                                      @Nonnull ReadContext ctx) {

        if (isSupportedInterface(instanceIdentifier)) {
            addressBuilder.setIp(instanceIdentifier.firstKeyOf(Address.class).getIp());
        }
    }

    @VisibleForTesting
    public boolean isSupportedInterface(InstanceIdentifier<Address> instanceIdentifier) {
        // Only subinterface with ID ZERO_SUBINTERFACE_ID can have IP
        return AbstractSubinterfaceReader.isSubInterfaceZero(instanceIdentifier);
    }
}
