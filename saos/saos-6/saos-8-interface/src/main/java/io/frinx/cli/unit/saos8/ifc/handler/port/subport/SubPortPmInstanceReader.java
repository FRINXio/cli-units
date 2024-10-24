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

package io.frinx.cli.unit.saos8.ifc.handler.port.subport;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos8.ifc.handler.port.PortReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.pm.instances.pm.instances.PmInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.pm.instances.pm.instances.PmInstanceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.pm.instances.pm.instances.PmInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.pm.instances.pm.instances.pm.instance.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubPortPmInstanceReader implements CliConfigListReader<PmInstance, PmInstanceKey, PmInstanceBuilder> {

    private static final String SHOW_COMMAND = "configuration search string \"pm create sub-port %s \"";

    private final Cli cli;

    public SubPortPmInstanceReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<PmInstanceKey> getAllIds(@NotNull InstanceIdentifier<PmInstance> instanceIdentifier,
                                         @NotNull ReadContext readContext) throws ReadFailedException {
        if (isPort(instanceIdentifier, readContext)) {
            var subPortName = findSubInterfaceNameInCache(instanceIdentifier.firstKeyOf(Subinterface.class),
                    readContext);
            var output = blockingRead(f(SHOW_COMMAND, subPortName), cli, instanceIdentifier, readContext);
            return getAllIds(output, subPortName, readContext);
        }
        return Collections.emptyList();
    }

    @VisibleForTesting
    static List<PmInstanceKey> getAllIds(String output, String subPort, ReadContext readContext) {
        var pattern = Pattern.compile("pm create sub-port " + subPort + " pm-instance (?<name>\\S+) "
                + "(.* bin-count (?<bin>\\S+).*|.*)");
        var ids = ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .map(pattern::matcher)
                .filter(Matcher::matches)
                .map(items -> getIdAndParseConfig(items.group("name"), items.group("bin")))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        readContext.getModificationCache().put(SubPortPmInstanceReader.class.getName(), ids);
        return new ArrayList<>(ids.keySet());
    }

    private static SimpleEntry<PmInstanceKey,
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if
            .extension.pm.instances.pm.instances.pm.instance.Config> getIdAndParseConfig(final String name,
                                                                                         final String bin) {
        var pmInstanceKey = new PmInstanceKey(name);
        var config = new ConfigBuilder()
                .setName(name)
                .setBinCount(bin)
                .build();
        return new SimpleEntry<>(pmInstanceKey, config);
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<PmInstance> instanceIdentifier,
                                      @NotNull PmInstanceBuilder pmInstanceBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        pmInstanceBuilder.setName(instanceIdentifier.firstKeyOf(PmInstance.class).getName())
                .setConfig(findConfigInCache(instanceIdentifier.firstKeyOf(PmInstance.class), readContext));
    }

    static org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if
            .extension.pm.instances.pm.instances.pm.instance.Config findConfigInCache(final PmInstanceKey pmInstanceKey,
                                                                                      final ReadContext readContext) {
        return ((Map<PmInstanceKey, org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos
                .extension.rev200205.saos._if.extension.pm.instances.pm.instances.pm.instance.Config>) readContext
                .getModificationCache().get(SubPortPmInstanceReader.class.getName())).get(pmInstanceKey);
    }

    private static String findSubInterfaceNameInCache(SubinterfaceKey subinterfaceKey,
                                                      @NotNull ReadContext readContext) {
        return SubPortReader.findConfigInCache(subinterfaceKey, readContext).getName();
    }

    private boolean isPort(InstanceIdentifier<PmInstance> id, ReadContext readContext) throws ReadFailedException {
        return PortReader.checkCachedIds(cli, this, id, readContext)
                .contains(id.firstKeyOf(Interface.class));
    }
}