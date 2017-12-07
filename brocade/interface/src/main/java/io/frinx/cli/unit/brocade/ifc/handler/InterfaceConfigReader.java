/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.brocade.ifc.handler;

import static com.google.common.base.Preconditions.checkArgument;
import static io.frinx.cli.unit.utils.ParsingUtils.parseField;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
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

    @Nonnull
    @Override
    public ConfigBuilder getBuilder(@Nonnull final InstanceIdentifier<Config> id) {
        return new ConfigBuilder();
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
        Class<? extends InterfaceType> ifcType = parseType(name);
        String ifcNumber = getIfcNumber(name);
        parseInterface(blockingRead(String.format(SH_SINGLE_INTERFACE_CFG, getTypeOnDevice(ifcType), ifcNumber), cli, id, ctx), builder, name, ifcType);
    }

    public static String getIfcNumber(String name) {
        Matcher matcher = IFC_NAME.matcher(name);
        checkArgument(matcher.matches(), "Interface name %s in unexpected format. Expected format: GigabitEthernet1/0", name);
        return matcher.group("number");
    }

    public static final String SH_SINGLE_INTERFACE_CFG = "sh run int %s %s";

    public static final Pattern IFC_NAME = Pattern.compile("[^a-zA-Z]*[a-zA-Z]+(?<number>[^a-zA-Z]+)");

    public static final Pattern SHUTDOWN_LINE = Pattern.compile("enable");
    public static final Pattern MTU_LINE = Pattern.compile("\\s*mtu (?<mtu>.+)$");
    public static final Pattern DESCR_LINE = Pattern.compile("\\s*port-name (?<desc>.+)");

    @VisibleForTesting
    public static void parseInterface(final String output, final ConfigBuilder builder, String name, Class<? extends InterfaceType> ifcType) {
        // Set disabled unless proven otherwise
        builder.setEnabled(false);
        builder.setName(name);
        builder.setType(ifcType);

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

    public static final Set<String> ETHERNET_NAME_PREFIX = Sets.newHashSet(
            "GigabitEther", "10GigabitEther", "FastEther", "Ethernetmgmt");

    public static Class<? extends InterfaceType> parseType(String name) {
        for (String ethernetNamePrefix : ETHERNET_NAME_PREFIX) {
            if (name.startsWith(ethernetNamePrefix)) {
                return EthernetCsmacd.class;
            }
        }

        if (name.startsWith("Loopback")) {
            return SoftwareLoopback.class;
        } else {
            return Other.class;
        }
    }

    public static final Map<Class<? extends InterfaceType>, String> IFC_TYPE_MAP = new HashMap<>();
    static {
        IFC_TYPE_MAP.put(EthernetCsmacd.class, "ethernet");
        IFC_TYPE_MAP.put(SoftwareLoopback.class, "loopback");
    }

    public static String getTypeOnDevice(Class<? extends InterfaceType> openconfigType) {
        return IFC_TYPE_MAP.getOrDefault(openconfigType, "other");
    }
}
