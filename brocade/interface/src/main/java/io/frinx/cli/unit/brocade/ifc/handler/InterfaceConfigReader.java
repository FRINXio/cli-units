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

package io.frinx.cli.unit.brocade.ifc.handler;

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.brocade.ifc.Util;
import io.frinx.cli.unit.ifc.base.handler.AbstractInterfaceConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.brocade.extension.rev190726.IfBrocadePriorityAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.brocade.extension.rev190726.IfBrocadePriorityAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;

public final class InterfaceConfigReader extends AbstractInterfaceConfigReader {

    public static final String SH_SINGLE_INTERFACE_CFG = "show running-config interface {$ifcType} {$ifcNumber}";

    private static final Pattern SHUTDOWN_LINE = Pattern.compile("enable");
    private static final Pattern MTU_LINE = Pattern.compile("\\s*mtu (?<mtu>.+)$");
    private static final Pattern DESCR_LINE = Pattern.compile("\\s*port-name (?<desc>.+)");
    private static final Pattern PRIORITY_LINE = Pattern.compile("\\s*priority (?<priority>[0-7]{1})");
    private static final Pattern PRIORITY_FORCE_LINE = Pattern.compile("\\s*priority force");

    public InterfaceConfigReader(Cli cli) {
        super(cli);
    }

    @Override
    protected String getReadCommand(String ifcName) {
        Class<? extends InterfaceType> ifcType = parseType(ifcName);
        String ifcNumber = Util.getIfcNumber(ifcName);
        return fT(SH_SINGLE_INTERFACE_CFG, "ifcType", Util.getTypeOnDevice(ifcType), "ifcNumber", ifcNumber);
    }

    @Override
    protected Pattern getShutdownLine() {
        return SHUTDOWN_LINE;
    }

    @Override
    protected Pattern getMtuLine() {
        return MTU_LINE;
    }

    @Override
    protected Pattern getDescriptionLine() {
        return DESCR_LINE;
    }

    @Override
    public Class<? extends InterfaceType> parseType(String name) {
        return Util.parseType(name);
    }

    @Override
    public void parseInterface(String output, ConfigBuilder builder, String name) {
        super.parseInterface(output, builder, name);
        parsePriority(output, builder);
    }

    @Override
    public void parseEnabled(final String output, final ConfigBuilder builder) {
        // Set disabled unless proven otherwise
        builder.setEnabled(false);

        // Actually check if enabled
        ParsingUtils.parseField(output, 0,
            SHUTDOWN_LINE::matcher,
            matcher -> true,
            builder::setEnabled);
    }

    private void parsePriority(String output, ConfigBuilder builder) {
        IfBrocadePriorityAugBuilder priorityBuilder = new IfBrocadePriorityAugBuilder();
        ParsingUtils.parseField(output,
            PRIORITY_LINE::matcher,
            m -> Short.valueOf(m.group("priority")),
            priorityBuilder::setPriority);

        ParsingUtils.parseField(output,
            PRIORITY_FORCE_LINE::matcher,
            m -> true,
            priorityBuilder::setPriorityForce);

        builder.addAugmentation(IfBrocadePriorityAug.class, priorityBuilder.build());
    }
}
