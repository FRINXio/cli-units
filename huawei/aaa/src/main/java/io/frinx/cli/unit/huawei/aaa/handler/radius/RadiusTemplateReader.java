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

package io.frinx.cli.unit.huawei.aaa.handler.radius;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.radius.extension.radius.Template;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.radius.extension.radius.TemplateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.radius.extension.radius.TemplateKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class RadiusTemplateReader implements CliConfigListReader<Template, TemplateKey, TemplateBuilder> {

    public static final String TEMPLATE_LIST =
            "display current-configuration | include ^radius-server";
    private static final Pattern TEMPLATE_LINE = Pattern.compile("radius-server template (?<name>\\S+)");
    private final Cli cli;

    public RadiusTemplateReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<TemplateKey> getAllIds(@Nonnull InstanceIdentifier<Template> instanceIdentifier,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String output = blockingRead(TEMPLATE_LIST, cli, instanceIdentifier, readContext);
        return getAllIds(output);
    }

    @VisibleForTesting
    static List<TemplateKey> getAllIds(String output) {
        return ParsingUtils.parseFields(output, 0,
            TEMPLATE_LINE::matcher,
            matcher -> matcher.group("name"),
            TemplateKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Template> instanceIdentifier,
                                      @Nonnull TemplateBuilder builder,
                                      @Nonnull ReadContext readContext) {
        builder.setKey(instanceIdentifier.firstKeyOf(Template.class));
    }
}


