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

package io.frinx.cli.unit.ospf.handler.table;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Protocols;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ProtocolsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.table.connection.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.BGP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.INSTALLPROTOCOLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.OSPF;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OspfTableConnectionWriter implements CompositeWriter.Child<Config>, CliWriter<Config> {

    @SuppressWarnings("checkstyle:linelength")
    private static final String UPDATE_REDIS = """
            configure terminal
            router ospf {$ospf}{.if ($vrf) } vrf {$vrf}{/if}
            {.if ($add) }{.else}no {/if}redistribute {$protocol} {$protocol_id}{.if ($add) }{.if ($policy) } route-map {$policy}{/if}{/if}
            end""";

    private Cli cli;

    public OspfTableConnectionWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                                 @NotNull Config config,
                                                 @NotNull WriteContext writeContext) throws WriteFailedException {
        boolean wasWriting = false;
        if (config.getDstProtocol()
                .equals(OSPF.class)) {
            List<Protocol> allProtocols = writeContext.readAfter(RWUtils.cutId(instanceIdentifier, IIDs
                    .NE_NETWORKINSTANCE)
                    .child(Protocols.class))
                    .orElse(new ProtocolsBuilder().setProtocol(Collections.emptyList())
                            .build())
                    .getProtocol();

            List<Protocol> dstProtocols = allProtocols.stream()
                    .filter(p -> p.getIdentifier()
                            .equals(OSPF.class))
                    .toList();

            for (Protocol dstProtocol : dstProtocols) {
                writeCurrentAttributesForOspf(instanceIdentifier, dstProtocol, config, allProtocols, true);
                wasWriting = true;
            }
        }
        return wasWriting;
    }

    private void writeCurrentAttributesForOspf(InstanceIdentifier<Config> id,
                                               Protocol ospfProtocol,
                                               Config config,
                                               List<Protocol> protocols,
                                               boolean add) throws WriteFailedException.CreateFailedException {
        NetworkInstanceKey vrfKey = id.firstKeyOf(NetworkInstance.class);

        List<Protocol> srcProtocols = protocols.stream()
                .filter(p -> p.getIdentifier()
                        .equals(config.getSrcProtocol()))
                .toList();

        Preconditions.checkArgument(!srcProtocols.isEmpty(),
                "No protocols: %s configured in current network", config.getSrcProtocol());

        List<String> importPolicy = config.getImportPolicy() == null ? Collections.emptyList() : config
                .getImportPolicy();

        Preconditions.checkArgument(importPolicy.isEmpty() || importPolicy.size() == 1,
                "Only a single import policy is supported: %s", importPolicy);

        for (Protocol srcProto : srcProtocols) {
            blockingWriteAndRead(cli, id, config,
                    fT(UPDATE_REDIS,
                            "ospf", ospfProtocol.getName(),
                            "add", add ? true : null,
                            "protocol", toDeviceProtocol(srcProto.getIdentifier()),
                            "protocol_id", srcProto.getIdentifier()
                                    .equals(BGP.class) ? srcProto.getBgp()
                                    .getGlobal()
                                    .getConfig()
                                    .getAs()
                                    .getValue() : srcProto.getName(),
                            "vrf", vrfKey.equals(NetworInstance.DEFAULT_NETWORK) ? null : vrfKey.getName(),
                            "policy", importPolicy.isEmpty() ? null : importPolicy.get(0)));
        }

    }

    private String toDeviceProtocol(Class<? extends INSTALLPROTOCOLTYPE> identifier) {
        if (identifier.equals(OSPF.class)) {
            return "ospf";
        }
        if (identifier.equals(BGP.class)) {
            return "bgp";
        }
        return identifier.getSimpleName();
    }

    @Override
    public boolean updateCurrentAttributesWResult(@NotNull InstanceIdentifier<Config> id,
                                                  @NotNull Config dataBefore,
                                                  @NotNull Config dataAfter,
                                                  @NotNull WriteContext writeContext) throws WriteFailedException {
        // this is fine, route-maps cannot be overwritten, you can add and delete
        boolean wasDeleting = deleteCurrentAttributesWResult(id, dataBefore, writeContext);
        boolean wasWriting = writeCurrentAttributesWResult(id, dataAfter, writeContext);
        return wasDeleting || wasWriting;
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                                  @NotNull Config config,
                                                  @NotNull WriteContext writeContext) throws WriteFailedException {
        boolean wasDeleting = false;
        if (config.getDstProtocol()
                .equals(OSPF.class)) {
            List<Protocol> allProtocols = writeContext.readBefore(RWUtils.cutId(instanceIdentifier, IIDs
                    .NE_NETWORKINSTANCE)
                    .child(Protocols.class))
                    .orElse(new ProtocolsBuilder().setProtocol(Collections.emptyList())
                            .build())
                    .getProtocol();

            List<Protocol> dstProtocols = allProtocols.stream()
                    .filter(p -> p.getIdentifier()
                            .equals(OSPF.class))
                    .toList();

            for (Protocol dstProtocol : dstProtocols) {
                writeCurrentAttributesForOspf(instanceIdentifier, dstProtocol, config, allProtocols, false);
                wasDeleting = true;
            }
        }
        return wasDeleting;
    }
}