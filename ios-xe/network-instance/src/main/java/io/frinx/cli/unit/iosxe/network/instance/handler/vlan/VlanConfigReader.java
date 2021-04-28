/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.iosxe.network.instance.handler.vlan;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanConfig.Status;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.Vlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VlanConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_SINGLE_VLAN_CONFIG = "show running-config vlan %s";
    private static final Pattern VLAN_NAME_LINE = Pattern.compile("name\\s*(?<name>.[^\\s]+)");
    private static final Pattern VLAN_STATUS_LINE = Pattern.compile("shutdown");

    private Cli cli;

    public VlanConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        VlanId vlanId = instanceIdentifier.firstKeyOf(Vlan.class).getVlanId();
        parseVlanConfig(blockingRead(f(SH_SINGLE_VLAN_CONFIG, vlanId.getValue()),
                cli, instanceIdentifier, readContext), configBuilder, vlanId);
    }

    @VisibleForTesting
    static void parseVlanConfig(String output, ConfigBuilder configBuilder, VlanId id) {
        configBuilder.setVlanId(id);
        configBuilder.setStatus(parseStatus(output));
        ParsingUtils.parseField(output, VLAN_NAME_LINE::matcher, m -> m.group("name"), configBuilder::setName);
    }

    private static Status parseStatus(String output) {
        return VLAN_STATUS_LINE.matcher(output).find() ? Status.SUSPENDED : Status.ACTIVE;
    }

}
