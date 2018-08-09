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

package io.frinx.cli.unit.dasan.network.instance.handler.vlan;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.common.CompositeListReader;
import io.frinx.cli.unit.dasan.utils.DasanCliUtil;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.VlansBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.Vlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VlanReader implements CliConfigListReader<Vlan, VlanKey, VlanBuilder>,
        CompositeListReader.Child<Vlan, VlanKey, VlanBuilder> {

    private final Cli cli;

    private static final String SHOW_VLAN_CREATE = "show running-config bridge | include ^ vlan create ";
    private static final Pattern VLAN_CREATE_LINE_PATTERN =
            Pattern.compile("vlan create (?<ids>\\S+)(\\s+(?<eline>eline))?");

    public VlanReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<VlanKey> getAllIds(@Nonnull InstanceIdentifier<Vlan> instanceIdentifier,
                                              @Nonnull ReadContext readContext) throws ReadFailedException {

        // If firstKeyOf returns null, should throw a runtime exception.
        if (!instanceIdentifier.firstKeyOf(NetworkInstance.class).equals(NetworInstance.DEFAULT_NETWORK)) {
            return Collections.emptyList();
        }

        return getAllIds(cli, this, instanceIdentifier, readContext);
    }

    static List<VlanKey> getAllIds(Cli cli, CliReader cliReader,
                                              @Nonnull InstanceIdentifier<?> instanceIdentifier,
                                              @Nonnull ReadContext readContext) throws ReadFailedException {

        return parseVlanIds(cliReader.blockingRead(SHOW_VLAN_CREATE, cli, instanceIdentifier, readContext));
    }

    @VisibleForTesting
    static List<VlanKey> parseVlanIds(String output) {

        return ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .map(VLAN_CREATE_LINE_PATTERN::matcher)
                .filter(Matcher::matches)
                .flatMap(m -> DasanCliUtil.parseIdRanges(m.group("ids")).stream())
                .map(Integer::valueOf)
                .map(VlanId::new)
                .map(VlanKey::new)
                .collect(Collectors.toList());
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Vlan> instanceIdentifier,
                                      @Nonnull VlanBuilder vlanBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {

        // If firstKeyOf returns null, should throw a runtime exception.
        VlanId vlanId = instanceIdentifier.firstKeyOf(Vlan.class).getVlanId();
        vlanBuilder.setVlanId(vlanId);
    }

    @Override
    public void merge(Builder<? extends DataObject> builder, List<Vlan> list) {
        ((VlansBuilder) builder).setVlan(list);
    }
}
