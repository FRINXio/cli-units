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

package io.frinx.cli.unit.ios.unit.acl.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclInterfaceReader implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder> {

    private final Cli cli;
    private static final String SH_IFACES = "show running-config";
    private static final Pattern IFACE_LINE = Pattern.compile("interface (?<name>.+)");

    public AclInterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<InterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Interface> instanceIdentifier, @Nonnull
            ReadContext readContext) throws ReadFailedException {
        return getInterfaceKeys(blockingRead(SH_IFACES, cli, instanceIdentifier, readContext));
    }

    @VisibleForTesting
    public static List<InterfaceKey> getInterfaceKeys(String output) {
        List<InterfaceKey> keys = new ArrayList<>();
        List<String> candidates = Pattern.compile("!").splitAsStream(output).collect(Collectors.toList());
        for (String candidate : candidates) {
            Optional<Boolean> maybeAcl = ParsingUtils.parseField(candidate, 0, Pattern.compile(".*access-group.*")
                    ::matcher, Matcher::matches);
            if (maybeAcl.isPresent() && maybeAcl.get()) {
                ParsingUtils.parseField(candidate, 0, IFACE_LINE::matcher, matcher -> matcher.group("name"))
                        .ifPresent(s -> keys.add(new InterfaceKey(new InterfaceId(s))));
            }
        }
        return keys;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Interface> list) {
        ((InterfacesBuilder) builder).setInterface(list);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Interface> instanceIdentifier, @Nonnull
            InterfaceBuilder interfaceBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        InterfaceKey key = instanceIdentifier.firstKeyOf(Interface.class);
        interfaceBuilder.setId(key.getId());
    }
}
