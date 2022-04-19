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
package io.frinx.cli.unit.iosxe.cable.handler.rpd;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102._if.rpd.ds.top.IfRpdDs;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102._if.rpd.ds.top._if.rpd.ds.DownstreamPorts;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102._if.rpd.us.top.IfRpdUs;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102._if.rpd.us.top._if.rpd.us.UpstreamPorts;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.rpds.Rpd;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.rpds.rpd.CoreInterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.rpds.rpd.core._interface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CableRpdCoreInterfaceConfigWriter implements CliWriter<CoreInterface> {

    private static final String WRITE_TEMPLATE = "configure terminal\n"
            + "cable rpd {$rpd}\n"
            + "core-interface {$config.name}\n"
            + "{% if ($principal == TRUE) %}principal\n{% endif %}"
            + "{% if ($nw_delay) %}network-delay {$nw_delay}\n{% endif %}"
            + "{% loop in $rpdDs as $ds%}"
            + "rpd-ds {$ds.id} {$ds.cable_controller} profile {$ds.profile}\n"
            + "{% endloop %}"
            + "{% loop in $rpdUs as $us%}"
            + "rpd-us {$us.id} {$us.cable_controller} profile {$us.profile}\n"
            + "{% endloop %}"
            + "end";

    private static final String UPDATE_TEMPLATE = "configure terminal\n"
            + "cable rpd {$rpd}\n"
            + "core-interface {$config.name}\n"
            + "{% if ($principal == TRUE) %}principal\n"
            + "{% else %}no principal\n"
            + "{% endif %}"
            + "{% if ($nw_delay) %}no network-delay\nnetwork-delay {$nw_delay}\n{% endif %}"
            + "{% loop in $rpdDsBefore as $ds%}"
            + "no rpd-ds {$ds.id} {$ds.cable_controller}\n"
            + "{% onEmpty %}"
            + "{% endloop %}"
            + "{% loop in $rpdDsAfter as $ds%}"
            + "rpd-ds {$ds.id} {$ds.cable_controller} profile {$ds.profile}\n"
            + "{% onEmpty %}"
            + "{% endloop %}"
            + "{% loop in $rpdUsBefore as $us%}"
            + "no rpd-us {$us.id} {$us.cable_controller}\n"
            + "{% onEmpty %}"
            + "{% endloop %}"
            + "{% loop in $rpdUsAfter as $us%}"
            + "rpd-us {$us.id} {$us.cable_controller} profile {$us.profile}\n"
            + "{% onEmpty %}"
            + "{% endloop %}"
            + "end";

    private static final String DELETE_TEMPLATE = "configure terminal\n"
            + "cable rpd {$rpd}\n"
            + "no core-interface {$config.name}\n"
            + "end";

    private static final Pattern NODE_NAME_OR_ID =
            Pattern.compile("(?<name>[Uu]pstream|[Dd]ownstream|[Ii]ntegrated)-[Cc]able *(?<id>.+)");

    private final Cli cli;

    public CableRpdCoreInterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<CoreInterface> instanceIdentifier,
                                       @Nonnull CoreInterface coreInterface,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String rpdId = instanceIdentifier.firstKeyOf(Rpd.class).getId();
        IfRpdDs rpdDs = coreInterface.getIfRpdDs();
        IfRpdUs rpdUs = coreInterface.getIfRpdUs();
        Config config = coreInterface.getConfig();
        List<Map<String, String>> dsTransformed = (rpdDs.getDownstreamPorts() != null)
                ? getRpdDsMap(rpdDs.getDownstreamPorts()) : null;
        List<Map<String, String>> usTransformed = (rpdUs.getUpstreamPorts() != null)
                ? getRpdUsMap(rpdUs.getUpstreamPorts()) : null;
        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(WRITE_TEMPLATE,
                        "rpd", rpdId,
                        "config", config,
                        "principal", config.isPrincipal(),
                        "nw_delay", config.getNetworkDelay(),
                        "rpdDs", dsTransformed,
                        "rpdUs", usTransformed
                ));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<CoreInterface> instanceIdentifier,
                                        @Nonnull CoreInterface dataBefore,
                                        @Nonnull CoreInterface dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String rpdId = instanceIdentifier.firstKeyOf(Rpd.class).getId();
        IfRpdDs rpdDs = dataAfter.getIfRpdDs();
        IfRpdUs rpdUs = dataAfter.getIfRpdUs();
        IfRpdDs rpdDsBefore = dataBefore.getIfRpdDs();
        IfRpdUs rpdUsBefore = dataBefore.getIfRpdUs();

        List<Map<String, String>> dsTransformed = (rpdDs != null && rpdDs.getDownstreamPorts() != null)
                ? getRpdDsMap(rpdDs.getDownstreamPorts()) : null;
        List<Map<String, String>> usTransformed = (rpdUs != null && rpdUs.getUpstreamPorts() != null)
                ? getRpdUsMap(rpdUs.getUpstreamPorts()) : null;

        List<Map<String, String>> dsBeforeTransformed = (rpdDsBefore != null
                && rpdDsBefore.getDownstreamPorts() != null)
                ? getRpdDsMap(rpdDsBefore.getDownstreamPorts()) : null;
        List<Map<String, String>> usBeforeTransformed = (rpdUsBefore != null && rpdUsBefore.getUpstreamPorts() != null)
                ? getRpdUsMap(rpdUsBefore.getUpstreamPorts()) : null;

        blockingWriteAndRead(cli, instanceIdentifier, dataAfter,
                fT(UPDATE_TEMPLATE, "rpd", rpdId,
                        "config", dataAfter.getConfig(),
                        "principal", dataAfter.getConfig().isPrincipal(),
                        "nw_delay", (!dataBefore.getConfig().getNetworkDelay().equals(
                                dataAfter.getConfig().getNetworkDelay()))
                                ? dataAfter.getConfig().getNetworkDelay() : null,
                        "rpdDsBefore", dsBeforeTransformed,
                        "rpdUsBefore", usBeforeTransformed,
                        "rpdDsAfter", dsTransformed,
                        "rpdUsAfter", usTransformed
                ));
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<CoreInterface> instanceIdentifier,
                                        @Nonnull CoreInterface coreInterface,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String rpdId = instanceIdentifier.firstKeyOf(Rpd.class).getId();
        Config config = coreInterface.getConfig();
        blockingWriteAndRead(cli, instanceIdentifier, config, fT(DELETE_TEMPLATE,
                "rpd", rpdId,
                "config", config,
                "principal", config.isPrincipal(),
                "nw_delay", config.getNetworkDelay()
        ));
    }

    private static String getNodeName(String id) {
        return ParsingUtils.parseField(id, 0,
            NODE_NAME_OR_ID::matcher,
            matcher -> matcher.group("name")).orElse("");
    }

    private static String getNodeNumber(String id) {
        return ParsingUtils.parseField(id, 0,
            NODE_NAME_OR_ID::matcher,
            matcher -> matcher.group("id")).orElse("");
    }

    private static List<Map<String, String>> getRpdDsMap(List<DownstreamPorts> rpdDs) {
        List<Map<String, String>> listOfDs = new ArrayList<>();

        rpdDs.forEach(rdp -> listOfDs.add(Map.of(
            "id", rdp.getId(),
            "cable_controller", getNodeName(rdp.getConfig().getCableController()) + "-Cable "
                    + getNodeNumber(rdp.getConfig().getCableController()),
            "profile", rdp.getConfig().getProfile())));

        return listOfDs;
    }

    private static List<Map<String, String>> getRpdUsMap(List<UpstreamPorts> rpdUs) {
        List<Map<String, String>> listOfUs = new ArrayList<>();

        rpdUs.forEach(rdp -> listOfUs.add(Map.of(
            "id", rdp.getId(),
            "cable_controller", getNodeName(rdp.getConfig().getCableController()) + "-Cable "
                    + getNodeNumber(rdp.getConfig().getCableController()),
            "profile", rdp.getConfig().getProfile())));

        return listOfUs;
    }
}
