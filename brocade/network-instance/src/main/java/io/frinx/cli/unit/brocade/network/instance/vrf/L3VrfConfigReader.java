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

package io.frinx.cli.unit.brocade.network.instance.vrf;

import com.google.common.annotations.VisibleForTesting;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ni.base.handler.vrf.AbstractL3VrfConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;

public final class L3VrfConfigReader extends AbstractL3VrfConfigReader {

    private static final Pattern RD_LINE = Pattern.compile(".*rd (?<rd>(\\S+):(\\S+)).*");
    private static final Pattern DESCR_LINE = Pattern.compile("");

    public L3VrfConfigReader(Cli cli) {
        super(new L3VrfReader(cli), cli);
    }

    @Override
    protected String getReadCommand() {
        return L3VrfReader.SH_IP_VRF;
    }

    @VisibleForTesting
    @Override
    public void parseVrfConfig(String output, ConfigBuilder builder) {
        String realignedOutput = realignOutput(output);

        String config = ParsingUtils.NEWLINE.splitAsStream(realignedOutput)
                // we can call builder.getName(), since this was previously set in the parent class
                .filter(vrfConfigLine -> vrfConfigLine.contains(String.format("vrf %s ", builder.getName())))
                .findAny()
                .orElse("");
        super.parseVrfConfig(config, builder);
    }

    @Override
    protected Pattern getRouteDistinguisherLine() {
        return RD_LINE;
    }

    @Override
    protected Pattern getDescriptionLine() {
        return DESCR_LINE;
    }

    private static String realignOutput(String output) {
        String withoutNewlines = output.replaceAll("[\r\n]", "");
        // We want to see the output in the form
        // " \nip vrf VRF  rd RD ip vrf VRF_WITHOUT_RD \nip vrf ANOTHER_VRF  rd RD \nip vrf ANOTHER_VRF_WITHOUT_RD ".
        // Note the space after each vrf name. That way we can distinguish VRFs
        // with names that are prefixes of name of some another VRFs in the
        // following processing of the config.
        return withoutNewlines.replaceAll("$", " ").replaceAll("vrf ", " \nvrf ");
    }
}
