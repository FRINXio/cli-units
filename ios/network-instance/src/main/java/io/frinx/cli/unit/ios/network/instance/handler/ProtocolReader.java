package io.frinx.cli.unit.ios.network.instance.handler;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.bgp.handler.BgpProtocolReader;
import io.frinx.cli.ospf.OspfProtocolReader;
import io.frinx.cli.unit.utils.CliListReader;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ProtocolsBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ProtocolReader implements CliListReader<Protocol, ProtocolKey, ProtocolBuilder> {

    private final List<CliListReader<Protocol, ProtocolKey, ProtocolBuilder>> specificReaders;

    public ProtocolReader(Cli cli) {
        specificReaders = new ArrayList<CliListReader<Protocol, ProtocolKey, ProtocolBuilder>>() {{
            add(new OspfProtocolReader(cli));
            add(new BgpProtocolReader(cli));
        }};
    }

    @Nonnull
    @Override
    public List<ProtocolKey> getAllIds(@Nonnull InstanceIdentifier<Protocol> instanceIdentifier,
                                       @Nonnull ReadContext readContext) throws ReadFailedException {
        ArrayList<ProtocolKey> allIds = Lists.newArrayList();
        for (CliListReader<Protocol, ProtocolKey, ProtocolBuilder> specificReader : specificReaders) {
            allIds.addAll(specificReader.getAllIds(instanceIdentifier, readContext));
        }
        return allIds;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Protocol> list) {
        ((ProtocolsBuilder) builder).setProtocol(list);
    }

    @Nonnull
    @Override
    public ProtocolBuilder getBuilder(@Nonnull InstanceIdentifier<Protocol> instanceIdentifier) {
        return new ProtocolBuilder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Protocol> instanceIdentifier,
                                      @Nonnull ProtocolBuilder protocolBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        // Invoking all specific readers here, each reader is responsible to check whether it is its type of
        // protocol and if so, set the values
        for (CliListReader<Protocol, ProtocolKey, ProtocolBuilder> specificReader : specificReaders) {
            specificReader.readCurrentAttributes(instanceIdentifier, protocolBuilder, readContext);
        }
    }
}
