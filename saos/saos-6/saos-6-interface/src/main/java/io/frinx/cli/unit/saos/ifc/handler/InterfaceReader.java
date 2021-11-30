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

package io.frinx.cli.unit.saos.ifc.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceReader implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder> {

    public static final String SH_PORTS = "port show";
    public static final String LOGICAL_INTERFACE = "interface show";
    private static final String LAG_PORTS = "configuration search string \"aggregation create\"";
    protected static final Pattern INTERFACE_ID_LINE = Pattern.compile("\\|\\s(?<id>\\d+)\\s+\\|.*");
    protected static final Pattern LAG_INTERFACE_ID_LINE = Pattern.compile(".*agg (?<id>\\S+)");
    public static final Pattern LOGICAL_INTERFACE_ID_LINE = Pattern.compile("\\|\\s+(?<id>\\S+)\\s+\\|"
            + "[\\s\\S]+\\|(?!\\s+IP)[\\s\\S]+\\|");
    private final Cli cli;

    public InterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<InterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                        @Nonnull ReadContext readContext) throws ReadFailedException {
        List<InterfaceKey> portIds = getAllIds(blockingRead(SH_PORTS, cli, instanceIdentifier, readContext),
                INTERFACE_ID_LINE);
        List<InterfaceKey> aggIds = getAllIds(blockingRead(LAG_PORTS, cli, instanceIdentifier, readContext),
                LAG_INTERFACE_ID_LINE);
        List<InterfaceKey> locIds = getAllIds(blockingRead(LOGICAL_INTERFACE, cli, instanceIdentifier, readContext),
                LOGICAL_INTERFACE_ID_LINE);
        List<InterfaceKey> ids = new ArrayList<>();
        ids.addAll(portIds);
        ids.addAll(aggIds);
        ids.addAll(locIds);
        return ids;
    }

    @VisibleForTesting
    public static List<InterfaceKey> getAllIds(String output, Pattern pattern) {
        return ParsingUtils.parseFields(output, 0,
            pattern::matcher,
            m -> m.group("id"), InterfaceKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                      @Nonnull InterfaceBuilder builder, @Nonnull ReadContext readContext) {
        builder.setName(instanceIdentifier.firstKeyOf(Interface.class).getName());
    }
}