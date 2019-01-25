/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.iosxr.mpls.handler;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.mpls.MplsWriter;
import io.frinx.cli.io.Cli;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.extension.rev171024.MplsRsvpSubscriptionConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.extension.rev171024.NiMplsRsvpIfSubscripAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.Percentage;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.mpls.rsvp.subscription.subscription.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.rsvp.global.rsvp.te._interface.attributes.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NiMplsRsvpIfSubscripConfigWriter implements MplsWriter<Config> {

    private static final String BW_FORMAT = "0.###";
    private final Cli cli;

    public NiMplsRsvpIfSubscripConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributesForType(InstanceIdentifier<Config> id,
                                              Config config,
                                              WriteContext writeContext) throws WriteFailedException {
        Percentage subscription = config.getSubscription();
        NiMplsRsvpIfSubscripAug aug = config.getAugmentation(NiMplsRsvpIfSubscripAug.class);
        Preconditions.checkArgument(subscription == null || aug == null,
                "Only subscription or bandwidth must be defined, not both.");
        final String ifaceName = id.firstKeyOf(Interface.class)
                .getInterfaceId()
                .getValue();
        if (subscription != null) {
            blockingWriteAndRead(cli, id, config,
                    "rsvp",
                    f("interface %s", ifaceName),
                    f("bandwidth percentage %s", subscription.getValue()),
                    "root");
            return;
        }

        blockingWriteAndRead(cli, id, config,
                "rsvp",
                f("interface %s", ifaceName),
                resolveBandwidth(aug.getBandwidth()),
                "root");
    }

    private String resolveBandwidth(MplsRsvpSubscriptionConfig.Bandwidth bandwidth) {
        if (NiMplsRsvpIfSubscripConfigReader.DEFAULT.equals(bandwidth.getString())) {
            return "bandwidth";
        } else if (bandwidth.getUint32() != null) {
            NumberFormat formatter = new DecimalFormat(BW_FORMAT);
            return f("bandwidth %s", formatter.format(kbps(bandwidth.getUint32())));
        }
        return "";
    }

    private static double kbps(Long bps) {
        return bps.doubleValue() / 1000;
    }

    @Override
    public void updateCurrentAttributesForType(InstanceIdentifier<Config> id,
                                               Config dataBefore,
                                               Config dataAfter,
                                               WriteContext writeContext) throws WriteFailedException {
        writeCurrentAttributesForType(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributesForType(InstanceIdentifier<Config> id,
                                               Config config,
                                               WriteContext writeContext) throws WriteFailedException {
        final String name = id.firstKeyOf(Interface.class)
                .getInterfaceId()
                .getValue();
        blockingWriteAndRead(cli, id, config,
                "rsvp",
                f("interface %s", name),
                "no bandwidth",
                "root");
    }
}
