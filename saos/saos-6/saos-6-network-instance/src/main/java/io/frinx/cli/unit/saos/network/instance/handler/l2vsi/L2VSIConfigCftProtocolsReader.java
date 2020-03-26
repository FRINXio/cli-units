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
import io.frinx.cli.unit.saos.network.instance.Util;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft.extension.cfts.Cft;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft.extension.cfts.cft.config.ctrl.protocols.CtrlProtocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft.extension.cfts.cft.config.ctrl.protocols.CtrlProtocol.Disposition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft.extension.cfts.cft.config.ctrl.protocols.CtrlProtocol.Name;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft.extension.cfts.cft.config.ctrl.protocols.CtrlProtocolBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft.extension.cfts.cft.config.ctrl.protocols.CtrlProtocolKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSIConfigCftProtocolsReader implements
        CliConfigListReader<CtrlProtocol, CtrlProtocolKey, CtrlProtocolBuilder> {

    private static final String SH_CFT_PROTOCOLS =
            "configuration search string \"l2-cft protocol add profile %s\"";

    private Cli cli;

    public L2VSIConfigCftProtocolsReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<CtrlProtocolKey> getAllIds(@Nonnull InstanceIdentifier<CtrlProtocol> id,
                                           @Nonnull ReadContext readContext) throws ReadFailedException {
        String cftName = id.firstKeyOf(Cft.class).getCftName();
        return getAllIds(blockingRead(f(SH_CFT_PROTOCOLS, cftName), cli, id, readContext), cftName);
    }

    @VisibleForTesting
    static List<CtrlProtocolKey> getAllIds(String output, String cftName) {
        Pattern protocol = Pattern.compile(".*" + "profile " + cftName + " ctrl-protocol (?<name>\\S+).*");
        return ParsingUtils.parseFields(output, 0,
            protocol::matcher,
            matcher -> matcher.group("name"),
            name -> new CtrlProtocolKey(Name.forValue(Util.getCtrlProtocolValue(name))));
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<CtrlProtocol> instanceIdentifier,
                                      @Nonnull CtrlProtocolBuilder ctrlProtocolBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String cftName = instanceIdentifier.firstKeyOf(Cft.class).getCftName();
        Name name = instanceIdentifier.firstKeyOf(CtrlProtocol.class).getName();
        setProtocol(blockingRead(f(SH_CFT_PROTOCOLS, cftName), cli, instanceIdentifier, readContext),
                name, cftName, ctrlProtocolBuilder);
    }

    @VisibleForTesting
    static void setProtocol(String output, Name name, String cftName, CtrlProtocolBuilder builder) {
        Pattern mode = Pattern.compile(".*" + "profile " + cftName + " ctrl-protocol " + name.getName()
                + " untagged-disposition (?<disposition>\\S+)");
        builder.setName(name);
        ParsingUtils.parseField(output,
            mode::matcher,
            matcher -> matcher.group("disposition"),
            disposition -> builder.setDisposition(Disposition
                    .forValue(Util.getDispositionValue(disposition))));
    }
}
