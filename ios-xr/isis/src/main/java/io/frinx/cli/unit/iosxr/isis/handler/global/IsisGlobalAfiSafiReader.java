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

package io.frinx.cli.unit.iosxr.isis.handler.global;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.types.rev181121.AFITYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.types.rev181121.IPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.types.rev181121.IPV6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.types.rev181121.MULTICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.types.rev181121.SAFITYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.types.rev181121.UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.isis.rev181121.isis.afi.safi.list.Af;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.isis.rev181121.isis.afi.safi.list.AfBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.isis.rev181121.isis.afi.safi.list.AfKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.isis.rev181121.isis.afi.safi.list.af.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IsisGlobalAfiSafiReader implements CliConfigListReader<Af, AfKey, AfBuilder> {

    private static final String SH_AFI = "show running-config router isis %s | include ^ address-family";
    private static final Pattern ADDRESS_FAMILY_LINE = Pattern.compile("address-family (?<afi>\\S+) (?<safi>\\S+)");
    private Cli cli;

    public IsisGlobalAfiSafiReader(final Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<AfKey> getAllIds(
        @Nonnull InstanceIdentifier<Af> id,
        @Nonnull ReadContext readContext) throws ReadFailedException {

        String insName = id.firstKeyOf(Protocol.class).getName();
        String output = blockingRead(f(SH_AFI, insName), cli, id, readContext);
        return getAfiKeys(output);
    }

    @Override
    public void readCurrentAttributes(
        @Nonnull InstanceIdentifier<Af> id,
        @Nonnull AfBuilder builder,
        @Nonnull ReadContext readContext) throws ReadFailedException {

        AfKey key = id.firstKeyOf(Af.class);

        builder.setAfiName(key.getAfiName());
        builder.setSafiName(key.getSafiName());

        builder.setConfig(new ConfigBuilder()
            .setAfiName(key.getAfiName())
            .setSafiName(key.getSafiName())
            .build());
    }

    private static List<AfKey> getAfiKeys(String output) {
        return ParsingUtils.parseFields(output, 0,
            ADDRESS_FAMILY_LINE::matcher,
            m -> convertAfiKeyFromString(m.group("afi"), m.group("safi")),
            v -> v);
    }

    private static AfKey convertAfiKeyFromString(String afi, String safi) {
        Class<? extends AFITYPE> afiType = convertAfiTypeFromString(afi);
        Class<? extends SAFITYPE> safiType = convertSafiTypeFromString(safi);

        return new AfKey(afiType, safiType);
    }

    private static Class<? extends AFITYPE> convertAfiTypeFromString(String afi) {
        switch (afi) {
            case "ipv4":
                return IPV4.class;
            case "ipv6":
                return IPV6.class;
            default:
                throw new IllegalArgumentException("Unknown AFI type " + afi);
        }
    }

    private static Class<? extends SAFITYPE> convertSafiTypeFromString(String safi) {
        switch (safi) {
            case "unicast":
                return UNICAST.class;
            case "multicast":
                return MULTICAST.class;
            default:
                throw new IllegalArgumentException("Unknown SAFI type " + safi);
        }
    }

    public static String convertSafiTypeToString(Class<? extends SAFITYPE> safi) {
        if (safi == UNICAST.class) {
            return "unicast";
        } else if (safi == MULTICAST.class) {
            return "multicast";
        } else {
            throw new IllegalArgumentException("Unknown SAFI type " + safi.getName());
        }
    }

    public static String convertAfiTypeToString(Class<? extends AFITYPE> afi) {
        if (afi == IPV4.class) {
            return "ipv4";
        } else if (afi == IPV6.class) {
            return "ipv6";
        } else {
            throw new IllegalArgumentException("Unknown AFI type " + afi.getName());
        }
    }
}
