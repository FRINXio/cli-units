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

package io.frinx.cli.iosxr.mpls.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.extension.rev171024.MplsRsvpSubscriptionConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.extension.rev171024.NiMplsRsvpIfSubscripAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.rsvp.global.rsvp.te._interface.attributes.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NiMplsRsvpIfSubscripAugWriter implements CliWriter<NiMplsRsvpIfSubscripAug> {

    private static final String BW_FORMAT = "0.###";

    private Cli cli;

    public NiMplsRsvpIfSubscripAugWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<NiMplsRsvpIfSubscripAug> id, @Nonnull
            NiMplsRsvpIfSubscripAug data, @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String name = id.firstKeyOf(Interface.class)
                .getInterfaceId()
                .getValue();
        blockingWriteAndRead(cli, id, data,
                "rsvp",
                f("interface %s", name),
                resolveBandwidth(data.getBandwidth()),
                "root");
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<NiMplsRsvpIfSubscripAug> id, @Nonnull
            NiMplsRsvpIfSubscripAug data, @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String name = id.firstKeyOf(Interface.class)
                .getInterfaceId()
                .getValue();
        blockingWriteAndRead(cli, id, data,
                "rsvp",
                f("interface %s", name),
                "no bandwidth",
                "root");
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<NiMplsRsvpIfSubscripAug> id, @Nonnull
            NiMplsRsvpIfSubscripAug dataBefore, @Nonnull NiMplsRsvpIfSubscripAug dataAfter, @Nonnull WriteContext
            writeContext) throws WriteFailedException {
        if (dataAfter.getBandwidth() == null) {
            deleteCurrentAttributes(id, dataBefore, writeContext);
        } else {
            writeCurrentAttributes(id, dataAfter, writeContext);
        }
    }

    private String resolveBandwidth(MplsRsvpSubscriptionConfig.Bandwidth bandwidth) {
        if (NiMplsRsvpIfSubscripAugReader.DEFAULT.equals(bandwidth.getString())) {
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
}
