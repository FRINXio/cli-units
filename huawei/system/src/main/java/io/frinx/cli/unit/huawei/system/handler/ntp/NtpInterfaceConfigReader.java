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

package io.frinx.cli.unit.huawei.system.handler.ntp;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.connection.extension.rev210930.huawei.ntp.service._interface.ntp._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.connection.extension.rev210930.huawei.ntp.service._interface.ntp._interface.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NtpInterfaceConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final Pattern INTERFACE_LINE = Pattern.compile("ntp-service source-interface (?<interf>\\S+)");

    private Cli cli;

    public NtpInterfaceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        parseConfig(blockingRead(NtpConfigReader.DISPLAY_NTP, cli, instanceIdentifier, readContext), configBuilder);
    }

    @VisibleForTesting
    static void parseConfig(String output, ConfigBuilder configBuilder) {
        ParsingUtils.parseField(output, 0,
            INTERFACE_LINE::matcher,
            matcher -> matcher.group("interf"),
            configBuilder::setNtpServiceSourceInterface);
    }
}