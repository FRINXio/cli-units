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

package io.frinx.cli.unit.iosxr.ifc.handler.verify;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.Interface1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.RPFALLOWCONFIGBASE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.RPFALLOWDEFAULT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.RPFALLOWSELFPING;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.RpfCheckTop.RpfCheck;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.ipv4.verify.Ipv4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.ipv4.verify.Ipv4Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.ipv6.verify.Ipv6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.ipv6.verify.Ipv6Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.verify.unicast.source.reachable.via.top.VerifyUnicastSourceReachableVia;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.verify.unicast.source.reachable.via.top.VerifyUnicastSourceReachableViaBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class RpfCheckReader implements CliConfigReader<VerifyUnicastSourceReachableVia,
        VerifyUnicastSourceReachableViaBuilder> {

    private static final String SH_RPF_INTF =
            "show running-config interface %s | include verify unicast source reachable-via";
    private static final Pattern ALL_INGRESS_ACLS_LINE = Pattern.compile(
            "(?<type>.+) verify unicast source reachable-via (?<config>(any|rx))( ?(?<allowConfig>.*))?", Pattern
                    .DOTALL);

    private final Cli cli;

    public RpfCheckReader(final Cli cli) {
        this.cli = cli;
    }
    //ipv4/ipv6 verify unicast source reachable-via any

    @Override
    public void readCurrentAttributes(@Nonnull final InstanceIdentifier<VerifyUnicastSourceReachableVia>
                                                  instanceIdentifier,
                                      @Nonnull final VerifyUnicastSourceReachableViaBuilder builder,
                                      @Nonnull final ReadContext readContext)
            throws ReadFailedException {

        final String interfaceName = instanceIdentifier.firstKeyOf(Interface.class)
                .getName();

        final String readCommand = f(SH_RPF_INTF, interfaceName);
        final String readConfig = blockingRead(
                readCommand,
                cli,
                instanceIdentifier,
                readContext
        );

        parseRpfCheckConfig(readConfig, builder);
    }

    @VisibleForTesting
    void parseRpfCheckConfig(final String readConfig, final VerifyUnicastSourceReachableViaBuilder builder) {
        ParsingUtils.parseFields(readConfig, 0,
                ALL_INGRESS_ACLS_LINE::matcher,
                this::configFromMatcher,
            value -> {
                if (value instanceof Ipv4) {
                    builder.setIpv4((Ipv4) value);
                } else if (value instanceof Ipv6) {
                    builder.setIpv6((Ipv6) value);
                }

                return value;
            });
    }

    private ChildOf<? extends DataObject> configFromMatcher(final Matcher matcher) {
        final String type = matcher.group("type");
        final String rpfConfig = matcher.group("config");
        final String allowConfigString = matcher.group("allowConfig");

        final List<Class<? extends RPFALLOWCONFIGBASE>> allowConfig = new ArrayList<>();
        if (allowConfigString.contains("allow-self-ping")) {
            allowConfig.add(RPFALLOWSELFPING.class);
        }
        if (allowConfigString.contains("allow-default")) {
            allowConfig.add(RPFALLOWDEFAULT.class);
        }

        if ("ipv4".equalsIgnoreCase(type)) {
            return new Ipv4Builder()
                    .setRpfCheck(RpfCheck.valueOf(rpfConfig.toUpperCase()))
                    .setAllowConfig(allowConfig)
                    .build();
        } else if ("ipv6".equalsIgnoreCase(type)) {
            return new Ipv6Builder()
                    .setRpfCheck(RpfCheck.valueOf(rpfConfig.toUpperCase()))
                    .setAllowConfig(allowConfig)
                    .build();
        }

        throw new IllegalArgumentException("Could not parse RPF check config type, Should be ipv4 or ipv6.");
    }

    @Override
    public void merge(@Nonnull final Builder<? extends DataObject> parentBuilder,
                      @Nonnull final VerifyUnicastSourceReachableVia readValue) {
        ((Interface1Builder) parentBuilder).setVerifyUnicastSourceReachableVia(readValue);
    }
}
