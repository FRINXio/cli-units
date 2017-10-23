package io.frinx.cli.ospf.handler;

import static io.frinx.cli.unit.utils.ParsingUtils.NEWLINE;
import static io.frinx.cli.unit.utils.ParsingUtils.parseField;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ospf.common.OspfListReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.AreaKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AreaInterfaceReader implements OspfListReader<Interface, InterfaceKey, InterfaceBuilder> {

    private static final String SHOW_OSPF_INT = "sh ip ospf %s int brie";
    private static final Pattern INTERFACE_AREA =
            Pattern.compile("(?<interface>[^\\s]+)\\s+(?<pid>[^\\s]+)\\s+(?<area>[^\\s]+)\\s+(?<ip>[^\\s]+)\\s+(?<cost>[^\\s]+).*");
    private static final Pattern INTERFACE_ID_LINE = Pattern.compile("(?<id>[^\\s]+).*");

    private Cli cli;

    public AreaInterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<InterfaceKey> getAllIdsForType(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                        @Nonnull ReadContext readContext) throws ReadFailedException {
        ProtocolKey protocolKey = instanceIdentifier.firstKeyOf(Protocol.class);
        AreaKey areaKey = instanceIdentifier.firstKeyOf(Area.class);

        String output = blockingRead(String.format(SHOW_OSPF_INT, protocolKey.getName()), cli, instanceIdentifier, readContext);

        // FIXME this conversion from short to long IFC name is duplicite with NetworkInstanceInterfaceReader
        // TODO extract into utils or an interface with default method  and reuse
        ArrayList<InterfaceKey> longNames = new ArrayList<>();
        for (InterfaceKey shortIfcKey : parseInterfaceIds(areaKey, output)) {
            String outputIfc = blockingRead(
                    String.format("sh ip interface brief %s", shortIfcKey.getId()), cli, instanceIdentifier, readContext);
            longNames.add(new InterfaceKey(
                    parseField(outputIfc, 1,
                            INTERFACE_ID_LINE::matcher,
                            matcher -> matcher.group("id"))
                            .orElse(shortIfcKey.getId())));
        }

        return longNames;
    }

    @VisibleForTesting
    static List<InterfaceKey> parseInterfaceIds(AreaKey areaKey, String output) {
        return NEWLINE.splitAsStream(output)
                .map(String::trim)
                .map(INTERFACE_AREA::matcher)
                .filter(Matcher::matches)
                // Filter out only current area
                .filter(m -> m.group("area").equals(areaKey.getIdentifier().getUint32().toString()))
                .map(matcher -> matcher.group("interface"))
                .map(InterfaceKey::new)
                .collect(Collectors.toList());
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Interface> list) {
        ((InterfacesBuilder) builder).setInterface(list);
    }

    @Nonnull
    @Override
    public InterfaceBuilder getBuilder(@Nonnull InstanceIdentifier<Interface> instanceIdentifier) {
        return new InterfaceBuilder();
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                             @Nonnull InterfaceBuilder interfaceBuilder,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        interfaceBuilder.setId(instanceIdentifier.firstKeyOf(Interface.class).getId());
    }
}
