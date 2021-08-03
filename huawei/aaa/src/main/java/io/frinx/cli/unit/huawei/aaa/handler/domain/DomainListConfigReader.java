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

package io.frinx.cli.unit.huawei.aaa.handler.domain;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.domain.list.Domain;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.domain.list.domain.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.domain.list.domain.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class DomainListConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String DISPLAY_DOMAIN_MODE =
            "display current-configuration | include ^ domain |^  authentication|^  accounting|^  radius";
    private static final Pattern AUTH_NAME = Pattern.compile("[\\s\\S]+authentication-scheme (?<name>(\\S+))(\\s?.*)+");
    private static final Pattern ACC_NAME = Pattern.compile("[\\s\\S]+accounting-scheme (?<name>\\S+)(\\s?.*)+");
    private static final Pattern RADIUS_NAME = Pattern.compile("[\\s\\S]+radius-server (?<name>\\S+)(\\s?.*)+");

    private final Cli cli;

    public DomainListConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {

        String schemasName = instanceIdentifier.firstKeyOf(Domain.class).getName();
        parseConfigAttributes(blockingRead(DISPLAY_DOMAIN_MODE, cli, instanceIdentifier,
                readContext), configBuilder, schemasName);
    }

    @VisibleForTesting
    static void parseConfigAttributes(String output, ConfigBuilder configBuilder, String name) {
        parsingDomainFields(output, name, AUTH_NAME, configBuilder::setAuthenticationScheme);
        parsingDomainFields(output, name, ACC_NAME, configBuilder::setAccountingScheme);
        parsingDomainFields(output, name, RADIUS_NAME, configBuilder::setRadiusServer);
    }

    private static void parsingDomainFields(String output, String name, Pattern pattern, Consumer<String> consumer) {
        Pattern.compile("\\n\\s\\S").splitAsStream(output)
                .filter(value -> value.contains(" " + name + "\n"))
                .map(pattern::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group("name"))
                .map(String::trim)
                .findFirst()
                .ifPresent(consumer);
    }
}
