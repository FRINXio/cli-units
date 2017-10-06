package io.frinx.cli.ospf.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ospf.OspfProtocolReader;
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.GlobalBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.Config;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class GlobalConfigReader implements CliReader<Config, ConfigBuilder> {

    private Cli cli;

    public GlobalConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public ConfigBuilder getBuilder(@Nonnull InstanceIdentifier<Config> instanceIdentifier) {
        return new ConfigBuilder();
    }

    static final String SH_OSPF = "sh ip ospf %s";
    static final Pattern ROUTER_ID = Pattern.compile(".*?with ID (?<routerId>[^\\s]+).*");

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        // FIXME extract this check into superclass since it is used everywhere under /network-instance/protocol/
        if(!instanceIdentifier.firstKeyOf(Protocol.class).getIdentifier().equals(OspfProtocolReader.TYPE)) {
            return;
        }

        String ospfId = instanceIdentifier.firstKeyOf(Protocol.class).getName();
        parseGlobal(blockingRead(String.format(SH_OSPF, ospfId), cli, instanceIdentifier), configBuilder);
    }

    @VisibleForTesting
    static void parseGlobal(String output, ConfigBuilder builder) {
        ParsingUtils.parseField(output, 0,
                ROUTER_ID::matcher,
                matcher -> matcher.group("routerId"),
                value -> builder.setRouterId(new DottedQuad(value)));
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Config config) {
        ((GlobalBuilder) builder).setConfig(config);
    }
}
