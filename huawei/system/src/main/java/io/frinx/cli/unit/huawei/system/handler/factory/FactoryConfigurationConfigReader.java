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

package io.frinx.cli.unit.huawei.system.handler.factory;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.factory.config.extension.rev211108.huawei.factory.configuration.factory.configuration.status.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.factory.config.extension.rev211108.huawei.factory.configuration.factory.configuration.status.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class FactoryConfigurationConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SHOW_FACTORY_CONFIG_STATUS = "display current-configuration configuration"
        + " | include ^ factory";
    private static final Pattern PARSE_FACTORY_CONFIG_STATUS = Pattern.compile("^factory-configuration *prohibit.*");

    private final Cli cli;

    public FactoryConfigurationConfigReader(Cli cli) {
        this.cli = cli;
    }

    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        parseConfig(blockingRead(SHOW_FACTORY_CONFIG_STATUS, cli, instanceIdentifier, readContext) ,configBuilder);
    }

    @VisibleForTesting
    static void parseConfig(String output, ConfigBuilder configBuilder) {
        configBuilder.setFactoryConfigurationProhibited(false);
        ParsingUtils.parseField(output, 0,
            PARSE_FACTORY_CONFIG_STATUS::matcher,
            matcher -> matcher.find(),
            value -> configBuilder.setFactoryConfigurationProhibited(true));
    }

}
