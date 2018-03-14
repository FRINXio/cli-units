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

package io.frinx.cli.iosxr.routing.policy.handler.prefix;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.top.prefix.sets.prefix.set.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PrefixSetConfigWriter implements CliWriter<Config> {

    private static final String TEMPLATE = "{.if ($delete) }no {/if}prefix-set {$config.name}\n" +
            "{.if ($delete) }{.else}end-set{/if}";

    private final Cli cli;

    public PrefixSetConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, id, config,
                fT(TEMPLATE, "config", config));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore, @Nonnull Config dataAfter, @Nonnull WriteContext writeContext) throws WriteFailedException {
        // NOOP, one line command with only one parameter does not need update
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, id, config,
                fT(TEMPLATE,
                        "config", config,
                        "delete", true));
    }
}
