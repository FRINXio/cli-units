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

package io.frinx.cli.unit.junos.ifc.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.AbstractInterfaceReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class InterfaceReader extends AbstractInterfaceReader {

    public static final String SHOW_INTERFACES = "show configuration interfaces | display set";

    private static final Pattern INTERFACE_ID_LINE = Pattern.compile("set interfaces (?<id>[\\S]+) .*");

    private static final Pattern SUBINTERFACE_ID_LINE = Pattern.compile("set interfaces (?<id>[\\S]+ unit [0-9]+).*");

    public static final Pattern SUBINTERFACE_NAME = Pattern.compile("(?<ifcId>[\\S]+) unit (?<subifcIndex>[0-9]+)");

    public InterfaceReader(Cli cli) {
        super(cli);
    }

    @Nonnull
    @Override
    public List<InterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
        @Nonnull ReadContext readContext) throws ReadFailedException {
        return super.getAllIds(instanceIdentifier, readContext).stream().distinct().collect(Collectors.toList());
    }

    @Override
    @VisibleForTesting
    public List<InterfaceKey> parseAllInterfaceIds(String output) {
        List<InterfaceKey> keys = super.parseAllInterfaceIds(output);
        keys.addAll(new ArrayList<>(ParsingUtils.parseFields(output, 0,
            SUBINTERFACE_ID_LINE::matcher, matcher -> matcher.group("id"),
            InterfaceKey::new)));
        return keys;
    }

    @Override
    protected String getReadCommand() {
        return SHOW_INTERFACES;
    }

    @Override
    protected Pattern getInterfaceIdLine() {
        return INTERFACE_ID_LINE;
    }

    @Override
    protected Pattern subinterfaceName() {
        return SUBINTERFACE_NAME;
    }
}
