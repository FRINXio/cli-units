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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsi;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft.extension.cfts.Cft;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft.extension.cfts.CftBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft.extension.cfts.CftKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSIConfigCftReader implements CliConfigListReader<Cft, CftKey, CftBuilder> {

    private static final String SH_CFT = "configuration search running-config string \"l2-cft create profile\"";
    private static final Pattern CFT_PROFILE = Pattern.compile("l2-cft create profile (?<profile>\\S+).*");

    private Cli cli;

    public L2VSIConfigCftReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<CftKey> getAllIds(@Nonnull InstanceIdentifier<Cft> instanceIdentifier,
                                  @Nonnull ReadContext readContext) throws ReadFailedException {
        return getAllIds(blockingRead(SH_CFT, cli, instanceIdentifier, readContext));
    }

    @VisibleForTesting
    static List<CftKey> getAllIds(String output) {
        return ParsingUtils.parseFields(output, 0,
            CFT_PROFILE::matcher,
            matcher -> matcher.group("profile"),
            CftKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Cft> instanceIdentifier,
                                      @Nonnull CftBuilder cftBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        cftBuilder.setCftName(instanceIdentifier.firstKeyOf(Cft.class).getCftName());
    }
}