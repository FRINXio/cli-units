package io.frinx.cli.ospf.handler.table;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.network.instance.L3VrfWriter;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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

public class OspfTableConnectionWriter implements
        L3VrfWriter<Config> {

    private static final String UPDATE_REDIS = "configure terminal\n" +
            "router ospf {$ospf}" +
            "{.if ($vrf) } vrf {$vrf}{/if}" +
            "\n" +
            "{.if ($add) }{.else}no {/if}" +
            "redistribute {$protocol} {$protocol_id}" +
            "{.if ($add) }{.if ($policy) } route-map {$policy}{/if}{/if}" +
            "\n" +
            "end";

    private Cli cli;

    public OspfTableConnectionWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier,
                                              Config config,
                                              WriteContext writeContext) throws WriteFailedException {
        if (config.getDstProtocol().equals(OSPF.class)) {
            List<Protocol> allProtocols = writeContext.readAfter(RWUtils.cutId(instanceIdentifier, IIDs.NE_NETWORKINSTANCE).child(Protocols.class))
                    .or(new ProtocolsBuilder().setProtocol(Collections.emptyList()).build())
                    .getProtocol();

            List<Protocol> dstProtocols = allProtocols.stream()
                    .filter(p -> p.getIdentifier().equals(OSPF.class))
                    .collect(Collectors.toList());

            for (Protocol dstProtocol : dstProtocols) {
                writeCurrentAttributesForOspf(instanceIdentifier, dstProtocol, config, allProtocols, true);
            }
        }
    }

    private void writeCurrentAttributesForOspf(InstanceIdentifier<Config> id,
                                               Protocol ospfProtocol,
                                               Config config,
                                               List<Protocol> protocols,
                                               boolean add) throws WriteFailedException.CreateFailedException {
        NetworkInstanceKey vrfKey = id.firstKeyOf(NetworkInstance.class);

        List<Protocol> srcProtocols = protocols.stream()
                .filter(p -> p.getIdentifier().equals(config.getSrcProtocol()))
                .collect(Collectors.toList());

        Preconditions.checkArgument(!srcProtocols.isEmpty(),
                "No protocols: %s configured in current network", config.getSrcProtocol());

        List<String> importPolicy = config.getImportPolicy() == null ? Collections.emptyList() : config.getImportPolicy();

        Preconditions.checkArgument(importPolicy.isEmpty() || importPolicy.size() ==1,
                "Only a single import policy is supported: %s", importPolicy);

        for (Protocol srcProto : srcProtocols) {
            blockingWriteAndRead(cli, id, config,
                    fT(UPDATE_REDIS,
                            "ospf", ospfProtocol.getName(),
                            "add", add ? true : null,
                            "protocol", toDeviceProtocol(srcProto.getIdentifier()),
                            "protocol_id", srcProto.getIdentifier().equals(BGP.class) ? srcProto.getBgp().getGlobal().getConfig().getAs().getValue() : srcProto.getName(),
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

        throw new IllegalArgumentException("Protocol of type: " + identifier + " is not supported");
    }

    @Override
    public void updateCurrentAttributesForType(InstanceIdentifier<Config> id,
                                               Config dataBefore,
                                               Config dataAfter,
                                               WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributesForType(id, dataBefore, writeContext);
        deleteCurrentAttributesForType(id, dataBefore, writeContext);
    }

    @Override
    public void deleteCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier,
                                               Config config,
                                               WriteContext writeContext) throws WriteFailedException {
        if (config.getDstProtocol().equals(OSPF.class)) {
            List<Protocol> allProtocols = writeContext.readBefore(RWUtils.cutId(instanceIdentifier, IIDs.NE_NETWORKINSTANCE).child(Protocols.class))
                    .or(new ProtocolsBuilder().setProtocol(Collections.emptyList()).build())
                    .getProtocol();

            List<Protocol> dstProtocols = allProtocols.stream()
                    .filter(p -> p.getIdentifier().equals(OSPF.class))
                    .collect(Collectors.toList());

            for (Protocol dstProtocol : dstProtocols) {
                writeCurrentAttributesForOspf(instanceIdentifier, dstProtocol, config, allProtocols, false);
            }
        }
    }
}
