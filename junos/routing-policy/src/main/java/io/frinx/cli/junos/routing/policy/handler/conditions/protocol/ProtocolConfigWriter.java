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

package io.frinx.cli.junos.routing.policy.handler.conditions.protocol;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.policy.rev170215.protocol.instance.policy.top.match.protocol.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.DIRECTLYCONNECTED;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.Statement;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ProtocolConfigWriter implements CliWriter<Config> {

    @VisibleForTesting
    static final String VERB_SET = "set";
    static final String VERB_DEL = "delete";
    static final String SH_WRITE_PROTOCOL_TYPE = "%s policy-options policy-statement %s term %s from protocol %s";
    static final String SH_WRITE_INSTANCE_NAME = "%s policy-options policy-statement %s term %s from instance %s";

    private final Cli cli;

    public ProtocolConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(
        @Nonnull InstanceIdentifier<Config> id, @Nonnull Config config, @Nonnull WriteContext writeContext)
            throws WriteFailedException {
        String statementName = id.firstKeyOf(PolicyDefinition.class).getName();
        String termId = id.firstKeyOf(Statement.class).getName();
        String protocolType = protocolTypeToString(config.getProtocolIdentifier());
        if (StringUtils.isNotEmpty(protocolType)) {
            String cmd = f(SH_WRITE_PROTOCOL_TYPE, VERB_SET, statementName, termId, protocolType);
            blockingWriteAndRead(cli, id, config, cmd);
        }
        String prInstanceName = config.getProtocolName();
        if (StringUtils.isNotEmpty(prInstanceName)) {
            String cmd = f(SH_WRITE_INSTANCE_NAME, VERB_SET, statementName, termId, prInstanceName);
            blockingWriteAndRead(cli, id, config, cmd);
        }
    }

    @Override
    public void updateCurrentAttributes(
        @Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore, @Nonnull Config dataAfter,
        @Nonnull WriteContext writeContext) throws WriteFailedException {
        // prepare list keys
        String statementName = id.firstKeyOf(PolicyDefinition.class).getName();
        String termId = id.firstKeyOf(Statement.class).getName();

        // update protocol type
        String protocolType = protocolTypeToString(dataAfter.getProtocolIdentifier());
        if (StringUtils.isNotEmpty(protocolType)) {
            String cmd = f(SH_WRITE_PROTOCOL_TYPE, VERB_SET, statementName, termId, protocolType);
            blockingWriteAndRead(cli, id, dataAfter, cmd);
        } else {
            // it is necessary to delete "protocol" in "dataBefore"
            protocolType = protocolTypeToString(dataBefore.getProtocolIdentifier());
            if (StringUtils.isNotEmpty(protocolType)) {
                String cmd = f(SH_WRITE_PROTOCOL_TYPE, VERB_DEL, statementName, termId, protocolType);
                blockingWriteAndRead(cli, id, dataAfter, cmd);
            }
        }

        // update protocol name
        String prInstanceName = dataAfter.getProtocolName();
        if (StringUtils.isNotEmpty(prInstanceName)) {
            String cmd = f(SH_WRITE_INSTANCE_NAME, VERB_SET, statementName, termId, prInstanceName);
            blockingWriteAndRead(cli, id, dataAfter, cmd);
        } else {
            prInstanceName = dataBefore.getProtocolName();
            // it is necessary to delete "instance" in "dataBefore"
            if (StringUtils.isNotEmpty(prInstanceName)) {
                String cmd = f(SH_WRITE_INSTANCE_NAME, VERB_DEL, statementName, termId, prInstanceName);
                blockingWriteAndRead(cli, id, dataAfter, cmd);
            }
        }
    }

    @Override
    public void deleteCurrentAttributes(
        @Nonnull InstanceIdentifier<Config> id, @Nonnull Config config, @Nonnull WriteContext writeContext)
            throws WriteFailedException {
        // prepare list keys
        String statementName = id.firstKeyOf(PolicyDefinition.class).getName();
        String termId = id.firstKeyOf(Statement.class).getName();

        // delete protocol type
        String protocolType = protocolTypeToString(config.getProtocolIdentifier());
        if (StringUtils.isNotEmpty(protocolType)) {
            String cmd = f(SH_WRITE_PROTOCOL_TYPE, VERB_DEL, statementName, termId, protocolType);
            blockingWriteAndRead(cli, id, config, cmd);
        }

        // update protocol name
        String prInstanceName = config.getProtocolName();
        if (StringUtils.isNotEmpty(prInstanceName)) {
            String cmd = f(SH_WRITE_INSTANCE_NAME, VERB_DEL, statementName, termId, prInstanceName);
            blockingWriteAndRead(cli, id, config, cmd);
        }
    }

    private String protocolTypeToString(
        Class<?> clazz) {
        if (DIRECTLYCONNECTED.class == clazz) {
            return "direct";
        }
        return null;
    }
}
