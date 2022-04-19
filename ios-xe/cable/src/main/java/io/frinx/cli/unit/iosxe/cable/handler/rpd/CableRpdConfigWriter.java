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
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.rpds.Rpd;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.rpds.rpd.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CableRpdConfigWriter implements CliWriter<Config> {

    private static final String WRITE_TEMPLATE = "configure terminal\n"
            + "cable rpd {$rpd}\n"
            + "{% if ($data.description) %}description {$data.description}\n{% endif %}"
            + "{% if ($data.identifier) %}identifier {$data.identifier}\n{% endif %}"
            + "{% if ($data.rpd_type) %}type {$data.rpd_type}\n{% endif %}"
            + "{% if ($data.r_dti) %}r-dti {$data.r_dti}\n{% endif %}"
            + "{% if ($data.rpd_event_profile) %}rpd-event profile {$data.rpd_event_profile}\n{% endif %}"
            + "end";

    private static final String UPDATE_TEMPLATE = "configure terminal\n"
            + "cable rpd {$rpd}\n"
            + "{% if ($description) %}description {$description}\n"
            + "{% elseIf (!$description) %}{% if (!$description_compared) %}no description\n{% endif %}{% endif %}"
            + "{% if ($identifier) %}identifier {$identifier}\n"
            + "{% elseIf (!$identifier) %}{% if (!$identifier_compared) %}no identifier\n{% endif %}{% endif %}"
            + "{% if ($type) %}type {$type}\n"
            + "{% elseIf (!$type) %}{% if (!$type_compared) %}no type\n{% endif %}{% endif %}"
            + "{% if ($r_dti) %}r-dti {$r_dti}\n"
            + "{% elseIf (!$r_dti) %}{% if (!$r_dti_compared) %}no r-dti\n{% endif %}{% endif %}"
            + "{% if ($rpd_event_profile) %}rpd-event profile {$rpd_event_profile}\n"
            + "{% elseIf (!$rpd_event_profile) %}{% if (!$rep_compared) %}no rpd-event profile\n{% endif %}{% endif %}"
            + "{% if ($us_event_profile) %}rpd-55d1-us-event profile {$us_event_profile}\n"
            + "{% elseIf (!$us_event_profile) %}{% if (!$uep_compared) %}no rpd-55d1-us-event profile {$uep_before}\n"
            + "{% endif %}{% endif %}"
            + "end";

    private static final String DELETE_TEMPLATE = "configure terminal\n"
            + "no cable rpd {$rpd}\n"
            + "end";

    private final Cli cli;

    public CableRpdConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String rpdId = instanceIdentifier.firstKeyOf(Rpd.class).getId();
        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(WRITE_TEMPLATE,
                        "rpd", rpdId,
                        "data", config));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String rpdId = instanceIdentifier.firstKeyOf(Rpd.class).getId();
        blockingWriteAndRead(cli, instanceIdentifier, dataAfter,
                fT(UPDATE_TEMPLATE, "rpd", rpdId,
                        "uep_before", dataBefore.getRpd55d1UsEventProfile(),
                        "description", (dataAfter.getDescription() != null
                                && !dataAfter.getDescription().equals(dataBefore.getDescription()))
                                ? dataAfter.getDescription() : null,
                        "identifier", (dataAfter.getIdentifier() != null
                                && !dataAfter.getIdentifier().equals(dataBefore.getIdentifier()))
                                ? dataAfter.getIdentifier() : null,
                        "type", (dataAfter.getRpdType() != null
                                && !dataAfter.getRpdType().equals(dataBefore.getRpdType()))
                                ? dataAfter.getRpdType() : null,
                        "r_dti", (dataAfter.getRDti() != null
                                && !dataAfter.getRDti().equals(dataBefore.getRDti()))
                                ? dataAfter.getRDti() : null,
                        "rpd_event_profile", (dataAfter.getRpdEventProfile() != null
                                && !dataAfter.getRpdEventProfile().equals(dataBefore.getRpdEventProfile()))
                                ? dataAfter.getRpdEventProfile() : null,
                        "us_event_profile", (dataAfter.getRpd55d1UsEventProfile() != null
                                && !dataAfter.getRpd55d1UsEventProfile().equals(dataBefore.getRpd55d1UsEventProfile()))
                                ? dataAfter.getRpd55d1UsEventProfile() : null,
                        "description_compared", (dataAfter.getDescription() != null
                                && dataBefore.getDescription() != null)
                                ? dataAfter.getDescription().equals(dataBefore.getDescription()) : null,
                        "identifier_compared", (dataAfter.getIdentifier() != null && dataBefore.getIdentifier() != null)
                                ? dataAfter.getIdentifier().equals(dataBefore.getIdentifier()) : null,
                        "type_compared", (dataAfter.getRpdType() != null && dataBefore.getRpdType() != null)
                                ? dataAfter.getRpdType().equals(dataBefore.getRpdType()) : null,
                        "r_dti_compared", (dataAfter.getRDti() != null && dataBefore.getRDti() != null)
                                ? dataAfter.getRDti().equals(dataBefore.getRDti()) : null,
                        "rep_compared", (dataAfter.getRpdEventProfile() != null
                                && dataBefore.getRpdEventProfile() != null)
                                ? dataAfter.getRpdEventProfile().equals(dataBefore.getRpdEventProfile()) : null,
                        "uep_compared", (dataAfter.getRpd55d1UsEventProfile() != null
                                && dataBefore.getRpd55d1UsEventProfile() != null)
                                ? dataAfter.getRpd55d1UsEventProfile().equals(dataBefore.getRpd55d1UsEventProfile())
                                : null
                ));
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String rpdId = instanceIdentifier.firstKeyOf(Rpd.class).getId();
        blockingWriteAndRead(cli, instanceIdentifier, config, fT(DELETE_TEMPLATE,
                "rpd", rpdId));
    }
}
