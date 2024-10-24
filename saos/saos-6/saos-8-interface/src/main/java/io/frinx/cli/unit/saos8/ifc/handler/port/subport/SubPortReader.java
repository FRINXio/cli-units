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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosSubIfConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosSubIfConfigAugBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubPortReader implements CliConfigListReader<Subinterface, SubinterfaceKey, SubinterfaceBuilder> {

    private static final String SHOW_COMMAND = "configuration search string \"parent-port %s classifier-precedence \"";
    private static final Pattern SUB_IFC_NAME_PAT = Pattern.compile("sub-port create sub-port (?<name>\\S+) "
            + "parent-port .*");
    private static final Pattern INGRESS_PAT = Pattern.compile(".* ingress-l2-transform (?<ingress>\\S+).*");
    private static final Pattern EGRESS_PAT = Pattern.compile(".* egress-l2-transform (?<egress>\\S+).*");

    private Cli cli;

    public SubPortReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<SubinterfaceKey> getAllIds(@NotNull InstanceIdentifier<Subinterface> instanceIdentifier,
                                           @NotNull ReadContext readContext) throws ReadFailedException {
        if (isPort(instanceIdentifier, readContext)) {
            String parentPort = instanceIdentifier.firstKeyOf(Interface.class).getName();
            String output = blockingRead(f(SHOW_COMMAND, parentPort), cli, instanceIdentifier, readContext);
            return getAllIds(output, parentPort, readContext);
        }
        return Collections.emptyList();
    }

    @VisibleForTesting
    static List<SubinterfaceKey> getAllIds(String output, String parentPort, @NotNull ReadContext readContext) {
        var pattern = Pattern.compile("sub-port create sub-port .* parent-port " + parentPort
                + " classifier-precedence (?<id>\\d+)( .+|$)");
        var ids = ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .map(pattern::matcher)
                .filter(Matcher::matches)
                .map(match -> getIdAndParseConfig(Long.valueOf(match.group("id")), match.group()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        readContext.getModificationCache().put(SubPortReader.class.getName(), ids);
        return new ArrayList<>(ids.keySet());
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Subinterface> instanceIdentifier,
                                      @NotNull SubinterfaceBuilder subinterfaceBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        var subInterfaceKey = instanceIdentifier.firstKeyOf(Subinterface.class);
        subinterfaceBuilder
                .setIndex(subInterfaceKey.getIndex())
                .setConfig(findConfigInCache(subInterfaceKey, readContext));
    }

    private static SimpleEntry<SubinterfaceKey, Config> getIdAndParseConfig(final Long index,
                                                                            final String command) {
        final var subInterfaceKey = new SubinterfaceKey(index);
        final var configBuilder = new ConfigBuilder().setIndex(index);
        final var configAug = new SaosSubIfConfigAugBuilder();
        ParsingUtils.parseField(command, 0,
            SUB_IFC_NAME_PAT::matcher,
            m -> m.group("name"),
            configBuilder::setName);
        ParsingUtils.parseField(command, 0,
            INGRESS_PAT::matcher,
            m -> m.group("ingress"),
            configAug::setIngressL2Transform);
        ParsingUtils.parseField(command, 0,
            EGRESS_PAT::matcher,
            m -> m.group("egress"),
            configAug::setEgressL2Transform);
        return new SimpleEntry<>(subInterfaceKey, configBuilder.addAugmentation(SaosSubIfConfigAug.class,
                configAug.build()).build());
    }

    static Config findConfigInCache(final SubinterfaceKey subinterfaceKey, @NotNull ReadContext readContext) {
        return ((Map<SubinterfaceKey, Config>) readContext.getModificationCache().get(SubPortReader.class.getName()))
                .get(subinterfaceKey);
    }

    private boolean isPort(InstanceIdentifier<Subinterface> id, ReadContext readContext) throws ReadFailedException {
        return PortReader.checkCachedIds(cli, this, id, readContext)
                .contains(id.firstKeyOf(Interface.class));
    }
}