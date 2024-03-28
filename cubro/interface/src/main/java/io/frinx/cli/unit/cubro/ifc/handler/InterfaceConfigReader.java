/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.cubro.ifc.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cubro.extension.rev200317.IfCubroAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cubro.extension.rev200317.IfCubroAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_INTERFACE_CFG = "show running-config";
    private static final String SINGLE_INTERFACE_STR = "(interface %s )(?s).*?(interface %s|^!|^access-list)";
    private static final String LAST_SINGLE_INTERFACE_STR = "(interface %s )(?s).*";

    private static final Pattern INTERFACE_ID = Pattern.compile("^interface (?<id>\\d+)$");
    private static final Pattern INTERFACE_MTU = Pattern.compile("mtu (?<mtu>[0-9]+)");
    private static final Pattern INTERFACE_COMMENT = Pattern.compile("^interface [0-9]+ comment (?<desc>.*)");
    private static final Pattern INTERFACE_SPEED = Pattern.compile("^speed (?<speed>.*)");
    private static final Pattern INTERFACE_ELAG = Pattern.compile("^elag (?<elag>\\d+)$");
    private Cli cli;

    public InterfaceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull final InstanceIdentifier<Config> id,
                                      @NotNull final ConfigBuilder builder,
                                      @NotNull final ReadContext ctx) throws ReadFailedException {
        final String ifcId = id.firstKeyOf(Interface.class).getName();
        parseInterface(blockingRead(SH_INTERFACE_CFG, cli, id, ctx), builder, ifcId);
    }

    @VisibleForTesting
    void parseInterface(String output, final ConfigBuilder builder, String name)  {
        output = cutOutInterface(output, name);
        parseEnabled(output, builder);
        builder.setName(name);
        builder.setType(EthernetCsmacd.class);

        ParsingUtils.parseFields(output, 0,
            INTERFACE_COMMENT::matcher,
            matcher -> matcher.group("desc"),
            builder::setDescription);

        ParsingUtils.parseFields(output, 0,
            INTERFACE_MTU::matcher,
            matcher -> Integer.parseInt(matcher.group("mtu")),
            builder::setMtu);

        IfCubroAugBuilder ifCubroAugBuilder = new IfCubroAugBuilder();
        ParsingUtils.findMatch(output, Pattern.compile("^rx.*"), ifCubroAugBuilder::setRx);
        ParsingUtils.parseFields(output, 0,
            INTERFACE_SPEED::matcher,
            matcher -> matcher.group("speed"),
            ifCubroAugBuilder::setSpeed);

        setElags(output, ifCubroAugBuilder);
        ParsingUtils.findMatch(output, Pattern.compile("^innerhash.*"), ifCubroAugBuilder::setInnerhash);
        ParsingUtils.findMatch(output, Pattern.compile("^inneracl.*"), ifCubroAugBuilder::setInneracl);
        ParsingUtils.findMatch(output, Pattern.compile("^vxlanterminated.*"), ifCubroAugBuilder::setVxlanterminated);

        builder.addAugmentation(IfCubroAug.class, ifCubroAugBuilder.build());
    }

    protected void parseEnabled(final String output, final ConfigBuilder builder) {
        builder.setEnabled(true);
        if (output.contains("shutdown")) {
            builder.setEnabled(false);
        }
    }

    private void setElags(String output, IfCubroAugBuilder ifCubroAugBuilder) {
        List<Short> elags = ParsingUtils.parseFields(output, 0,
            INTERFACE_ELAG::matcher,
            matcher -> Short.parseShort(matcher.group("elag")),
            value -> value);
        if (elags.isEmpty()) {
            elags = null;
        }
        ifCubroAugBuilder.setElag(elags);
    }

    private static String nextId(String output, String ifcId) {
        List<Integer> ids = ParsingUtils.parseFields(output, 0,
            INTERFACE_ID::matcher,
            matcher -> Integer.parseInt(matcher.group("id").trim()),
            value -> value);
        int nextID = ids.indexOf(Integer.parseInt(ifcId)) + 1;
        if (nextID >= ids.size()) {
            return null;
        }
        return String.valueOf(ids.get(nextID));
    }

    private static String cutOutInterface(final String output, final String name) {
        String nextId = nextId(output, name);
        Matcher ifcMatcher;
        if (nextId != null) {
            ifcMatcher = Pattern.compile(String.format(SINGLE_INTERFACE_STR, name, nextId(output, name)))
                    .matcher(output);
        } else {
            ifcMatcher = Pattern.compile(String.format(LAST_SINGLE_INTERFACE_STR, name))
                    .matcher(output);
        }
        if (ifcMatcher.find()) {
            return ifcMatcher.group(0);
        }
        return null;
    }
}