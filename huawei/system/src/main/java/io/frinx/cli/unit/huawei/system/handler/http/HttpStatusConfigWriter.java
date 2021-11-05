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

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.fd.honeycomb.translate.write.WriteFailedException.DeleteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.http.status.extension.rev211028.huawei.http.server.http.server.status.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class HttpStatusConfigWriter implements CliWriter<Config> {

    private static final String HTTP_SERVER_ENABLE_DISABLE = "system-view\n"
            + "{% if($enableHttp == TRUE) %}"
            + "http server enable\n"
            + "{% else %}"
            + "undo http server enable\n"
            + "Y\n"
            + "{% endif %}"
            + "{% if($enableHttps == TRUE) %}"
            + "http secure-server enable\n"
            + "{% else %}"
            + "undo http secure-server enable\n"
            + "Y\n"
            + "{% endif %}"
            + "return";

    private final Cli cli;

    public HttpStatusConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        boolean enableHttp = config.isHttpServerStatusEnabled();
        boolean enableHttps = config.isHttpSecureServerStatusEnabled();
        blockingWriteAndRead(cli, id, config, fT(HTTP_SERVER_ENABLE_DISABLE, "enableHttp", enableHttp,
                "enableHttps", enableHttps));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        boolean enableHttp = dataAfter.isHttpServerStatusEnabled();
        boolean enableHttps = dataAfter.isHttpSecureServerStatusEnabled();
        blockingWriteAndRead(cli, id, dataAfter, fT(HTTP_SERVER_ENABLE_DISABLE, "enableHttp", enableHttp,
                "enableHttps", enableHttps));
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        throw new DeleteFailedException(id,
                new IllegalStateException("Deleting HTTP server is not permitted."));
    }

}
