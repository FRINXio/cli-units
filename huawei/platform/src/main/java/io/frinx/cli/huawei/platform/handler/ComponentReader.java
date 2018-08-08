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

package io.frinx.cli.huawei.platform.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperListReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.OsComponent;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.ComponentsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.Component;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.ComponentBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.ComponentKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ComponentReader implements CliOperListReader<Component, ComponentKey, ComponentBuilder> {

    private Cli cli;

    public ComponentReader(Cli cli) {
        this.cli = cli;
    }

    static final String SH_MODULE = "display elabel";

    static final Pattern LINE = Pattern.compile("^BoardType=(?<type>\\S+)$\\s*"
            + "^BarCode=(?<sn>.+)$\\s*"
            + "^Item=(?<item>.*)$\\s*"
            + "^Description=(?<descr>.*)$\\s*"
            + "^Manufactured=(?<date>.*)$\\s*"
            + "^VendorName=(?<vendor>.*)$\\s*"
            + "^?(IssueNumber=)?(?<issue>.*)$?\\s*"
            + "^CLEICode=(?<clei>.*)$\\s*"
            + "^BOM=(?<bom>.*)$\\s*",
            Pattern.MULTILINE);


    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Component> config) {
        ((ComponentsBuilder) builder).setComponent(config);
    }

    @Override
    public void readCurrentAttributes(InstanceIdentifier<Component> instanceIdentifier, ComponentBuilder
            componentBuilder, ReadContext readContext) {
        componentBuilder.setName(instanceIdentifier.firstKeyOf(Component.class)
                .getName());
    }

    @Nonnull
    @Override
    public List<ComponentKey> getAllIds(@Nonnull InstanceIdentifier<Component> id, @Nonnull ReadContext context)
            throws ReadFailedException {
        List<ComponentKey> componentKeys = getComponents(blockingRead(SH_MODULE, cli, id, context) + parseOS());
        componentKeys.addAll(parseOS());
        return componentKeys;
    }

    static List<ComponentKey> getComponents(String output) {
        Matcher matcher = LINE.matcher(output);
        ArrayList<ComponentKey> componentKeys = new ArrayList<>();

        while (matcher.find()) {
            String sn = matcher.group("sn").trim();
            componentKeys.add(new ComponentKey(sn));
        }

        return componentKeys;
    }

    private static List<ComponentKey> parseOS() {
        return Collections.singletonList(OsComponent.OS_KEY);
    }
}
