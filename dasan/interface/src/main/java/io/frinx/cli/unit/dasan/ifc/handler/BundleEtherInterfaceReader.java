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

package io.frinx.cli.unit.dasan.ifc.handler;

import static io.frinx.cli.unit.utils.ParsingUtils.NEWLINE;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.dasan.utils.DasanCliUtil;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.translate.unit.commons.registry.common.CompositeListReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BundleEtherInterfaceReader implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder>,
        CompositeListReader.Child<Interface, InterfaceKey, InterfaceBuilder> {

    public static final String SHOW_BUNDLE_ETHER_LIST = "show running-config bridge | include ^ lacp aggregator";
    public static final Pattern LACP_INTERFACE_LINE = Pattern.compile("lacp aggregator\\s+(?<ids>.*)$");
    public static final String BUNDLE_ETHER_IF_NAME_PREFIX = "Bundle-Ether";
    public static final Pattern BUNDLE_ETHER_IF_NAME_PATTERN =
            Pattern.compile(BundleEtherInterfaceReader.BUNDLE_ETHER_IF_NAME_PREFIX + "(?<number>[0-9]+)");
    private Cli cli;

    public BundleEtherInterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<InterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                        @Nonnull ReadContext readContext) throws ReadFailedException {
        return parseInterfaceIds(blockingRead(SHOW_BUNDLE_ETHER_LIST, cli, instanceIdentifier, readContext));
    }

    @VisibleForTesting
    static List<InterfaceKey> parseInterfaceIds(String output) {
        return parseAllInterfaceIds(output);
    }


    static List<InterfaceKey> parseAllInterfaceIds(String output) {
        return NEWLINE.splitAsStream(output)
                .map(String::trim)
                .map(LACP_INTERFACE_LINE::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group("ids"))
                .flatMap(ids -> DasanCliUtil.parseIdRanges(ids).stream())
                .map(id -> "Bundle-Ether" + id)
                .map(InterfaceKey::new)
                .collect(Collectors.toList());
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                      @Nonnull InterfaceBuilder builder,
                                      @Nonnull ReadContext readContext) {

        builder.setName(instanceIdentifier.firstKeyOf(Interface.class).getName());
    }
}