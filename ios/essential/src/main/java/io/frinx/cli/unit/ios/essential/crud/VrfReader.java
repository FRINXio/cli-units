/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.essential.crud;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.read.Initialized;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.InitCliListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ios.essential.rev170520.VrfsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ios.essential.rev170520.vrfs.Vrf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ios.essential.rev170520.vrfs.VrfBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ios.essential.rev170520.vrfs.VrfKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.regex.Pattern;

public class VrfReader implements InitCliListReader<Vrf, VrfKey, VrfBuilder> {

    private Cli cli;

    public VrfReader(Cli cli) {
        this.cli = cli;
    }

    private static final String SH_IP_VRF = "sh ip vrf";

    @Nonnull
    @Override
    public List<VrfKey> getAllIds(@Nonnull InstanceIdentifier<Vrf> instanceIdentifier,
                                  @Nonnull ReadContext readContext) throws ReadFailedException {
        return parseVrfIds(blockingRead(SH_IP_VRF, cli, instanceIdentifier));
    }

    private static final Pattern VRF_ID_LINE = Pattern.compile("(?<id>[^\\s]+).*");

    @VisibleForTesting
    static List<VrfKey> parseVrfIds(String output) {
        return ParsingUtils.parseFields(output, 1,
                VRF_ID_LINE::matcher,
                m -> m.group("id"),
                VrfKey::new);
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Vrf> list) {
        ((VrfsBuilder) builder).setVrf(list);
    }

    @Nonnull
    @Override
    public VrfBuilder getBuilder(@Nonnull InstanceIdentifier<Vrf> instanceIdentifier) {
        return new VrfBuilder();
    }

    private static final String SH_VRF_DETAIL_ID_TEMPLATE = "sh vrf detail %s";

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Vrf> instanceIdentifier,
                                      @Nonnull VrfBuilder vrfBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String id = instanceIdentifier.firstKeyOf(Vrf.class).getId();
        vrfBuilder.setId(id);

        parseVrf(blockingRead(String.format(SH_VRF_DETAIL_ID_TEMPLATE, id), cli, instanceIdentifier), vrfBuilder);
    }

    private static final Pattern DESCRIPTION_LINE = Pattern.compile("Description:\\s+(?<description>.+)");

    @VisibleForTesting
    static void parseVrf(String output, VrfBuilder vrfBuilder) {
        ParsingUtils.parseField(output, 0,
                DESCRIPTION_LINE::matcher,
                matcher -> matcher.group("description"),
                vrfBuilder::setDescription);
    }

    @Nonnull
    @Override
    public Initialized<? extends DataObject> init(@Nonnull InstanceIdentifier<Vrf> id, @Nonnull Vrf readValue, @Nonnull ReadContext ctx) {
        // Direct translation
        return Initialized.create(id, readValue);
    }
}
