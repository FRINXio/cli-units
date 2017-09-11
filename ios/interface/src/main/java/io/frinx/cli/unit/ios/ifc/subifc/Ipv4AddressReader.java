/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.ifc.subifc;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.read.Initialized;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.InitCliListReader;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.AddressesBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.Address;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.AddressBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.AddressKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.regex.Pattern;

import static io.frinx.cli.unit.utils.ParsingUtils.parseFields;

public class Ipv4AddressReader implements InitCliListReader<Address, AddressKey, AddressBuilder> {

    private final Cli cli;

    public Ipv4AddressReader(Cli cli) {
        this.cli = cli;
    }

    static final String SH_INTERFACE_IP = "sh ip inter %s | include Internet address";
    static final Pattern INTERFACE_IP_LINE =
            Pattern.compile("Internet address is (?<ip>[^/]+)/(?<prefix>[0-9]+)");

    @Nonnull
    @Override
    public List<AddressKey> getAllIds(@Nonnull InstanceIdentifier<Address> instanceIdentifier,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String id = instanceIdentifier.firstKeyOf(Interface.class).getName();
        return parseAddressIds(blockingRead(String.format(SH_INTERFACE_IP, id), cli, instanceIdentifier, readContext));
    }

    @VisibleForTesting
    static List<AddressKey> parseAddressIds(String output) {
        return parseFields(output,0,
                INTERFACE_IP_LINE::matcher,
                m -> m.group("ip"),
                addr -> new AddressKey(new Ipv4AddressNoZone(addr)));
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder,
                      @Nonnull List<Address> list) {
        ((AddressesBuilder) builder).setAddress(list);
    }

    @Nonnull
    @Override
    public AddressBuilder getBuilder(@Nonnull InstanceIdentifier<Address> instanceIdentifier) {
        return new AddressBuilder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Address> instanceIdentifier,
                                      @Nonnull AddressBuilder addressBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        addressBuilder.setIp(instanceIdentifier.firstKeyOf(Address.class).getIp());
    }

    @Nonnull
    @Override
    public Initialized<? extends DataObject> init(@Nonnull InstanceIdentifier<Address> instanceIdentifier,
                                                  @Nonnull Address address,
                                                  @Nonnull ReadContext readContext) {
        // Direct
        return Initialized.create(instanceIdentifier, address);
    }
}
