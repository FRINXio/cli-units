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

package io.frinx.cli.unit.iosxr.bfd.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.ifc.handler.aggregate.AggregateConfigReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.rev171117.bfd.top.bfd.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.rev171117.bfd.top.bfd.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.rev171117.bfd.top.bfd.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.rev171117.bfd.top.bfd.interfaces.InterfaceKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceReader implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder> {

    private static final String SH_RUN_INTERFACE = "show running-config interface";
    private static final Pattern INTERFACE_CHUNK = Pattern.compile("(?=interface [\\S]{0,128})");
    private static final Pattern BFD_MODE_ITEF = Pattern.compile("\\s*bfd mode ietf\\s*");
    private static final Pattern BFD_FAST_DETECT = Pattern.compile("\\s*bfd address-family ipv4 fast-detect\\s*");
    private static final Pattern INTERFACE_LINE = Pattern.compile("\\s*interface (?<id>[\\S]+).*");

    private final Cli cli;

    public InterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<InterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                        @Nonnull ReadContext readContext) throws ReadFailedException {
        // bundle interfaces are used as IDs
        return parseInterfaceIds(blockingRead(SH_RUN_INTERFACE, cli, instanceIdentifier, readContext));
    }

    List<InterfaceKey> parseInterfaceIds(@Nonnull String interfacesConfiguration) {
        return INTERFACE_CHUNK.splitAsStream(interfacesConfiguration)
                .filter(this::checkInterfaceConfiguration)
                .map(InterfaceReader::extractInterfaceId)
                .map(InterfaceKey::new)
                .collect(Collectors.toList());
    }

    private boolean checkInterfaceConfiguration(@Nonnull String interfaceConfiguration) {
        if (!isConfigurationOfBundleInterface(interfaceConfiguration)) {
            return false;
        }
        if (!isSupportedBfdConfig(interfaceConfiguration)) {
            LOG.info("{}: The interface {} bfd configuration read from device is not supported."
                            + "'bfd mode ietf' and/or 'bfd address-family ipv4 fast-detect' commands missing", cli,
                    extractInterfaceId(interfaceConfiguration));
            return false;
        }
        return true;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Interface> list) {
        ((InterfacesBuilder) builder).setInterface(list);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                      @Nonnull InterfaceBuilder interfaceBuilder, @Nonnull ReadContext readContext) {
        interfaceBuilder.setId(instanceIdentifier.firstKeyOf(Interface.class).getId());
    }

    private static String extractInterfaceId(@Nonnull String interfaceConfiguration) {
        final AtomicReference<String> interfaceId = new AtomicReference<>();
        ParsingUtils.parseField(interfaceConfiguration,
                INTERFACE_LINE::matcher,
            matcher -> matcher.group("id"),
                interfaceId::set);
        return interfaceId.get();
    }

    private static boolean isConfigurationOfBundleInterface(String output) {
        final String extractedId = extractInterfaceId(output);
        if (Objects.nonNull(extractedId)) {
            return AggregateConfigReader.isLAGInterface(extractedId);
        }
        return false;
    }

    private static boolean isSupportedBfdConfig(String output) {
        List<Boolean> bfdMode = ParsingUtils.parseFields(output, 0,
                BFD_MODE_ITEF::matcher,
            matcher -> true,
                Function.identity());

        List<Boolean> fastDetect = ParsingUtils.parseFields(output, 0,
                BFD_FAST_DETECT::matcher,
            matcher -> true,
                Function.identity());
        return !fastDetect.isEmpty() && !bfdMode.isEmpty();
    }
}
