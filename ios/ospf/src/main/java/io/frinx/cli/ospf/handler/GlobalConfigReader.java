/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ospf.handler;

import static io.frinx.cli.unit.utils.ParsingUtils.NEWLINE;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.ospf.OspfReader;
import io.frinx.cli.io.Cli;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.GlobalBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class GlobalConfigReader implements OspfReader.OspfConfigReader<Config, ConfigBuilder> {

    private Cli cli;

    public GlobalConfigReader(Cli cli) {
        this.cli = cli;
    }

    static final String SH_OSPF = "sh run | include router ospf|router-id";
    static final Pattern ROUTER_ID = Pattern.compile(".*?router-id (?<routerId>[^\\s]+).*");

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String ospfId = instanceIdentifier.firstKeyOf(Protocol.class).getName();
        parseGlobal(blockingRead(SH_OSPF, cli, instanceIdentifier, readContext), configBuilder, ospfId);
    }

    @VisibleForTesting
    static void parseGlobal(String output, ConfigBuilder builder, String ospfId) {
        output = output.replaceAll("\\n|\\r", "");

        output = output.replace("router ospf", "\nrouter ospf");

        NEWLINE.splitAsStream(output)
                .map(String::trim)
                .filter(s -> s.startsWith("router ospf " + ospfId))
                .map(ROUTER_ID::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group("routerId"))
                .findFirst()
                .ifPresent(rd -> builder.setRouterId(new DottedQuad(rd)));
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Config config) {
        ((GlobalBuilder) builder).setConfig(config);
    }
}
