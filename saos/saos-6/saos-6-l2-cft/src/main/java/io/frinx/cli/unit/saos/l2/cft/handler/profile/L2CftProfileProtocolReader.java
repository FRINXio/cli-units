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
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.l2.cft.top.l2.cft.profiles.Profile;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.l2.cft.top.l2.cft.profiles.profile.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.l2.cft.top.l2.cft.profiles.profile.protocols.ProtocolBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.l2.cft.top.l2.cft.profiles.profile.protocols.ProtocolKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2CftProfileProtocolReader implements CliConfigListReader<Protocol, ProtocolKey, ProtocolBuilder> {

    private static final String SHOW_COMMAND =
            "configuration search string \"l2-cft protocol add profile %s\"";

    private Cli cli;

    public L2CftProfileProtocolReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<ProtocolKey> getAllIds(@Nonnull InstanceIdentifier<Protocol> instanceIdentifier,
                                       @Nonnull ReadContext readContext) throws ReadFailedException {
        String profileName = instanceIdentifier.firstKeyOf(Profile.class).getName();
        return getAllIds(blockingRead(f(SHOW_COMMAND, profileName), cli, instanceIdentifier, readContext));
    }

    @VisibleForTesting
    static List<ProtocolKey> getAllIds(String output) {
        Pattern protocol = Pattern.compile(".* ctrl-protocol (?<name>\\S+).*");
        return ParsingUtils.parseFields(output, 0,
            protocol::matcher,
            matcher -> matcher.group("name"),
                ProtocolKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Protocol> instanceIdentifier,
                                      @Nonnull ProtocolBuilder protocolBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        protocolBuilder.setName(instanceIdentifier.firstKeyOf(Protocol.class).getName());
    }
}
