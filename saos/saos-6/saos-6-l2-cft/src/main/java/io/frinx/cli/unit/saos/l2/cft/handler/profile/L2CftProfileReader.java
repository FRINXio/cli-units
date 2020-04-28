/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos.l2.cft.handler.profile;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.l2.cft.top.l2.cft.profiles.Profile;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.l2.cft.top.l2.cft.profiles.ProfileBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.l2.cft.top.l2.cft.profiles.ProfileKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2CftProfileReader implements CliConfigListReader<Profile, ProfileKey, ProfileBuilder> {

    private static final String SHOW_COMMAND = "configuration search string \"l2-cft create profile\"";
    private static final Pattern CFT_PROFILE = Pattern.compile("l2-cft create profile (?<profile>\\S+).*");

    private Cli cli;

    public L2CftProfileReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<ProfileKey> getAllIds(@Nonnull InstanceIdentifier<Profile> instanceIdentifier,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        return getAllIds(blockingRead(SHOW_COMMAND, cli, instanceIdentifier, readContext));
    }

    @VisibleForTesting
    static List<ProfileKey> getAllIds(String output) {
        return ParsingUtils.parseFields(output, 0,
            CFT_PROFILE::matcher,
            matcher -> matcher.group("profile"),
            ProfileKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Profile> instanceIdentifier,
                                      @Nonnull ProfileBuilder profileBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        profileBuilder.setName(instanceIdentifier.firstKeyOf(Profile.class).getName());
    }
}
