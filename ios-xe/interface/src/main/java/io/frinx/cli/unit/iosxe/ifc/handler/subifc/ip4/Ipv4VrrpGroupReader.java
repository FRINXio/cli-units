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
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ip.vrrp.top.vrrp.VrrpGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ip.vrrp.top.vrrp.VrrpGroupBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ip.vrrp.top.vrrp.VrrpGroupKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv4VrrpGroupReader implements CliConfigListReader<VrrpGroup, VrrpGroupKey, VrrpGroupBuilder> {

    public static final String SH_INTERFACE_VRRP = "show running-config interface %s | include vrrp";
    private static final Pattern VRRP_GROUP_LINE = Pattern.compile("vrrp (?<group>.+) address-family ipv4.*");
    private static final Pattern VRRP_LINE = Pattern.compile(".*vrrp.*address-family ipv4.*");

    private final Cli cli;

    public Ipv4VrrpGroupReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<VrrpGroupKey> getAllIds(@NotNull InstanceIdentifier<VrrpGroup> instanceIdentifier,
                                        @NotNull ReadContext readContext) throws ReadFailedException {
        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        return getVrrpKeys(blockingRead(f(SH_INTERFACE_VRRP, ifcName), cli, instanceIdentifier, readContext));
    }

    @VisibleForTesting
    static List<VrrpGroupKey> getVrrpKeys(String output) {
        List<VrrpGroupKey> keys = new ArrayList<>();
        List<String> candidates = Pattern.compile("\n").splitAsStream(output).collect(Collectors.toList());
        for (String candidate : candidates) {
            Optional<Boolean> maybeVrrp = ParsingUtils.parseField(candidate, 0,
                VRRP_LINE::matcher, Matcher::matches);
            if (maybeVrrp.isPresent() && maybeVrrp.get()) {
                ParsingUtils.parseField(candidate, 0, VRRP_GROUP_LINE::matcher, matcher -> matcher.group("group"))
                        .ifPresent(s -> keys.add(new VrrpGroupKey(Short.valueOf(s))));
            }
        }
        return keys;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<VrrpGroup> instanceIdentifier,
                                      @NotNull VrrpGroupBuilder vrrpGroupBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        VrrpGroupKey key = instanceIdentifier.firstKeyOf(VrrpGroup.class);
        vrrpGroupBuilder.setKey(key);
    }
}