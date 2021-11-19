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

package io.frinx.cli.unit.huawei.snmp.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.EncryptedPassword;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.PlainString;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.huawei.snmp.extension.rev211129.huawei.snmp.top.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.huawei.snmp.extension.rev211129.huawei.snmp.top.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CommunityConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    public static final String SHOW_SNMP_COMMUNITY_CONFIG =
            "display current-configuration configuration | include snmp-agent";
    private static final Pattern SNMP_ENGINE_ID = Pattern.compile("snmp-agent local-engineid (?<value>.*)");
    private static final Pattern SNMP_LOCATION = Pattern.compile("snmp-agent sys-info location (?<value>.*)");
    private static final Pattern SNMP_READ_PASSWORD =
            Pattern.compile("snmp-agent community read (?<value>\\S+) acl 2000");
    private static final Pattern SNMP_WRITE_PASSWORD =
            Pattern.compile("snmp-agent community write (?<value>\\S+) acl 2000");

    private final Cli cli;

    public CommunityConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        parseConfigAttributes(blockingRead(SHOW_SNMP_COMMUNITY_CONFIG, cli, id, ctx), configBuilder);
    }

    @VisibleForTesting
    static void parseConfigAttributes(String output, ConfigBuilder configBuilder) {
        ParsingUtils.parseFields(output, 0, SNMP_ENGINE_ID::matcher, m -> m.group("value"),
            configBuilder::setLocalEngineid);
        ParsingUtils.parseFields(output, 0, SNMP_LOCATION::matcher, m -> m.group("value"),
            configBuilder::setCommunityLocation);
        ParsingUtils.parseFields(output, 0, SNMP_READ_PASSWORD::matcher, m -> m.group("value"),
            value -> configBuilder.setReadCommunityPassword(new EncryptedPassword(new PlainString(value))));
        ParsingUtils.parseFields(output, 0, SNMP_WRITE_PASSWORD::matcher, m -> m.group("value"),
            value -> configBuilder.setWriteCommunityPassword(new EncryptedPassword(new PlainString(value))));
    }
}