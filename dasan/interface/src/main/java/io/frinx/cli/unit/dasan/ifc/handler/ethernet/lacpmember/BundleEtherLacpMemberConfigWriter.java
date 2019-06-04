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

package io.frinx.cli.unit.dasan.ifc.handler.ethernet.lacpmember;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BundleEtherLacpMemberConfigWriter implements CliWriter<Config>, CompositeWriter.Child<Config> {

    private final Cli cli;
    private static Pattern AGGREGATE_IFC_NAME = Pattern.compile("Bundle-Ether(?<id>\\d+)");

    public BundleEtherLacpMemberConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(
            @Nonnull InstanceIdentifier<Config> id,
            @Nonnull Config dataAfter,
            @Nonnull WriteContext writeContext) throws WriteFailedException {
        return writeOrUpdateBundleEtherLacpMember(id, null, dataAfter);
    }

    @Override
    public boolean updateCurrentAttributesWResult(
            @Nonnull InstanceIdentifier<Config> id,
            @Nonnull Config dataBefore,
            @Nonnull Config dataAfter,
            @Nonnull WriteContext writeContext) throws WriteFailedException {

        return writeOrUpdateBundleEtherLacpMember(id, dataBefore, dataAfter);
    }

    private boolean writeOrUpdateBundleEtherLacpMember(
            InstanceIdentifier<Config> id,
            Config dataBefore,
            Config dataAfter) throws WriteFailedException {

        String portId = id.firstKeyOf(Interface.class).getName().replace("Ethernet", "");
        String bundleIdBefore = getBundleId(dataBefore);
        String bundleIdAfter = getBundleId(dataAfter);

        // If both of bundleId have the same value, we can skip the update.
        if (StringUtils.equals(bundleIdBefore, bundleIdAfter)) {
            return false;
        }

        // Dasan does not allow to update assignment of lag interface,
        // so we need to delete the existing data and put new data.
        blockingWriteAndRead(cli, id, dataAfter,
                "configure terminal",
                "bridge",
                bundleIdBefore != null
                        ? f("no lacp port %s aggregator %s", portId, bundleIdBefore) : "",
                bundleIdAfter != null
                        ? f("lacp port %s aggregator %s", portId, bundleIdAfter) : "",
                "end");
        return true;
    }

    private static String getBundleId(Config config) {
        if (config == null) {
            return null;
        }
        Config1 aggregationAugAfter = config.getAugmentation(Config1.class);
        if (aggregationAugAfter == null) {
            return null;
        }
        String aggregateIfcName = aggregationAugAfter.getAggregateId();
        if (aggregateIfcName == null) {
            return null;
        }

        Matcher aggregateIfcNameMatcher = AGGREGATE_IFC_NAME.matcher(aggregateIfcName.trim());

        Preconditions.checkArgument(aggregateIfcNameMatcher.matches(), "aggregate-id %s should reference LAG interface",
                aggregateIfcName);
        return aggregateIfcNameMatcher.group("id");
    }

    @Override
    public boolean deleteCurrentAttributesWResult(
            @Nonnull InstanceIdentifier<Config> id,
            @Nonnull Config dataBefore,
            @Nonnull WriteContext writeContext) throws WriteFailedException {

        String portId = id.firstKeyOf(Interface.class).getName().replace("Ethernet", "");
        String bundleIdBefore = getBundleId(dataBefore);

        if (bundleIdBefore == null) {
            return false;
        }

        String bundleId = getBundleId(dataBefore);
        blockingDeleteAndRead(cli, id,
                "configure terminal",
                "bridge",
                f("no lacp port %s aggregator %s", portId, bundleId),
                "end");
        return true;
    }
}
