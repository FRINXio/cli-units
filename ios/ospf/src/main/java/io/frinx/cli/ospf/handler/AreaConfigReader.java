package io.frinx.cli.ospf.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.ospf.OspfProtocolReader;
import io.frinx.cli.unit.utils.CliReader;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.structure.Config;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.structure.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.AreaBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AreaConfigReader implements CliReader<Config, ConfigBuilder> {

    @Nonnull
    @Override
    public ConfigBuilder getBuilder(@Nonnull InstanceIdentifier<Config> instanceIdentifier) {
        return new ConfigBuilder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull ConfigBuilder configBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        if(instanceIdentifier.firstKeyOf(Protocol.class).getIdentifier().equals(OspfProtocolReader.TYPE)) {
            configBuilder.setIdentifier(instanceIdentifier.firstKeyOf(Area.class).getIdentifier());
        }
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Config config) {
        ((AreaBuilder) builder).setConfig(config);
    }
}
