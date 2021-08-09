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

package io.frinx.cli.unit.huawei.network.instance.handler.vrf;

import com.google.common.annotations.VisibleForTesting;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ni.base.handler.vrf.AbstractL3VrfConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.huawei.rev210726.HuaweiNiAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.huawei.rev210726.HuaweiNiAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;

public final class L3VrfConfigReader extends AbstractL3VrfConfigReader {

    private static final String DISPLAY_VRF_CFG = "display current-configuration configuration ip vpn-instance";
    private static final Pattern DESC_CONFIG = Pattern.compile(".*description (?<desc>.*)");
    private static final Pattern RD_CONFIG = Pattern.compile(".*route-distinguisher (?<rd>\\S+).*");
    private static final Pattern PREFIX_LIMIT = Pattern.compile(".*prefix limit (?<from>\\d+) (?<to>\\d+).*");

    public L3VrfConfigReader(Cli cli) {
        super(new L3VrfReader(cli), cli);
    }

    @VisibleForTesting
    @Override
    public void parseVrfConfig(String output, ConfigBuilder builder) {
        String config = Stream.of(output.split("#"))
                .filter(vrfConfigLine -> vrfConfigLine.contains("ip vpn-instance " + builder.getName()))
                .findAny()
                .orElse("");

        HuaweiNiAugBuilder augBuilder = new HuaweiNiAugBuilder();
        ParsingUtils.NEWLINE.splitAsStream(config.trim())
            .map(PREFIX_LIMIT::matcher)
            .filter(Matcher::matches)
            .forEach(matcher -> {
                String from = matcher.group("from");
                String to = matcher.group("to");
                augBuilder.setPrefixLimitFrom(Short.valueOf(from));
                augBuilder.setPrefixLimitTo(Short.valueOf(to));
            });

        if (augBuilder.getPrefixLimitTo() != null || augBuilder.getPrefixLimitFrom() != null) {
            builder.addAugmentation(HuaweiNiAug.class, augBuilder.build());
        }

        super.parseVrfConfig(config, builder);
    }

    @Override
    protected String getReadCommand() {
        return DISPLAY_VRF_CFG;
    }

    @Override
    protected Pattern getRouteDistinguisherLine() {
        return RD_CONFIG;
    }

    @Override
    protected Pattern getDescriptionLine() {
        return DESC_CONFIG;
    }
}
