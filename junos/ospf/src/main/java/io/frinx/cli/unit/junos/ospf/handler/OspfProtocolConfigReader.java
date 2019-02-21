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

package io.frinx.cli.unit.junos.ospf.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.ospf.OspfReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.protocol.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.protocol.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.extension.rev190117.ProtocolConfAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.extension.rev190117.ProtocolConfAugBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OspfProtocolConfigReader implements CliReader<Config, ConfigBuilder>,
        OspfReader.OspfConfigReader<Config, ConfigBuilder>,
        CompositeReader.Child<Config, ConfigBuilder> {

    private Cli cli;

    public OspfProtocolConfigReader(Cli cli) {
        this.cli = cli;
    }

    @VisibleForTesting
    static final String SHOW_EXPORT_POLICY =
            "show configuration%s protocols ospf export | display set";
    private static final Pattern POLICY_LINE = Pattern.compile(
            "set.* protocols ospf export (?<export>.+)");

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull
            ConfigBuilder configBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        ProtocolKey protocolKey = instanceIdentifier.firstKeyOf(Protocol.class);
        configBuilder.setIdentifier(protocolKey.getIdentifier());
        configBuilder.setName(protocolKey.getName());

        final String nwInsName = OspfProtocolReader.resolveVrfWithName(instanceIdentifier);
        ProtocolConfAugBuilder builder = new ProtocolConfAugBuilder();
        String output = blockingRead(String.format(SHOW_EXPORT_POLICY, nwInsName), cli,
                instanceIdentifier, readContext);
        parseExportPolicy(output, builder);
        configBuilder.addAugmentation(ProtocolConfAug.class, builder.build());
    }

    @VisibleForTesting
    private void parseExportPolicy(String output, ProtocolConfAugBuilder builder) {

        List<String> policyList = ParsingUtils.parseFields(output, 0,
            POLICY_LINE::matcher,
            m -> m.group("export"),
            v -> v);

        if (policyList != null && !policyList.isEmpty()) {
            builder.setExportPolicy(policyList);
        }
    }
}
