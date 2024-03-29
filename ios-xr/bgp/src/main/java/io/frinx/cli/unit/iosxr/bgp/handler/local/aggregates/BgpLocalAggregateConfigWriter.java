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

package io.frinx.cli.unit.iosxr.bgp.handler.local.aggregates;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.bgp.handler.GlobalAfiSafiReader;
import io.frinx.cli.unit.iosxr.bgp.handler.GlobalConfigWriter;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.NiProtAggAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local.aggregate.top.local.aggregates.aggregate.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BgpLocalAggregateConfigWriter implements CliWriter<Config> {

    private Cli cli;

    @SuppressWarnings("checkstyle:linelength")
    static final String LOCAL_AGGREGATE_CONFIG = """
            router bgp {$as} {$instance} {$vrf}
            {% loop in $oldAfiSafis as $afiSafi}
            {% if ($afiSafi == ipv4unicast) && ($configOld.prefix.ipv4_prefix|default(nonIpv4) != nonIpv4) %}address-family ipv4 unicast
            no network {$configOld.prefix.ipv4_prefix.value}{.if ($oldPolicy)} route-policy {$oldPolicy}{/if}
            exit
            {% elseIf ($afiSafi == ipv6unicast) && ($configOld.prefix.ipv6_prefix|default(nonIpv6) != nonIpv6) %}address-family ipv6 unicast
            no network {$configOld.prefix.ipv6_prefix.value}{.if ($oldPolicy)} route-policy {$oldPolicy}{/if}
            exit
            {% endif %}{% onEmpty %}{% endloop %}{% loop in $newAfiSafis as $afiSafi}
            {% if ($afiSafi == ipv4unicast) && ($configNew.prefix.ipv4_prefix|default(nonIpv4) != nonIpv4) %}address-family ipv4 unicast
            network {$configNew.prefix.ipv4_prefix.value}{.if ($newPolicy)} route-policy {$newPolicy}{/if}
            exit
            {% elseIf ($afiSafi == ipv6unicast) && ($configNew.prefix.ipv6_prefix|default(nonIpv6) != nonIpv6) %}address-family ipv6 unicast
            network {$configNew.prefix.ipv6_prefix.value}{.if ($newPolicy)} route-policy {$newPolicy}{/if}
            exit
            {% endif %}{% onEmpty %}{% endloop %}root
            """;

    public BgpLocalAggregateConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        Optional<Bgp> bgpOptional = writeContext.readAfter(id.firstIdentifierOf(Protocol.class)
                .child(Bgp.class));
        Preconditions.checkArgument(bgpOptional.isPresent());
        final Global g = Preconditions.checkNotNull(bgpOptional.get()
                .getGlobal());
        Preconditions.checkArgument(g.getAfiSafis() != null && g.getAfiSafis()
                        .getAfiSafi() != null,
                "Address-family for network doesn't exist");
        final String instName = GlobalConfigWriter.getProtoInstanceName(id);
        final String nwInsName = GlobalConfigWriter.resolveVrfWithName(id);
        final String policy = getApplyPolicy(config);
        blockingWriteAndRead(fT(LOCAL_AGGREGATE_CONFIG,
                "as", g.getConfig()
                        .getAs()
                        .getValue(),
                "instance", instName,
                "vrf", nwInsName,
                "newAfiSafis", g.getAfiSafis()
                        .getAfiSafi()
                        .stream()
                        .map(afi -> GlobalAfiSafiReader.transformAfiToString(afi.getAfiSafiName()))
                        .map(afi -> afi.replaceAll(" ", ""))
                        .collect(Collectors.toList()),
                "oldAfiSafis", Collections.EMPTY_LIST,
                "oldPolicy", null,
                "newPolicy", policy,
                "configNew", config,
                "configOld", null),
                cli, id, config);
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        Optional<Bgp> bgpOptional = writeContext.readAfter(id.firstIdentifierOf(Protocol.class)
                .child(Bgp.class));
        Preconditions.checkArgument(bgpOptional.isPresent());
        final Global g = Preconditions.checkNotNull(bgpOptional.get()
                .getGlobal());
        Preconditions.checkArgument(g.getAfiSafis() != null && g.getAfiSafis()
                        .getAfiSafi() != null,
                "Address-family for network doesn't exist");
        final String instName = GlobalConfigWriter.getProtoInstanceName(id);
        final String nwInsName = GlobalConfigWriter.resolveVrfWithName(id);
        final String oldPolicy = getApplyPolicy(dataBefore);
        final String newPolicy = getApplyPolicy(dataAfter);
        blockingWriteAndRead(fT(LOCAL_AGGREGATE_CONFIG,
                "as", g.getConfig()
                        .getAs()
                        .getValue(),
                "instance", instName,
                "vrf", nwInsName,
                "newAfiSafis", g.getAfiSafis()
                        .getAfiSafi()
                        .stream()
                        .map(afi -> GlobalAfiSafiReader.transformAfiToString(afi.getAfiSafiName()))
                        .map(afi -> afi.replaceAll(" ", ""))
                        .collect(Collectors.toList()),
                "oldAfiSafis", Collections.EMPTY_LIST,
                "oldPolicy", oldPolicy,
                "newPolicy", newPolicy,
                "configNew", dataAfter,
                "configOld", dataBefore),
                cli, id, dataAfter);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        Optional<Bgp> bgpOptional = writeContext.readAfter(id.firstIdentifierOf(Protocol.class)
                .child(Bgp.class));
        if (!bgpOptional.isPresent()) {
            return;
        }
        final Global g = bgpOptional.get()
                .getGlobal();
        if (g.getAfiSafis() == null || g.getAfiSafis()
                .getAfiSafi() == null) {
            return;
        }
        final String instName = GlobalConfigWriter.getProtoInstanceName(id);
        final String nwInsName = GlobalConfigWriter.resolveVrfWithName(id);
        final String policy = getApplyPolicy(config);
        blockingDeleteAndRead(fT(LOCAL_AGGREGATE_CONFIG,
                "as", g.getConfig()
                        .getAs()
                        .getValue(),
                "instance", instName,
                "vrf", nwInsName,
                "newAfiSafis", Collections.EMPTY_LIST,
                "oldAfiSafis", g.getAfiSafis()
                        .getAfiSafi()
                        .stream()
                        .map(afi -> GlobalAfiSafiReader.transformAfiToString(afi.getAfiSafiName()))
                        .map(afi -> afi.replaceAll(" ", ""))
                        .collect(Collectors.toList()),
                "oldPolicy", policy,
                "newPolicy", null,
                "configNew", null,
                "configOld", config),
                cli, id);
    }

    private static String getApplyPolicy(Config config) {
        NiProtAggAug aug = config.getAugmentation(NiProtAggAug.class);

        if (aug == null) {
            return null;
        }

        List<String> policies = aug.getApplyPolicy();
        if (policies == null || policies.isEmpty()) {
            return null;
        }
        return policies.get(0);
    }
}