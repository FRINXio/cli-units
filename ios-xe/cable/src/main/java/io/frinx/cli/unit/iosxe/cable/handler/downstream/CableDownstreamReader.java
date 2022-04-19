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
package io.frinx.cli.unit.iosxe.cable.handler.downstream;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.downstream.top.downstreams.DownstreamCableProfile;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.downstream.top.downstreams.DownstreamCableProfileBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.downstream.top.downstreams.DownstreamCableProfileKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.downstream.top.downstreams.downstream.cable.profile.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CableDownstreamReader implements CliConfigListReader<DownstreamCableProfile, DownstreamCableProfileKey,
        DownstreamCableProfileBuilder> {
    static final String SH_CABLE_DOWN = "show running-config | include cable downstream controller-profile";
    private static final Pattern CABLE_DOWN_LINE = Pattern.compile("cable downstream controller-profile (?<id>\\d+)");

    private final Cli cli;

    public CableDownstreamReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<DownstreamCableProfileKey> getAllIds(@Nonnull InstanceIdentifier<DownstreamCableProfile> id,
                                         @Nonnull ReadContext readContext) throws ReadFailedException {
        final String output = blockingRead(SH_CABLE_DOWN, cli, id, readContext);
        return getCableDownstreams(output);
    }

    @VisibleForTesting
    public static List<DownstreamCableProfileKey> getCableDownstreams(String output) {
        return ParsingUtils.parseFields(output, 0, CABLE_DOWN_LINE::matcher,
            matcher -> matcher.group("id"), DownstreamCableProfileKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<DownstreamCableProfile> instanceIdentifier,
                                      @Nonnull DownstreamCableProfileBuilder downstreamBuilder,
                                      @Nonnull ReadContext readContext) {
        final String downName = instanceIdentifier.firstKeyOf(DownstreamCableProfile.class).getId();
        downstreamBuilder.setId(downName);
        downstreamBuilder.setConfig(new ConfigBuilder().setId(downName).build());
    }
}