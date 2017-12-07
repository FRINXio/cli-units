package io.frinx.cli.unit.brocade.ifc.handler;

import static io.frinx.cli.unit.brocade.ifc.handler.InterfaceConfigReader.getIfcNumber;
import static io.frinx.cli.unit.brocade.ifc.handler.InterfaceConfigReader.getTypeOnDevice;
import static io.frinx.cli.unit.brocade.ifc.handler.InterfaceConfigReader.parseType;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X8100;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X8A88;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X9100;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X9200;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPIDTYPES;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TpIdInterfaceReader implements CliConfigReader<Config1, Config1Builder> {

    private static final String SH_IFC_TAG_TYPE = "sh run | include tag-type";
    private static final Pattern TAG_TYPE = Pattern.compile(".*tag-type (?<tpid>\\S+) (?<ifcType>\\S+) (?<ifcNumber>\\S+).*");

    private Cli cli;

    public TpIdInterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public Config1Builder getBuilder(@Nonnull InstanceIdentifier<Config1> id) {
        return new Config1Builder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config1> id,
                                      @Nonnull Config1Builder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        String name = id.firstKeyOf(Interface.class).getName();
        Class<? extends InterfaceType> ifcType = parseType(name);
        String typeOnDevice = getTypeOnDevice(ifcType);
        String ifcNumber = getIfcNumber(name);

        parseTagTypes(blockingRead(SH_IFC_TAG_TYPE, cli, id, ctx), typeOnDevice, ifcNumber, builder);
    }

    private void parseTagTypes(String output, String ifcType, String ifcNumber, Config1Builder builder) {
        ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .map(TAG_TYPE::matcher)
                .filter(Matcher::matches)
                .filter(m -> ifcType.startsWith(m.group("ifcType")) && ifcNumber.equals(m.group("ifcNumber")))
                .findFirst()
                .map(m -> m.group("tpid"))
                .ifPresent(s -> builder.setTpid(parseTpId(s)));
    }

    private static final Set<Class<? extends TPIDTYPES>> SUPPORTED_IDS = Sets.newHashSet(
            TPID0X8100.class, TPID0X8A88.class, TPID0X9100.class, TPID0X9200.class
    );

    private Class<? extends TPIDTYPES> parseTpId(String s) {
        return SUPPORTED_IDS.stream()
                .filter(cls -> cls.getSimpleName().toLowerCase().contains(s.toLowerCase()))
                .findFirst()
                .orElseGet(() -> {
                    // 88a8 on device == 8A88
                    if (s.toLowerCase().equals("88a8")) {
                        return TPID0X8A88.class;
                    } else {
                        // Other tags are unsupported
                        // This also applies to some Ironware devices where tag-type command has different syntax
                        return null;
                    }
                });

    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull Config1 readValue) {
        ((ConfigBuilder) parentBuilder).addAugmentation(Config1.class, readValue);
    }
}
