/*
 * Copyright © 2022 Frinx and others.
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

package io.frinx.cli.unit.ios.routing.policy.handlers.tags;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.unit.utils.CliConfigReader;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.cisco.tag.top.tags.Tag;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.cisco.tag.top.tags.tag.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.cisco.tag.top.tags.tag.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TagConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        final long tagSetId = instanceIdentifier.firstKeyOf(Tag.class).getName();
        configBuilder.setName(tagSetId);
    }
}