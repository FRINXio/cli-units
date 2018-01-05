/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.bgp.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.bgp.BgpReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.GlobalBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.yang.rev170403.DottedQuad;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class GlobalConfigReader implements BgpReader.BgpConfigReader<Config, ConfigBuilder> {


    private static final String SH_BGP= "sh bgp %s summary";
    private static final String SH_BGP_INSTANCE = "show bgp instances | include %s";
    private static final Pattern CONFIG_LINE = Pattern.compile("BGP router identifier (?<id>.+), local AS number (?<as>.+)");

    private Cli cli;

    public GlobalConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Config config) {
        ((GlobalBuilder) builder).setConfig(config);
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                             @Nonnull ConfigBuilder configBuilder,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        final String name = instanceIdentifier.firstKeyOf(Protocol.class).getName().toString();
        // if the instance of bgp is not running, it does not return router ID nor AS
        parseRouterId(blockingRead(String.format(SH_BGP, (NetworInstance.DEFAULT_NETWORK_NAME.equals(name)) ? "" : "instance " + name),
                cli, instanceIdentifier, readContext), configBuilder);
        parseAs(blockingRead(String.format(SH_BGP_INSTANCE, name), cli, instanceIdentifier, readContext), configBuilder);
    }

    @VisibleForTesting
    public static void parseRouterId(String output, ConfigBuilder cBuilder) {
        ParsingUtils.parseField(output, 0,
            CONFIG_LINE::matcher,
            matcher -> matcher.group("id"),
            value -> cBuilder.setRouterId(new DottedQuad(value)));
    }

    @VisibleForTesting
    public static void parseAs(String output, ConfigBuilder cBuilder) {
        ParsingUtils.parseField(output.replaceAll("\\h+", " "),
            BgpProtocolReader.INSTANCE_LINE::matcher,
            matcher -> matcher.group("as"),
            value -> cBuilder.setAs(new AsNumber(Long.valueOf(value))));
    }
}
