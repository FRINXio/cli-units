/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.iosxe.ifc.handler.service.instance;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.ServiceInstanceRewrite.Operation;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.ServiceInstanceRewrite.Type;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.ServiceInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.Rewrite;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.RewriteBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ServiceInstanceRewriteReader implements CliConfigReader<Rewrite, RewriteBuilder> {

    private static final Pattern REWRITE_LINE =
            Pattern.compile("rewrite (?<type>\\S+) tag (?<operation>\\S+) 1 symmetric");

    private final Cli cli;

    public ServiceInstanceRewriteReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Rewrite> instanceIdentifier,
                                      @Nonnull RewriteBuilder rewriteBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        final String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        final Long serviceInstanceId = instanceIdentifier.firstKeyOf(ServiceInstance.class).getId();
        final String showCommand = f(ServiceInstanceConfigReader.SH_SERVICE_INSTANCE, ifcName, serviceInstanceId);
        final String serviceInstanceOutput = blockingRead(showCommand, cli, instanceIdentifier, readContext);
        parseRewrite(serviceInstanceOutput, rewriteBuilder);
    }

    protected static void parseRewrite(String output, RewriteBuilder rewriteBuilder) {
        setType(output, rewriteBuilder);
        setOperation(output, rewriteBuilder);
    }

    private static void setOperation(String output, RewriteBuilder rewriteBuilder) {
        final Optional<String> operation = ParsingUtils.parseField(output, 0,
            REWRITE_LINE::matcher,
            matcher -> matcher.group("operation"));
        if (operation.isPresent()) {
            switch (operation.get()) {
                case "pop":
                    rewriteBuilder.setOperation(Operation.Pop);
                    break;
                case "push":
                    rewriteBuilder.setOperation(Operation.Push);
                    break;
                case "translate":
                    rewriteBuilder.setOperation(Operation.Translate);
                    break;
                default:
                    throw new IllegalArgumentException("Cannot parse Operation value: " + operation.get());
            }
        }
    }

    private static void setType(String output, RewriteBuilder rewriteBuilder) {
        final Optional<String> type = ParsingUtils.parseField(output, 0,
            REWRITE_LINE::matcher,
            matcher -> matcher.group("type"));
        if (type.isPresent()) {
            switch (type.get()) {
                case "ingress":
                    rewriteBuilder.setType(Type.Ingress);
                    break;
                case "egress":
                    rewriteBuilder.setType(Type.Egress);
                    break;
                default:
                    throw new IllegalArgumentException("Cannot parse Type value: " + type.get());
            }
        }
    }
}
