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

package io.frinx.cli.unit.saos.ifc.handler.aggregate;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos.ifc.handler.InterfaceReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AggregateConfigReader  implements CliConfigReader<Config1, Config1Builder> {

    private final Cli cli;

    public AggregateConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config1> instanceIdentifier,
                                      @Nonnull Config1Builder config1Builder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        String output = blockingRead(InterfaceReader.SH_PORTS, cli, instanceIdentifier, readContext);
        parseConfig(output, config1Builder, ifcName);
    }

    @VisibleForTesting
    void parseConfig(String output, Config1Builder builder, String ifcName)  {
        Pattern pattern = Pattern.compile("aggregation add agg (?<name>\\S+) port " + ifcName);

        ParsingUtils.parseFields(output, 0,
            pattern::matcher,
            matcher -> matcher.group("name"),
            builder::setAggregateId);
    }
}
