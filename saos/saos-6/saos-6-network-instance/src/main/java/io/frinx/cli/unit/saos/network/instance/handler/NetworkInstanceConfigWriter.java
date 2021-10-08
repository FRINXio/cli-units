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

package io.frinx.cli.unit.saos.network.instance.handler;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.handlers.def.DefaultConfigWriter;
import io.frinx.cli.unit.saos.network.instance.handler.l2vsi.L2VSIConfigWriter;
import io.frinx.cli.unit.saos.network.instance.handler.l2vsicp.L2vsicpConfigWriter;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NetworkInstanceConfigWriter extends CompositeWriter<Config> {

    public NetworkInstanceConfigWriter(Cli cli) {
        super(Lists.newArrayList(
                new L2vsicpConfigWriter(cli),
                new L2VSIConfigWriter(cli),
                new DefaultConfigWriter()
        ));
    }

    // Below code does following:
    // Before performing an update or write, check the expected number of updates/writes to be executed for this writer
    // If we are not at the end yet (last update/write), put the update/write into a temporaray cache
    // If we are at the end, write/update all of the objects at once in proper order

    @Override
    public void writeCurrentAttributes(InstanceIdentifier<Config> instanceIdentifier,
                                       Config config,
                                       WriteContext writeContext) throws WriteFailedException {
        final int allUpdatesToConfigSize = getCurrentSizeOfUpdates(writeContext);
        Map<InstanceIdentifier<Config>, Object> collectorMap = getUpdatesCache(writeContext, "updateOrCreate");
        collectorMap.put(instanceIdentifier, config);

        if (collectorMap.size() == allUpdatesToConfigSize) {
            handleAllUpdatesOrWritesAtOnce(writeContext, collectorMap);
        }
    }

    @Override
    public void updateCurrentAttributes(InstanceIdentifier<Config> id,
                                        Config dataBefore,
                                        Config dataAfter,
                                        WriteContext writeContext) throws WriteFailedException {
        final int allUpdatesToConfigSize = getCurrentSizeOfUpdates(writeContext);
        Map<InstanceIdentifier<Config>, Object> collectorMap = getUpdatesCache(writeContext, "updateOrCreate");
        collectorMap.put(id, dataAfter);

        if (collectorMap.size() == allUpdatesToConfigSize) {
            handleAllUpdatesOrWritesAtOnce(writeContext, collectorMap);
        }
    }

    @Override
    public void deleteCurrentAttributes(InstanceIdentifier<Config> instanceIdentifier,
                                        Config object,
                                        WriteContext writeContext) throws WriteFailedException {
        final int allUpdatesToConfigSize = getCurrentSizeOfUpdates(writeContext);
        Map<InstanceIdentifier<Config>, Object> collectorMap = getUpdatesCache(writeContext, "delete");
        collectorMap.put(instanceIdentifier, object);

        if (collectorMap.size() == allUpdatesToConfigSize) {
            List<Entry<InstanceIdentifier<Config>, Object>> sortedEntries =
                    sortEntries(collectorMap, Comparator.naturalOrder());

            for (Map.Entry<InstanceIdentifier<Config>, Object> entry : sortedEntries) {
                super.deleteCurrentAttributes(entry.getKey(), (Config) entry.getValue(), writeContext);
            }
        }
    }

    // We need to sort the order in which all types of commands
    // are executed to ensure that they run correctly on the device.
    // To delete, using the reverse order.

    @VisibleForTesting
    protected static List<Entry<InstanceIdentifier<Config>, Object>> sortEntries(Map<InstanceIdentifier<Config>,
                                                                        Object> collectorMap,
                                                                       Comparator<Boolean> order) {
        return collectorMap.entrySet()
                .stream()
                .sorted(Comparator.comparing(value -> {
                    Config config = (Config) value.getValue();
                    return config.getType().getSimpleName().equals("L2VSICP");
                }, order))
                .collect(Collectors.toList());
    }

    private int getCurrentSizeOfUpdates(WriteContext writeContext) {
        return (int) writeContext.getModificationCache().get(IIDs.NE_NE_CONFIG + "_updatesCount");
    }

    static Map<InstanceIdentifier<Config>, Object> getUpdatesCache(WriteContext writeContext, String classifier) {
        final String keyForTheCache = IIDs.NE_NE_CONFIG + "_collector_" + classifier;
        if (!writeContext.getModificationCache().containsKey(keyForTheCache)) {
            writeContext.getModificationCache().put(keyForTheCache,
                    new HashMap<InstanceIdentifier<Config>, Config>());
        }
        return (Map<InstanceIdentifier<Config>, Object>)
                writeContext.getModificationCache().get(keyForTheCache);
    }

    private void handleAllUpdatesOrWritesAtOnce(WriteContext writeContext,
                                                Map<InstanceIdentifier<Config>, Object> collectorMap)
            throws WriteFailedException {
        List<Entry<InstanceIdentifier<Config>, Object>> sortedEntries =
                sortEntries(collectorMap, Comparator.reverseOrder());

        for (Entry<InstanceIdentifier<Config>, Object> sortedEntry : sortedEntries) {
            if (sortedEntry.getValue() instanceof Map.Entry) {
                final Map.Entry<Config, Config> beforeAndAfter = (Map.Entry<Config, Config>) sortedEntry.getValue();
                updateCurrentAttributes(sortedEntry.getKey(), beforeAndAfter.getKey(), beforeAndAfter.getValue(),
                        writeContext);
            } else {
                super.writeCurrentAttributes(sortedEntry.getKey(), (Config) sortedEntry.getValue(), writeContext);
            }
        }
    }
}