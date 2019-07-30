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

package io.frinx.cli.unit.iosxr.ifc.handler.subifc.cfm;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.ifc.Util;
import io.frinx.cli.unit.utils.CliWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.ethernet.cfm._interface.cfm.domains.Domain;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CfmDomainWriter implements CliWriter<Domain> {
    private Cli cli;

    private static final String CREATE_TEMPLATE = "interface {$ifc}\n"
        + "ethernet cfm\n"
        + "mep domain {$domain.domain_name} service {$mep.ma_name} mep-id {$mep.mep_id}\n"
        + "{% if $mep.cos %}"
        + "cos {$mep.cos}\n"
        + "{% else %}"
        + "no cos\n"
        + "{% endif %}"
        + "root";

    private static final String DELETE_TEMPLATE = "interface {$ifc}\n"
        + "ethernet cfm\n"
        + "no mep domain {$domain.domain_name}\n"
        + "root";

    public CfmDomainWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(
        InstanceIdentifier<Domain> id,
        Domain data,
        WriteContext writeContext) throws WriteFailedException {

        String ifcName = Util.getSubinterfaceName(id);
        if (isActive(data)) {
            blockingWriteAndRead(cli, id, data,
                    fT(CREATE_TEMPLATE,
                        "ifc", ifcName,
                        "domain", data.getConfig(),
                        "mep", data.getMep().getConfig()));
        }
    }

    private boolean isActive(Domain data) {
        return data.getConfig() != null && data.getMep() != null && data.getMep().getConfig() != null
            && data.getConfig().getDomainName() != null
            && data.getMep().getConfig().getMaName() != null
            && data.getMep().getConfig().getMepId() != null;
    }

    @Override
    public void updateCurrentAttributes(
        InstanceIdentifier<Domain> id,
        Domain dataBefore,
        Domain dataAfter,
        WriteContext writeContext) throws WriteFailedException {

        if (isActive(dataAfter)) {
            writeCurrentAttributes(id, dataAfter, writeContext);
        } else {
            deleteCurrentAttributes(id, dataBefore, writeContext);
        }
    }

    @Override
    public void deleteCurrentAttributes(
        InstanceIdentifier<Domain> id,
        Domain data,
        WriteContext writeContext) throws WriteFailedException {

        String ifcName = Util.getSubinterfaceName(id);
        if (isActive(data)) {
            blockingDeleteAndRead(cli, id,
                fT(DELETE_TEMPLATE,
                    "ifc", ifcName,
                    "domain", data.getConfig()));
        }
    }
}
