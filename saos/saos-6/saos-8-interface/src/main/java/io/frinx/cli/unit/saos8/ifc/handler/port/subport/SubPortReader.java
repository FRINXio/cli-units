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

package io.frinx.cli.unit.saos8.ifc.handler.port.subport;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos8.ifc.handler.port.PortReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubPortReader implements CliConfigListReader<Subinterface, SubinterfaceKey, SubinterfaceBuilder> {

    public static final String SHOW_COMMAND = "configuration search string \"sub-port create sub-port\"";

    private Cli cli;

    public SubPortReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<SubinterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Subinterface> instanceIdentifier,
                                           @Nonnull ReadContext readContext) throws ReadFailedException {
        if (isPort(instanceIdentifier, readContext)) {
            String parentPort = instanceIdentifier.firstKeyOf(Interface.class).getName();
            String output = blockingRead(f(SHOW_COMMAND, parentPort), cli, instanceIdentifier, readContext);
            return getAllIds(output, parentPort);
        }
        return Collections.emptyList();
    }

    @VisibleForTesting
    static List<SubinterfaceKey> getAllIds(String output, String parentPort) {
        Pattern allIds = Pattern.compile(".* parent-port " + parentPort + " classifier-precedence (?<id>\\d+)( .+|$)");
        return ParsingUtils.parseFields(output, 0,
            allIds::matcher,
            matcher -> matcher.group("id"),
            id -> new SubinterfaceKey(Long.valueOf(id)));
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Subinterface> instanceIdentifier,
                                      @Nonnull SubinterfaceBuilder subinterfaceBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        if (isPort(instanceIdentifier, readContext)) {
            subinterfaceBuilder.setIndex(instanceIdentifier.firstKeyOf(Subinterface.class).getIndex());
        }
    }

    private boolean isPort(InstanceIdentifier<Subinterface> id, ReadContext readContext) throws ReadFailedException {
        return PortReader.getAllIds(cli, this, id, readContext)
                .contains(id.firstKeyOf(Interface.class));
    }
}
