/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.dasan.ifc.handler.lacp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.dasan.ifc.handler.BundleEtherInterfaceReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import java.util.regex.Matcher;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.AggregationType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.aggregation.logical.top.AggregationBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.aggregation.logical.top.aggregation.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.aggregation.logical.top.aggregation.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class BundleEtherLacpConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private Cli cli;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    public BundleEtherLacpConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void merge(@NotNull final Builder<? extends DataObject> builder, @NotNull final Config value) {
        ((AggregationBuilder) builder).setConfig(value);
    }

    @Override
    public void readCurrentAttributes(@NotNull final InstanceIdentifier<Config> id,
                                      @NotNull final ConfigBuilder builder,
            @NotNull final ReadContext ctx) throws ReadFailedException {

        String ifName = id.firstKeyOf(Interface.class).getName();
        Matcher matcher = BundleEtherInterfaceReader.BUNDLE_ETHER_IF_NAME_PATTERN.matcher(ifName);
        if (!matcher.matches()) {
            return;
        }

        builder.setLagType(AggregationType.LACP);
    }
}