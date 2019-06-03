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
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.dasan.utils.DasanCliUtil;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.Vlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VlanConfigReader implements CliConfigReader<Config, ConfigBuilder>,
        CompositeReader.Child<Config, ConfigBuilder> {

    private static final Logger LOG = LoggerFactory.getLogger(VlanConfigReader.class);

    @VisibleForTesting
    static final String SHOW_VLAN_CREATE = "show running-config bridge | include ^ vlan create ";
    private static final Pattern VLAN_CREATE_LINE_PATTERN =
            Pattern.compile("vlan create (?<ids>\\S+)(\\s+(?<eline>eline))?");

    private Cli cli;

    public VlanConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {

        // If firstKeyOf returns null, should throw a runtime exception.
        if (!instanceIdentifier.firstKeyOf(NetworkInstance.class).equals(NetworInstance.DEFAULT_NETWORK)) {
            return;
        }
        VlanId vlanId = instanceIdentifier.firstKeyOf(Vlan.class).getVlanId();

        if (!isVlan(instanceIdentifier, readContext, vlanId)) {
            return;
        }

        parseVlanConfig(blockingRead(SHOW_VLAN_CREATE,
                cli, instanceIdentifier, readContext), configBuilder, vlanId);
    }

    @VisibleForTesting
    boolean isVlan(InstanceIdentifier<Config> id, ReadContext readContext, VlanId vlanId) throws ReadFailedException {
        List<VlanKey> vlanIds = VlanReader.getAllIds(cli, this, id, readContext);

        return vlanIds.stream()
                .map(VlanKey::getVlanId)
                .filter(vlanId::equals)
                .findFirst()
                .isPresent();
    }

    @Override
    public void merge(Builder<? extends DataObject> builder, Config value) {
        ((VlanBuilder) builder).setConfig(value);
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }

    @VisibleForTesting
    static void parseVlanConfig(String output, ConfigBuilder builder, VlanId vlanId) {
        Integer vlanIdValue = vlanId.getValue();

        ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .map(VLAN_CREATE_LINE_PATTERN::matcher)
                .filter(Matcher::matches)
                .filter(m -> DasanCliUtil.parseIdRanges(m.group("ids")).stream()
                .map(Integer::valueOf)
                .filter(vlanIdValue::equals)
                .findFirst().isPresent())
                .findFirst()
                .ifPresent(m -> {
                    builder.setVlanId(vlanId);

                    boolean eline = "eline".equals(m.group("eline"));
                    LOG.debug("VlanId:{} -> eline = {}", vlanIdValue, eline);

                    if (eline) {
                        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.dasan.rev180801.Config1Builder
                                augmentConfigBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                                .vlan.dasan.rev180801.Config1Builder();
                        augmentConfigBuilder.setEline(Boolean.TRUE);
                        builder.addAugmentation(
                                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.dasan.rev180801
                                .Config1.class,
                                augmentConfigBuilder.build());
                    }
                });
    }
}
