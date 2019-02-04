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

package io.frinx.cli.unit.junos.network.instance.handler.policy.forwarding;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.regex.Matcher;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension.juniper.rev171109.NiPfIfJuniperAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension.juniper.rev171109.juniper.pf._interface.extension.config.Classifiers;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension.juniper.rev171109.juniper.pf._interface.extension.config.classifiers.InetPrecedence;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces._interface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PolicyForwardingInterfaceConfigWriter implements CliWriter<Config> {
    private static String CREATE_TEMPLATE =
        "set class-of-service interfaces %s unit %s classifiers inet-precedence %s";
    private static String DELETE_TEMPLATE =
        "delete class-of-service interfaces %s unit %s classifiers inet-precedence %s";

    private Cli cli;

    public PolicyForwardingInterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(
        @Nonnull InstanceIdentifier<Config> id,
        @Nonnull Config dataAfter,
        @Nonnull WriteContext writeContext) throws WriteFailedException {

        Preconditions.checkArgument(NetworInstance.DEFAULT_NETWORK.equals(id.firstKeyOf(NetworkInstance.class)),
                "Policy forwarding should be configured in default network instance");

        String interfaceId = id.firstKeyOf(Interface.class).getInterfaceId().getValue();
        Matcher matcher = PolicyForwardingInterfaceConfigReader.INTERFACE_ID_PATTERN.matcher(interfaceId);
        Preconditions.checkArgument(matcher.matches(),
            "Interface id does not match '<interface>.<unit>'. id=" + interfaceId);

        String precedenceName = getPrecedenceName(dataAfter);

        if (precedenceName != null) {
            blockingWriteAndRead(cli, id, dataAfter,
                f(CREATE_TEMPLATE, matcher.group("ifname"), matcher.group("unit"), precedenceName));
        }
    }

    @Override
    public void updateCurrentAttributes(
        @Nonnull InstanceIdentifier<Config> id,
        @Nonnull Config dataBefore,
        @Nonnull Config dataAfter,
        @Nonnull WriteContext writeContext) throws WriteFailedException {

        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(
        @Nonnull InstanceIdentifier<Config> id,
        @Nonnull Config dataBefore,
        @Nonnull WriteContext writeContext) throws WriteFailedException {

        String interfaceId = id.firstKeyOf(Interface.class).getInterfaceId().getValue();
        Matcher matcher = PolicyForwardingInterfaceConfigReader.INTERFACE_ID_PATTERN.matcher(interfaceId);
        matcher.matches();

        String precedenceName = getPrecedenceName(dataBefore);

        if (precedenceName != null) {
            blockingDeleteAndRead(cli, id,
                f(DELETE_TEMPLATE, matcher.group("ifname"), matcher.group("unit"), precedenceName));
        }
    }

    private static String getPrecedenceName(Config dataAfter) {
        NiPfIfJuniperAug juniperAug = dataAfter.getAugmentation(NiPfIfJuniperAug.class);
        if (juniperAug == null) {
            return null;
        }

        Classifiers classifiers = juniperAug.getClassifiers();
        if (classifiers == null) {
            return null;
        }

        InetPrecedence inetPrecedence = classifiers.getInetPrecedence();
        if (inetPrecedence == null) {
            return null;
        }

        return inetPrecedence.getName();
    }
}
