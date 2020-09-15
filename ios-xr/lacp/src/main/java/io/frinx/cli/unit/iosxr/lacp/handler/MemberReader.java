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

package io.frinx.cli.unit.iosxr.lacp.handler;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.aggregation.lacp.members.top.members.Member;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.aggregation.lacp.members.top.members.MemberBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.aggregation.lacp.members.top.members.MemberKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.lacp.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class MemberReader implements CliConfigListReader<Member, MemberKey, MemberBuilder> {

    private static final String SHOW_ALL_INTERFACES_CONFIG = "show running-config interface";
    private static final Pattern INTERFACE_LINE_SEPARATOR = Pattern.compile("(?=interface (?<id>[\\S]{0,128}))");
    private static final Pattern LAG_ID_IN_INTERFACE_LINE = Pattern.compile("\\s*Bundle-Ether(?<id>\\d+).*");

    private final Cli cli;

    public MemberReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<MemberKey> getAllIds(@Nonnull InstanceIdentifier<Member> instanceIdentifier,
                                     @Nonnull ReadContext readContext) throws ReadFailedException {
        final String bundleName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        final String commandOutput = blockingRead(SHOW_ALL_INTERFACES_CONFIG, cli, instanceIdentifier, readContext);
        return parseMemberKeys(commandOutput, bundleName);
    }

    static List<MemberKey> parseMemberKeys(@Nonnull String commandOutput, @Nonnull String bundleName) {
        final String bundleId = parseBundleIdFromBundleName(bundleName);
        final Pattern specificBundlePattern = Pattern.compile("\\s*bundle id " + bundleId
                + " mode (?<mode>active|passive|on).*");
        return INTERFACE_LINE_SEPARATOR.splitAsStream(commandOutput)
                .filter(interfaceConfig -> isItIsInterfaceOfBundle(specificBundlePattern, interfaceConfig))
                .map(MemberReader::parseInterfaceName)
                .map(MemberKey::new)
                .collect(Collectors.toList());
    }

    private static String parseInterfaceName(@Nonnull String interfaceConfiguration) {
        final AtomicReference<String> interfaceName = new AtomicReference<>();
        ParsingUtils.parseField(
                interfaceConfiguration,
                BundleReader.INTERFACE_LINE::matcher,
            matcher -> matcher.group("id"),
                interfaceName::set);
        return interfaceName.get();
    }

    private static boolean isItIsInterfaceOfBundle(@Nonnull Pattern specificBundlePattern,
                                                   @Nonnull String interfaceConfiguration) {
        return Arrays.stream(ParsingUtils.NEWLINE.split(interfaceConfiguration))
                .map(specificBundlePattern::matcher)
                .anyMatch(Matcher::matches);
    }

    static String parseBundleIdFromBundleName(@Nonnull String bundleName) {
        final Matcher matcher = LAG_ID_IN_INTERFACE_LINE.matcher(bundleName);
        Preconditions.checkArgument(matcher.matches());
        return matcher.group("id");
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Member> instanceIdentifier,
                                      @Nonnull MemberBuilder memberBuilder, @Nonnull ReadContext readContext) {
        memberBuilder.setInterface(instanceIdentifier.firstKeyOf(Member.class).getInterface());
    }
}
