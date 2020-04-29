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

package io.frinx.cli.unit.saos.qos.handler.ifc;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos.qos.Util;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosIfAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosIfAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosIfExtensionConfig;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SHOW_COMMAND = "configuration search running-config string \"traffic-profiling\"";
    private static final String ENABLE = "traffic-profiling enable port %s";
    private static final String MODE = "traffic-profiling set port %s mode ";

    private Cli cli;

    public InterfaceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String ifcId = instanceIdentifier.firstKeyOf(Interface.class).getInterfaceId();
        parseIfcConfig(blockingRead(SHOW_COMMAND, cli, instanceIdentifier, readContext), configBuilder, ifcId);
    }

    @VisibleForTesting
    void parseIfcConfig(String output, ConfigBuilder configBuilder, String ifcId) {
        SaosQosIfAugBuilder saosQosIfAugBuilder = new SaosQosIfAugBuilder();

        configBuilder.setInterfaceId(ifcId);
        setEnabled(output, saosQosIfAugBuilder, ifcId);
        setMode(output, saosQosIfAugBuilder, ifcId);

        configBuilder.addAugmentation(SaosQosIfAug.class, saosQosIfAugBuilder.build());
    }

    private void setEnabled(String output, SaosQosIfAugBuilder saosQosIfAugBuilder, String ifcId) {
        Pattern enable = Pattern.compile(f(ENABLE, ifcId));
        ParsingUtils.parseField(output,
            enable::matcher,
            matcher -> true,
            saosQosIfAugBuilder::setEnabled);
    }

    private void setMode(String output, SaosQosIfAugBuilder saosQosIfAugBuilder, String ifcId) {
        Pattern mode = Pattern.compile(f(MODE, ifcId).concat("(?<mode>\\S+).*"));
        ParsingUtils.parseField(output,
            mode::matcher,
            matcher -> matcher.group("mode"),
            m -> saosQosIfAugBuilder.setMode(SaosQosIfExtensionConfig.Mode.forValue(Util.getModeValue(m))));
    }
}