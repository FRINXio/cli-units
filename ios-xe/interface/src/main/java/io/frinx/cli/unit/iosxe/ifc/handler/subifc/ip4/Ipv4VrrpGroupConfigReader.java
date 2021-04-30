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

package io.frinx.cli.unit.iosxe.ifc.handler.subifc.ip4;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.cisco.vrrp.ext.rev210521.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.cisco.vrrp.ext.rev210521.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.cisco.vrrp.ext.rev210521.ipv4.vrrp.group.config.TrackedObjects;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.cisco.vrrp.ext.rev210521.ipv4.vrrp.group.config.TrackedObjectsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ip.vrrp.top.vrrp.VrrpGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ip.vrrp.top.vrrp.VrrpGroupKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ip.vrrp.top.vrrp.vrrp.group.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ip.vrrp.top.vrrp.vrrp.group.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv4VrrpGroupConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_INTERFACE_VRRP = "show running-config interface %s "
            + "| section vrrp %s address-family ipv4";
    private static final Pattern PREEMPT_DELAY_LINE = Pattern.compile("preempt delay minimum (?<delay>.+)");
    private static final Pattern PRIORITY_LINE = Pattern.compile("priority (?<priority>.+)");
    private static final Pattern TRACK_LINE = Pattern.compile("track (?<object>.+) decrement (?<value>.+)");
    private static final Pattern PRIMARY_ADDRESS_LINE = Pattern.compile("address (?<ip>.+) primary");
    private static final Pattern SECONDARY_ADDRESS_LINE = Pattern.compile("address (?<ip>.+) secondary");

    private final Cli cli;

    public Ipv4VrrpGroupConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                      @Nonnull ConfigBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();
        VrrpGroupKey key = id.firstKeyOf(VrrpGroup.class);

        final String output = blockingRead(f(SH_INTERFACE_VRRP, ifcName, key.getVirtualRouterId()), cli, id, ctx);
        parseVrrpGroupConfig(output, builder, key.getVirtualRouterId());
    }

    @VisibleForTesting
    static void parseVrrpGroupConfig(String output, ConfigBuilder builder, Short virtualRouterId) {
        builder.setVirtualRouterId(virtualRouterId);
        setPreemptDelayMinimum(output, builder);
        setPriority(output, builder);
        setPrimaryAddress(output, builder);

        Config1Builder configAug = new Config1Builder();
        setTrackedObjects(output, configAug);
        setSecondaryAddress(output, configAug);
        if (!configAug.equals(new Config1Builder())) {
            builder.addAugmentation(Config1.class, configAug.build());
        }
    }

    private static void setPrimaryAddress(String output, ConfigBuilder builder) {
        ParsingUtils.parseFields(output, 0,
            PRIMARY_ADDRESS_LINE::matcher,
            matcher -> matcher.group("ip"),
            ip -> builder.setVirtualAddress(Collections.singletonList(new IpAddress(new Ipv4Address(ip)))));
    }

    private static void setSecondaryAddress(String output, Config1Builder configAug) {
        configAug.setVirtualSecondaryAddresses(ParsingUtils.parseFields(output, 0,
            SECONDARY_ADDRESS_LINE::matcher,
            m -> m.group("ip"),
            ip -> new IpAddress(new Ipv4Address(ip))));
    }

    private static void setTrackedObjects(String output, Config1Builder configAug) {
        List<Integer> objects = ParsingUtils.parseFields(output, 0,
            TRACK_LINE::matcher,
            m -> m.group("object"),
            Integer::parseInt);
        List<TrackedObjects> trackedObjects = new ArrayList<>();
        for (Integer object: objects) {
            Pattern pattern = Pattern.compile(String.format("track %s decrement (?<value>.+)", object));
            Optional<Boolean> maybeVrrp = ParsingUtils.parseField(output, 0,
                    pattern::matcher, Matcher::matches);
            if (maybeVrrp.isPresent() && maybeVrrp.get()) {
                TrackedObjectsBuilder trackedObject = new TrackedObjectsBuilder().setTrackedObjectId(object);
                ParsingUtils.parseField(output, 0,
                    pattern::matcher,
                    matcher -> matcher.group("value"))
                        .ifPresent(s -> trackedObject.setPriorityDecrement(Short.valueOf(s)));
                trackedObjects.add(trackedObject.build());
            }
        }
        configAug.setTrackedObjects(trackedObjects);
    }

    private static void setPriority(String output, ConfigBuilder builder) {
        ParsingUtils.parseFields(output, 0,
            PRIORITY_LINE::matcher,
            matcher -> Short.parseShort(matcher.group("priority")),
            builder::setPriority);
    }

    private static void setPreemptDelayMinimum(String output, ConfigBuilder builder) {
        ParsingUtils.parseFields(output, 0,
            PREEMPT_DELAY_LINE::matcher,
            matcher -> Integer.parseInt(matcher.group("delay")),
            builder::setPreemptDelay);
    }
}
