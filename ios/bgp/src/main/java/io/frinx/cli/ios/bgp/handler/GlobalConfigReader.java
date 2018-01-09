/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.bgp.handler;

import static io.frinx.cli.unit.utils.ParsingUtils.NEWLINE;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.bgp.BgpReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.GlobalBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.yang.rev170403.DottedQuad;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class GlobalConfigReader implements BgpReader.BgpConfigReader<Config, ConfigBuilder> {

    private static final String SH_SUMM = "sh run | include ^router bgp|^ *address-family|^ *bgp router-id";
    private static final Pattern AS_PATTERN = Pattern.compile("router bgp (?<as>\\S*).*");
    private static final Pattern ROUTER_ID_PATTERN_GLOBAL = Pattern.compile("\\s*router bgp (?<as>\\S*)\\s+bgp router-id (?<routerId>\\S*).*");
    private static final Pattern ROUTER_ID_PATTERN = Pattern.compile("\\s*address-family (?<family>\\S*) vrf (?<vrf>\\S*)\\s+bgp router-id (?<routerId>\\S*).*");

    private Cli cli;

    public GlobalConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Config config) {
        ((GlobalBuilder) builder).setConfig(config);
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Config> id,
                                             @Nonnull ConfigBuilder configBuilder,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        String output = blockingRead(SH_SUMM, cli, id, readContext);
        NetworkInstanceKey vrfKey = id.firstKeyOf(NetworkInstance.class);

        parseConfigAttributes(output, configBuilder, vrfKey);
    }

    @VisibleForTesting
    public static void parseConfigAttributes(String output, ConfigBuilder builder, NetworkInstanceKey vrfKey) {
        parseGlobalAs(output, builder);
        if (vrfKey.equals(NetworInstance.DEFAULT_NETWORK)) {
            setGlobalRouterId(builder, output);
        } else {
            setVrfRouterId(builder, output, vrfKey.getName());
        }
    }

    private static void setGlobalRouterId(ConfigBuilder cBuilder, String output) {
        output = realignOutput(output);

        ParsingUtils.parseField(output, 0,
            ROUTER_ID_PATTERN_GLOBAL::matcher,
            matcher -> matcher.group("routerId"),
            (String value) -> cBuilder.setRouterId(new DottedQuad(value)));
    }

    private static String realignOutput(String output) {
        output = output.replaceAll(NEWLINE.pattern(), "");
        output = output.replaceAll("address-family", "\naddress-family");
        return output;
    }

    private static void setVrfRouterId(ConfigBuilder cBuilder, String output, String vrf) {
        output = realignOutput(output);

        NEWLINE.splitAsStream(output)
                .map(String::trim)
                .map(ROUTER_ID_PATTERN::matcher)
                .filter(Matcher::matches)
                .filter(m -> m.group("vrf").equals(vrf))
                .map(m -> m.group("routerId"))
                .findFirst()
                .map(DottedQuad::new)
                .ifPresent(cBuilder::setRouterId);
    }

    @VisibleForTesting
    public static void parseGlobalAs(String output, ConfigBuilder cBuilder) {
        ParsingUtils.parseField(output, 0,
            AS_PATTERN::matcher,
            matcher -> matcher.group("as"),
            (String value) -> cBuilder.setAs(new AsNumber(Long.valueOf(value))));
    }

}
