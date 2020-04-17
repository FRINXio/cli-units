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

package io.frinx.cli.unit.saos.l2.cft.handler.profile;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos.l2.cft.Util;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.ProtocolConfig.Disposition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.ProtocolConfig.Name;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.l2.cft.top.l2.cft.profiles.Profile;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.l2.cft.top.l2.cft.profiles.profile.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.l2.cft.top.l2.cft.profiles.profile.protocols.protocol.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.l2.cft.top.l2.cft.profiles.profile.protocols.protocol.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2CftProfileProtocolConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SHOW_COMMAND =
            "configuration search string \"l2-cft protocol add profile %s ctrl-protocol %s\"";

    private Cli cli;

    public L2CftProfileProtocolConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String profileName = instanceIdentifier.firstKeyOf(Profile.class).getName();
        String protocolName = instanceIdentifier.firstKeyOf(Protocol.class).getName();
        String output = blockingRead(f(SHOW_COMMAND, profileName, protocolName), cli, instanceIdentifier, readContext);
        parseConfig(output, protocolName, configBuilder);
    }

    @VisibleForTesting
    void parseConfig(String output, String protocolName, ConfigBuilder builder) {
        builder.setName(Name.forValue(Util.getProtocolValue(protocolName)));
        setDisposition(output, builder);
    }

    private void setDisposition(String output, ConfigBuilder builder) {
        Pattern disposition = Pattern.compile(".* untagged-disposition (?<disposition>\\S+).*");

        ParsingUtils.parseField(output, 0,
            disposition::matcher,
            matcher -> matcher.group("disposition"),
            disp -> builder.setDisposition(Disposition.forValue(Util.getDispositionValue(disp))));
    }
}
