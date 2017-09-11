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
import io.frinx.cli.unit.utils.InitCliReader;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.AddressBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.Config;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;

import static io.frinx.cli.unit.ios.ifc.subifc.Ipv4AddressReader.INTERFACE_IP_LINE;
import static io.frinx.cli.unit.ios.ifc.subifc.Ipv4AddressReader.SH_INTERFACE_IP;
import static io.frinx.cli.unit.utils.ParsingUtils.parseField;

public class Ipv4ConfigReader implements InitCliReader<Config, ConfigBuilder> {

    private Cli cli;

    public Ipv4ConfigReader(Cli cli) {
        this.cli = cli;
    }

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
        parseAddressConfig(configBuilder, blockingRead(String.format(SH_INTERFACE_IP, name), cli, id, readContext));
    }

    @VisibleForTesting
    static void parseAddressConfig(ConfigBuilder configBuilder, String output) {
        parseField(output,
                INTERFACE_IP_LINE::matcher,
                m -> new Ipv4AddressNoZone(m.group("ip")),
                configBuilder::setIp);

        parseField(output,
                INTERFACE_IP_LINE::matcher,
                m -> Short.parseShort(m.group("prefix")),
                configBuilder::setPrefixLength);
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Config config) {
        ((AddressBuilder) builder).setConfig(config);
    }

    @Nonnull
    @Override
    public Initialized<? extends DataObject> init(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                                  @Nonnull Config config,
                                                  @Nonnull ReadContext readContext) {
        return Initialized.create(instanceIdentifier, config);
    }
}
