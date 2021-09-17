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
import io.frinx.cli.unit.utils.CliConfigReader;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.radius.extension.radius.Template;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.radius.extension.radius.template.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.radius.extension.radius.template.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.radius.extension.radius.template.config.AuthenticationData;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.radius.extension.radius.template.config.AuthenticationDataBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.types.rev181121.CryptPasswordType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class RadiusTemplateConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String DISPLAY_RADIUS_CONFIG = "display current-configuration configuration radius-server";
    private static final Pattern SECRET_KEY = Pattern.compile("[\\s\\S]+shared-key cipher (?<name>.*)(\\s?.*)+");
    private static final Pattern AUTH_NAME =
            Pattern.compile("[\\s\\S]+authentication (?<address>\\S+) \\d+ (?<instance>\\S+) (?<name>(\\S+))(\\s?.*)+");
    private static final Pattern RETRANSMIT_ATTEMPTS =
            Pattern.compile("[\\s\\S]+radius-server retransmit (?<name>\\d+)(\\s?.*)+");

    private final Cli cli;

    public RadiusTemplateConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String templateName = instanceIdentifier.firstKeyOf(Template.class).getName();
        parseConfigAttributes(blockingRead(DISPLAY_RADIUS_CONFIG, cli, instanceIdentifier,
                readContext), configBuilder, templateName);
    }

    @VisibleForTesting
    static void parseConfigAttributes(String output, ConfigBuilder configBuilder, String name) {
        parsingDomainFields(output, name, SECRET_KEY,
            value -> configBuilder.setSecretKeyHashed(new CryptPasswordType(value)));
        parsingDomainFields(output, name, RETRANSMIT_ATTEMPTS,
            value -> configBuilder.setRetransmitAttempts(Short.valueOf(value)));

        List<AuthenticationData> authData = Pattern.compile("\\n\\S").splitAsStream(output)
                .filter(value -> value.contains(" " + name + "\r\n"))
                .flatMap(Pattern.compile("\n")::splitAsStream)
                .map(AUTH_NAME::matcher)
                .filter(Matcher::matches)
                .map(m -> {
                    Ipv4Address address = new Ipv4Address(m.group("address"));
                    String vrfName = null;
                    if (m.group("instance").equals("vpn-instance")) {
                        vrfName = m.group("name");
                    }
                    return new AuthenticationDataBuilder()
                            .setSourceAddress(new IpAddress(address))
                            .setVrfName(vrfName)
                            .build();
                })
                .collect(Collectors.toList());

        if (!authData.isEmpty()) {
            configBuilder.setAuthenticationData(authData);
        }
    }

    private static void parsingDomainFields(String output, String name, Pattern pattern, Consumer<String> consumer) {
        Pattern.compile("\\n\\S").splitAsStream(output)
                .filter(value -> value.contains(" " + name + "\r\n"))
                .map(pattern::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group("name"))
                .map(String::trim)
                .findFirst()
                .ifPresent(consumer);
    }
}
