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

package io.frinx.cli.iosxr.qos.handler.scheduler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.InputsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.inputs.Input;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.inputs.InputBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.inputs.InputKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InputReader implements CliConfigListReader<Input, InputKey, InputBuilder> {

    private static final String SH_POLICY_MAP_CLASSES = "show running-config policy-map %s | include class";

    private Cli cli;

    public InputReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<InputKey> getAllIds(@Nonnull InstanceIdentifier<Input> instanceIdentifier, @Nonnull ReadContext
            readContext) throws ReadFailedException {
        String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class)
                .getName();
        Long seq = instanceIdentifier.firstKeyOf(Scheduler.class)
                .getSequence();
        String output = blockingRead(f(SH_POLICY_MAP_CLASSES, policyName), cli, instanceIdentifier, readContext);
        return getInputKeys(output, seq);
    }

    @VisibleForTesting
    public static List<InputKey> getInputKeys(final String output, final Long seq) {
        //if seq = 1 meaning we want first class-map, do not skip anything as first matching is done (which removes the
        // timestamp and any other header), then the skipping takes place
        List<InputKey> keys = new ArrayList<>(1);
        ParsingUtils.parseField(output, seq.intValue() - 1,
                SchedulerReader.CLASS_NAME_LINE::matcher,
            m -> m.group("name"), k -> keys.add(new InputKey(k)));
        return keys;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Input> list) {
        ((InputsBuilder) builder).setInput(list);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Input> instanceIdentifier, @Nonnull InputBuilder
            inputBuilder, @Nonnull ReadContext readContext) {
        String className = instanceIdentifier.firstKeyOf(Input.class)
                .getId();
        inputBuilder.setId(className);
    }
}
