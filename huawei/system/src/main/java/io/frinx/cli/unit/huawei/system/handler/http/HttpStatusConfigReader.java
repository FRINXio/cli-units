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

package io.frinx.cli.unit.huawei.system.handler.http;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.http.status.extension.rev211028.huawei.http.server.http.server.status.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.http.status.extension.rev211028.huawei.http.server.http.server.status.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class HttpStatusConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SHOW_HTTP_STATUS = "display http server";
    private static final Pattern PARSE_HTTP_STATUS = Pattern.compile("^HTTP server status *: Enabled.*");
    private static final Pattern PARSE_HTTPS_STATUS = Pattern.compile("^HTTPS server status *: Enabled.*");

    private final Cli cli;

    public HttpStatusConfigReader(Cli cli) {
        this.cli = cli;
    }

    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        parseConfig(blockingRead(SHOW_HTTP_STATUS, cli, instanceIdentifier, readContext), configBuilder);
    }

    @VisibleForTesting
    static void parseConfig(String output, ConfigBuilder configBuilder) {
        configBuilder.setHttpServerStatusEnabled(false);
        configBuilder.setHttpSecureServerStatusEnabled(false);
        ParsingUtils.parseField(output, 0,
            PARSE_HTTP_STATUS::matcher,
            matcher -> matcher.find(),
            value -> configBuilder.setHttpServerStatusEnabled(true));
        ParsingUtils.parseField(output, 0,
            PARSE_HTTPS_STATUS::matcher,
            matcher -> matcher.find(),
            value -> configBuilder.setHttpSecureServerStatusEnabled(true));
    }

}
