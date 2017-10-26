package io.frinx.cli.ospf.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ospf.common.OspfReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.GlobalBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.State;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.StateBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class GlobalStateReader implements OspfReader<State, StateBuilder> {

    private Cli cli;

    public GlobalStateReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public StateBuilder getBuilder(@Nonnull InstanceIdentifier<State> instanceIdentifier) {
        return new StateBuilder();
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<State> instanceIdentifier,
                                      @Nonnull StateBuilder configBuilder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        String ospfId = instanceIdentifier.firstKeyOf(Protocol.class).getName();
        parseGlobal(blockingRead(String.format(GlobalConfigReader.SH_OSPF, ospfId), cli, instanceIdentifier, ctx), configBuilder);
    }

    @VisibleForTesting
    static void parseGlobal(String output, StateBuilder builder) {
        ParsingUtils.parseField(output, 0,
                GlobalConfigReader.ROUTER_ID::matcher,
                matcher -> matcher.group("routerId"),
                value -> builder.setRouterId(new DottedQuad(value)));
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull State config) {
        ((GlobalBuilder) builder).setState(config);
    }
}
