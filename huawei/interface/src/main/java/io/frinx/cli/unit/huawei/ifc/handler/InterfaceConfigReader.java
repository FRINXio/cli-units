/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.huawei.ifc.handler;

import static io.frinx.cli.unit.utils.ParsingUtils.parseField;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Other;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.SoftwareLoopback;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class InterfaceConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private Cli cli;

    public InterfaceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void merge(@Nonnull final Builder<? extends DataObject> builder, @Nonnull final Config value) {
        ((InterfaceBuilder) builder).setConfig(value);
    }

    @Override
    public void readCurrentAttributes(@Nonnull final InstanceIdentifier<Config> id,
                                      @Nonnull final ConfigBuilder builder,
                                      @Nonnull final ReadContext ctx) throws ReadFailedException {
        String name = id.firstKeyOf(Interface.class).getName();
        parseInterface(blockingRead(String.format(SH_SINGLE_INTERFACE_CFG, name), cli, id, ctx), builder, name);
    }

    public static final String SH_SINGLE_INTERFACE_CFG = "display current-configuration interface %s";

    public static final Pattern SHUTDOWN_LINE = Pattern.compile("undo shutdown");
    public static final Pattern MTU_LINE = Pattern.compile("\\s*mtu (?<mtu>.+)\\s*");
    public static final Pattern DESCR_LINE = Pattern.compile("\\s*description (?<desc>.+)\\s*");

    @VisibleForTesting
    static void parseInterface(final String output, final ConfigBuilder builder, String name) {
        // Set disabled unless proven otherwise
        builder.setEnabled(false);
        builder.setName(name);
        builder.setType(parseType(name));

        // Actually check if disabled
        parseField(output, 0,
                SHUTDOWN_LINE::matcher,
                matcher -> true,
                builder::setEnabled);

        parseField(output,
                MTU_LINE::matcher,
                matcher -> Integer.valueOf(matcher.group("mtu")),
                builder::setMtu);

        parseField(output,
                DESCR_LINE::matcher,
                matcher -> matcher.group("desc"),
                builder::setDescription);
    }

    static Class<? extends InterfaceType> parseType(String name) {
        if (name.startsWith("GigabitEthernet")) {
            return EthernetCsmacd.class;
        }  else if (name.startsWith("LoopBack")) {
            return SoftwareLoopback.class;
        } else {
            return Other.class;
        }
    }
}
