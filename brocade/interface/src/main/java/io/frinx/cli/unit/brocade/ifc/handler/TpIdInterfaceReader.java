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

package io.frinx.cli.unit.brocade.ifc.handler;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.brocade.ifc.Util;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X8100;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X88A8;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X9100;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X9200;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPIDTYPES;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TpIdInterfaceReader implements CliConfigReader<Config1, Config1Builder> {

    private static final String SH_IFC_TAG_TYPE = "show running-config | include tag-type";
    private static final Pattern TAG_TYPE = Pattern.compile(".*tag-type (?<tpid>\\S+) (?<ifcType>\\S+) "
            + "(?<ifcNumber>\\S+).*");

    private Cli cli;

    public TpIdInterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public Config1Builder getBuilder(@NotNull InstanceIdentifier<Config1> id) {
        return new Config1Builder();
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config1> id,
                                      @NotNull Config1Builder builder,
                                      @NotNull ReadContext ctx) throws ReadFailedException {
        String name = id.firstKeyOf(Interface.class).getName();
        Class<? extends InterfaceType> ifcType = Util.parseType(name);
        String typeOnDevice = Util.getTypeOnDevice(ifcType);
        String ifcNumber = Util.getIfcNumber(name);

        parseTagTypes(blockingRead(SH_IFC_TAG_TYPE, cli, id, ctx), typeOnDevice, ifcNumber, builder);
    }

    private void parseTagTypes(String output, String ifcType, String ifcNumber, Config1Builder builder) {
        ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .map(TAG_TYPE::matcher)
                .filter(Matcher::matches)
                .filter(m -> ifcType.startsWith(m.group("ifcType"))
                        && ifcNumber.equals(m.group("ifcNumber")))
                .findFirst()
                .map(m -> m.group("tpid"))
                .ifPresent(s -> builder.setTpid(parseTpId(s)));
    }

    private static final Set<Class<? extends TPIDTYPES>> SUPPORTED_IDS = Sets.newHashSet(
            TPID0X8100.class, TPID0X88A8.class, TPID0X9100.class, TPID0X9200.class
    );

    //map default tag values, may not be accurate, device has possibility to change default tag-values
    private static final Map<String, Class<? extends TPIDTYPES>> SUPPORTED_TAGS = Map.of(
        "tag1", TPID0X8100.class,
        "tag2", TPID0X88A8.class
    );

    private Class<? extends TPIDTYPES> parseTpId(String string) {
        Class<? extends TPIDTYPES> tpId = SUPPORTED_IDS.stream()
                .filter(cls -> cls.getSimpleName().toLowerCase(Locale.ROOT).contains(string.toLowerCase(Locale.ROOT)))
                .findFirst()
                .orElseGet(() -> {
                    // Other tags are unsupported
                    // This also applies to some Ironware devices where tag-type command has different syntax
                    return null;
                });
        if (tpId == null) {
            return SUPPORTED_TAGS.get(string.toLowerCase(Locale.ROOT));
        }
        return tpId;

    }

    @Override
    public void merge(@NotNull Builder<? extends DataObject> parentBuilder, @NotNull Config1 readValue) {
        ((ConfigBuilder) parentBuilder).addAugmentation(Config1.class, readValue);
    }
}