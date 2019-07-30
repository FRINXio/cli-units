/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.brocade.network.instance.vrf.vlan;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.Vlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

//public class VlanReader implements CliConfigListReader<Vlan, VlanKey, VlanBuilder> {
public class DefaultVlanReader implements CompositeListReader.Child<Vlan, VlanKey, VlanBuilder>,
        CliConfigListReader<Vlan, VlanKey, VlanBuilder> {

    private static final String SH_VLAN = "show running-config vlan | include ^vlan ";
    public static final Pattern VLAN_ID_LINE = Pattern.compile("vlan (?<id>\\d+)\\s*(name)?\\s*(?<name>.+)?");
    public static final Check CHECK = BasicCheck.checkData(
            ChecksMap.DataCheck.NetworkInstanceConfig.IID_TRANSFORMATION,
            ChecksMap.DataCheck.NetworkInstanceConfig.TYPE_DEFAULTINSTANCE);

    private final Cli cli;

    public DefaultVlanReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<VlanKey> getAllIds(@Nonnull InstanceIdentifier<Vlan> instanceIdentifier,
                                   @Nonnull ReadContext readContext) throws ReadFailedException {
        return getAllIds(instanceIdentifier, readContext, this.cli, this);
    }

    static List<VlanKey> getAllIds(@Nonnull InstanceIdentifier<?> instanceIdentifier,
                                   @Nonnull ReadContext readContext,
                                   @Nonnull Cli cli,
                                   @Nonnull CliReader reader) throws ReadFailedException {
        if (!instanceIdentifier.getTargetType().equals(Vlan.class)) {
            instanceIdentifier = RWUtils.cutId(instanceIdentifier, Vlan.class);
        }

        // The getCheck() is not used for getAllIds by the CompositeListReader, so it needs to be manually invoked
        return CHECK.canProcess(instanceIdentifier, readContext)
                ? parseVlans(reader.blockingRead(SH_VLAN, cli, instanceIdentifier, readContext))
                : Collections.emptyList();
    }

    @VisibleForTesting
    static List<VlanKey> parseVlans(String output) {
        return ParsingUtils.parseFields(output, 0,
                VLAN_ID_LINE::matcher, m -> Integer.valueOf(m.group("id")), id -> new VlanKey(new VlanId(id)));
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Vlan> instanceIdentifier,
                                      @Nonnull VlanBuilder vlanBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        vlanBuilder.setVlanId(instanceIdentifier.firstKeyOf(Vlan.class).getVlanId());
    }

    @Override
    public Check getCheck() {
        return CHECK;
    }
}