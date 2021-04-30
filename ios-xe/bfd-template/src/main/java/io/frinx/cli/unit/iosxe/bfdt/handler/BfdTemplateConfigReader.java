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

package io.frinx.cli.unit.iosxe.bfdt.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.BfdTemplateConfig.Type;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.config.bfd.template.grouping.bfd.templates.BfdTemplate;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.config.bfd.template.grouping.bfd.templates.bfd.template.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.config.bfd.template.grouping.bfd.templates.bfd.template.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BfdTemplateConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    public static final String SH_BFD_TEMPLATE =
            BfdTemplateReader.SH_RUN_BFD_TEMPLATE + " (.+) %s";

    private final Cli cli;

    public BfdTemplateConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        final String bfdTemplateName = instanceIdentifier.firstKeyOf(BfdTemplate.class).getName();
        final String showCommand = f(SH_BFD_TEMPLATE, bfdTemplateName);
        configBuilder.setName(bfdTemplateName);
        parseBfdTemplateConfig(blockingRead(showCommand, cli, instanceIdentifier, readContext), configBuilder);
    }

    @VisibleForTesting
    static void parseBfdTemplateConfig(String output, ConfigBuilder configBuilder) {
        final Optional<String> bfdTemapleType = ParsingUtils.parseField(output, 0,
            parseBfdTemplate(configBuilder.getName())::matcher,
            matcher -> matcher.group("type"));
        bfdTemapleType.ifPresent(s -> configBuilder.setType(getBfdTemplateConfigType(s)));
    }

    private static Type getBfdTemplateConfigType(final String type) {
        for (final Type bfdType: Type.values()) {
            if (type.equalsIgnoreCase(bfdType.getName())) {
                return bfdType;
            }
        }
        return null;
    }

    private static Pattern parseBfdTemplate(final String name) {
        final String regex = String.format("bfd-template (?<type>\\S+) %s", name);
        return Pattern.compile(regex);
    }
}
