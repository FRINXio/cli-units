/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.sonic.ifc.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_INTERFACE_CFG = "show running-config";
    private static final String SINGLE_INTERFACE_STR = "(interface %s)(?s).*?(interface %s|^!|^router)";
    private static final String LAST_SINGLE_INTERFACE_STR = "(interface %s)(?s).*";

    private static final Pattern INTERFACE_ID = Pattern.compile("^interface (?<id>\\S+)$");
    private static final Pattern INTERFACE_DESCRIPTION = Pattern.compile("^description (?<desc>.*)");
    private Cli cli;

    public InterfaceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull final InstanceIdentifier<Config> id,
                                      @Nonnull final ConfigBuilder builder,
                                      @Nonnull final ReadContext ctx) throws ReadFailedException {
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
            INTERFACE_DESCRIPTION::matcher,
            matcher -> matcher.group("desc"),
            builder::setDescription);
    }

    protected void parseEnabled(final String output, final ConfigBuilder builder) {
        builder.setEnabled(true);
        if (output.contains("shutdown")) {
            builder.setEnabled(false);
        }
    }

    private static String nextId(String output, String ifcId) {
        List<String> ids = ParsingUtils.parseFields(output, 0,
            INTERFACE_ID::matcher,
            matcher -> matcher.group("id").trim(),
            value -> value);
        int nextID = ids.indexOf(ifcId) + 1;
        if (nextID >= ids.size()) {
            return null;
        }
        return String.valueOf(ids.get(nextID));
    }

    private static String cutOutInterface(final String output, final String name) {
        String nextId = nextId(output, name);
        Matcher ifcMatcher;
        if (nextId != null) {
            ifcMatcher = Pattern.compile(String.format(SINGLE_INTERFACE_STR, name, nextId))
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