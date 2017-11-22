/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip6;

import static io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip6.Ipv6AddressReader.IPV6_UNICAST_ADDRESS;
import static io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip6.Ipv6AddressReader.SH_INTERFACE_IP;
import static io.frinx.cli.unit.utils.ParsingUtils.NEWLINE;
import static io.frinx.cli.unit.utils.ParsingUtils.parseField;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import java.util.Arrays;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.Address;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.AddressBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.address.Config;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.address.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6AddressNoZone;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv6ConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private Cli cli;

    public Ipv6ConfigReader(Cli cli) {
        this.cli = cli;
    }

    public static final short DEFAULT_PREFIX_LENGHT = (short)64;

    @Nonnull
    @Override
    public ConfigBuilder getBuilder(@Nonnull InstanceIdentifier<Config> instanceIdentifier) {
        return new ConfigBuilder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String name = id.firstKeyOf(Interface.class).getName();
        Long subId = id.firstKeyOf(Subinterface.class).getIndex();

        // Only subinterface with ID ZERO_SUBINTERFACE_ID can have IP
        if (subId == SubinterfaceReader.ZERO_SUBINTERFACE_ID) {
            Ipv6AddressNoZone address = id.firstKeyOf(Address.class).getIp();
            parseAddressConfig(configBuilder, blockingRead(String.format(SH_INTERFACE_IP, name), cli, id, readContext), address);
        }
    }

    @VisibleForTesting
    static void parseAddressConfig(ConfigBuilder configBuilder, String output, Ipv6AddressNoZone address) {
        configBuilder.setIp(address);
        configBuilder.setPrefixLength(DEFAULT_PREFIX_LENGHT);
        output = String.valueOf(
                Arrays.stream(output.split(NEWLINE.pattern())).filter(line -> line.contains(address.getValue())).findAny());
        parseField(output,
                IPV6_UNICAST_ADDRESS::matcher,
                m -> Short.parseShort(m.group("prefix")),
                configBuilder::setPrefixLength);
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Config config) {
        ((AddressBuilder) builder).setConfig(config);
    }
}
