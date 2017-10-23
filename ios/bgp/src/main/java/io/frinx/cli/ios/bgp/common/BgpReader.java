package io.frinx.cli.ios.bgp.common;

import io.frinx.cli.registry.common.TypedReader;
import io.frinx.cli.unit.utils.CliReader;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.policy.types.rev160512.BGP;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifier;

public interface BgpReader<O extends DataObject, B extends Builder<O>> extends TypedReader<O, B>, CliReader<O, B> {

    Class<BGP> TYPE = BGP.class;

    @Override
    default Identifier<? extends DataObject> getKey() {
        return new ProtocolKey(TYPE, null);
    }
}
