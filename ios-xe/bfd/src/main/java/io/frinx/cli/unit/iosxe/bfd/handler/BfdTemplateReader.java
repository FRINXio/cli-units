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

package io.frinx.cli.unit.iosxe.bfd.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.config.bfd.template.grouping.bfd.templates.BfdTemplate;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.config.bfd.template.grouping.bfd.templates.BfdTemplateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.config.bfd.template.grouping.bfd.templates.BfdTemplateKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BfdTemplateReader implements CliConfigListReader<BfdTemplate, BfdTemplateKey, BfdTemplateBuilder> {

    public static final String SH_RUN_BFD_TEMPLATE = "show running-config | section bfd-template";
    private static final Pattern INTERFACE_CHUNK = Pattern.compile("bfd-template (?<type>\\S+) (?<name>\\S+)");

    private final Cli cli;

    public BfdTemplateReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<BfdTemplateKey> getAllIds(@Nonnull InstanceIdentifier<BfdTemplate> instanceIdentifier,
                                        @Nonnull ReadContext readContext) throws ReadFailedException {
        return parseBfdTemplateNames(blockingRead(SH_RUN_BFD_TEMPLATE, cli, instanceIdentifier, readContext));
    }

    @VisibleForTesting
    static List<BfdTemplateKey> parseBfdTemplateNames(@Nonnull String bfdTemplatesConfiguration) {
        return ParsingUtils.parseFields(bfdTemplatesConfiguration, 0,
            INTERFACE_CHUNK::matcher,
            matcher -> matcher.group("name"),
            BfdTemplateKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<BfdTemplate> instanceIdentifier,
                                      @Nonnull BfdTemplateBuilder bfdTempAugBuilder, @Nonnull ReadContext readContext)
            throws ReadFailedException {
        bfdTempAugBuilder.setName(instanceIdentifier.firstKeyOf(BfdTemplate.class).getName());
    }
}
