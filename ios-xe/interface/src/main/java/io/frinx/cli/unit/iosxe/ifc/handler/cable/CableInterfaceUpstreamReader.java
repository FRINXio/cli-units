/*
 * Copyright © 2022 Frinx and others.
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

package io.frinx.cli.unit.iosxe.ifc.handler.cable;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.upstream.top.upstream.UpstreamCables;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.upstream.top.upstream.UpstreamCablesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.upstream.top.upstream.UpstreamCablesKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CableInterfaceUpstreamReader implements
        CliConfigListReader<UpstreamCables, UpstreamCablesKey, UpstreamCablesBuilder> {

    static final String SH_UPSTREAM_CABLE =
            "show running-config interface %s | include ^ upstream";

    static final Pattern UPSTREAM_CABLE_ID = Pattern.compile(" *upstream (?<id>.+) Upstream-Cable .*");

    private final Cli cli;

    public CableInterfaceUpstreamReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<UpstreamCablesKey> getAllIds(@Nonnull InstanceIdentifier<UpstreamCables> instanceIdentifier,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        final String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        if (ifcName.startsWith("Cable")) {
            final String ifcOutput = blockingRead(f(SH_UPSTREAM_CABLE, ifcName), cli, instanceIdentifier, readContext);
            return parseIds(ifcOutput);
        }
        else {
            return Collections.emptyList();
        }
    }

    static List<UpstreamCablesKey> parseIds(final String ifcOutput) {
        return ParsingUtils.parseFields(ifcOutput, 0,
            UPSTREAM_CABLE_ID::matcher,
            matcher -> matcher.group("id"),
            UpstreamCablesKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<UpstreamCables> instanceIdentifier,
                                      @Nonnull UpstreamCablesBuilder upstreamCablesBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        final String upstreamIds = instanceIdentifier.firstKeyOf(UpstreamCables.class).getId();
        upstreamCablesBuilder.setId(upstreamIds);
        upstreamCablesBuilder.setKey(new UpstreamCablesKey(upstreamIds));
    }
}
