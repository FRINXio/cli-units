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

package io.frinx.cli.unit.sros.ipsec.handler.clientgroup;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.sros.utils.InfoCmdUtil;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.ClientGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.ClientGroupBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.ClientGroupKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ClientGroupReader implements CliConfigListReader<ClientGroup, ClientGroupKey, ClientGroupBuilder> {
    private static final String CLIENT_DB_INFO_COMMAND =
        InfoCmdUtil.genInfoCommand("ipsec", "match ^[[:space:]]+client-db expression");
    private static final Pattern CLIENT_DB_LINE = Pattern.compile("^client-db \"(?<name>\\S+)\" create$");

    private Cli cli;

    public ClientGroupReader(final Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<ClientGroupKey> getAllIds(
        @Nonnull InstanceIdentifier<ClientGroup> id,
        @Nonnull ReadContext readContext) throws ReadFailedException {

        String output = blockingRead(CLIENT_DB_INFO_COMMAND, cli, id, readContext);
        return getClientGroupKeys(output);
    }

    @Override
    public void readCurrentAttributes(
        @Nonnull InstanceIdentifier<ClientGroup> id,
        @Nonnull ClientGroupBuilder builder,
        @Nonnull ReadContext readContext) throws ReadFailedException {

        ClientGroupKey key = id.firstKeyOf(ClientGroup.class);
        builder.setGroupName(key.getGroupName());
    }

    private static List<ClientGroupKey> getClientGroupKeys(String output) {
        return ParsingUtils.parseFields(output, 0,
            CLIENT_DB_LINE::matcher,
            m -> m.group("name"),
            s -> new ClientGroupKey(s));
    }
}
