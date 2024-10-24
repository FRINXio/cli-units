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
package io.frinx.cli.unit.iosxe.evc.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.evc.rev200416.evc.top.evcs.Evc;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.evc.rev200416.evc.top.evcs.EvcBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.evc.rev200416.evc.top.evcs.EvcKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class EvcReader implements CliConfigListReader<Evc, EvcKey, EvcBuilder> {

    public static final String SH_EVCS = "show running-config | include ^ethernet evc";
    private static final Pattern EVC_LINE = Pattern.compile("ethernet evc (?<name>\\S+).*");
    private final Cli cli;

    public EvcReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<EvcKey> getAllIds(@NotNull InstanceIdentifier<Evc> instanceIdentifier,
                                  @NotNull ReadContext readContext) throws ReadFailedException {
        String output = blockingRead(SH_EVCS, cli, instanceIdentifier, readContext);
        return getAllIds(output);
    }

    @VisibleForTesting
    static List<EvcKey> getAllIds(String output) {
        return ParsingUtils.parseFields(output, 0,
            EVC_LINE::matcher,
            matcher -> matcher.group("name"),
            EvcKey::new);
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Evc> instanceIdentifier,
                                      @NotNull EvcBuilder evcBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        evcBuilder.setKey(instanceIdentifier.firstKeyOf(Evc.class));
    }
}