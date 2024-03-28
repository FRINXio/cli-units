/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.iosxe.ifc.handler.subifc.ip6;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.cisco.vrrp.ext.rev210521.Config2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.cisco.vrrp.ext.rev210521.Config2Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.cisco.vrrp.ext.rev210521.ipv6.vrrp.group.config.TrackedObjects;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.cisco.vrrp.ext.rev210521.ipv6.vrrp.group.config.TrackedObjectsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ip.vrrp.top.vrrp.VrrpGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ip.vrrp.top.vrrp.VrrpGroupKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ip.vrrp.top.vrrp.vrrp.group.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ip.vrrp.top.vrrp.vrrp.group.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv6VrrpGroupConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_INTERFACE_VRRP = "show running-config interface %s "
            + "| section vrrp %s address-family ipv6";
    private static final Pattern TRACK_SHUTDOWN_LINE = Pattern.compile("track (?<object>.+) shutdown");
    private static final Pattern PRIMARY_ADDRESS_LINE = Pattern.compile("address (?<ip>.+) primary");
    private static final Pattern ADDRESS_LINE = Pattern.compile("address (?<ip>\\S+\\/\\S+)");

    private final Cli cli;

    public Ipv6VrrpGroupConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                      @NotNull ConfigBuilder builder,
                                      @NotNull ReadContext ctx) throws ReadFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();
        VrrpGroupKey key = id.firstKeyOf(VrrpGroup.class);

        final String output = blockingRead(f(SH_INTERFACE_VRRP, ifcName, key.getVirtualRouterId()), cli, id, ctx);
        parseVrrpGroupConfig(output, builder, key.getVirtualRouterId());
    }

    @VisibleForTesting
    static void parseVrrpGroupConfig(String output, ConfigBuilder builder, Short virtualRouterId) {
        builder.setVirtualRouterId(virtualRouterId);
        setPrimaryAddress(output, builder);

        Config2Builder configAug = new Config2Builder();
        setTrackedObjects(output, configAug);
        setAddress(output, configAug);
        if (!configAug.equals(new Config2Builder())) {
            builder.addAugmentation(Config2.class, configAug.build());
        }
    }

    private static void setAddress(String output, Config2Builder configAug) {
        configAug.setAddresses(ParsingUtils.parseFields(output, 0,
            ADDRESS_LINE::matcher,
            m -> m.group("ip"),
            ip -> new Ipv6Prefix(ip)));
    }

    private static void setTrackedObjects(String output, Config2Builder configAug) {
        List<TrackedObjects> trackedObjects = new ArrayList<>();
        setShutdown(output, trackedObjects);
        configAug.setTrackedObjects(trackedObjects);
    }

    private static void setPrimaryAddress(String output, ConfigBuilder builder) {
        ParsingUtils.parseFields(output, 0,
            PRIMARY_ADDRESS_LINE::matcher,
            matcher -> matcher.group("ip"),
            ip -> builder.setVirtualAddress(Collections.singletonList(new IpAddress(new Ipv6Address(ip)))));
    }

    private static List<TrackedObjects> setShutdown(String output, List<TrackedObjects> trackedObjects) {
        List<Integer> objects = ParsingUtils.parseFields(output, 0,
            TRACK_SHUTDOWN_LINE::matcher,
            m -> m.group("object"),
            Integer::parseInt);
        for (Integer object: objects) {
            TrackedObjectsBuilder trackedObject = new TrackedObjectsBuilder()
                    .setTrackedObjectId(object)
                    .setShutdown(true);

            trackedObjects.add(trackedObject.build());
        }
        return trackedObjects;
    }
}