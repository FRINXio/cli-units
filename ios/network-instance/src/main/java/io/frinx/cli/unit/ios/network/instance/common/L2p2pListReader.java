package io.frinx.cli.unit.ios.network.instance.common;

import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.registry.common.TypedListReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.CliOperListReader;
import java.util.AbstractMap;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public interface L2p2pListReader<O extends DataObject & Identifiable<K>, K extends Identifier<O>, B extends Builder<O>> extends TypedListReader<O, K, B> {

    @Nullable
    @Override
    default Map.Entry<InstanceIdentifier<? extends DataObject>, Function<DataObject, Boolean>> getParentCheck(InstanceIdentifier<O> id) {
        return new AbstractMap.SimpleEntry<>(
                RWUtils.cutId(id, NetworkInstance.class).child(Config.class),
                L2p2pReader.L2P2P_CHECK);
    }

    interface L2p2pConfigListReader<O extends DataObject & Identifiable<K>, K extends Identifier<O>, B extends Builder<O>> extends L2p2pListReader<O, K, B>, CliConfigListReader<O, K, B> {}
    interface L2p2pOperListReader<O extends DataObject & Identifiable<K>, K extends Identifier<O>, B extends Builder<O>> extends L2p2pListReader<O, K, B>, CliOperListReader<O, K, B> {}
}
