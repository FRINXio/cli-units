/*
 * Copyright © 2022 Frinx and others.
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

package io.frinx.cli.unit.huawei.qos.handler.behavior;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.behavior.top.behaviors.Behavior;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.behavior.top.behaviors.BehaviorBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.behavior.top.behaviors.BehaviorKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BehaviorReader implements CliConfigListReader<Behavior, BehaviorKey, BehaviorBuilder> {

    private static final String SH_ALL_B = "display current-configuration | include traffic behavior";
    private static final Pattern CLASS_LINE = Pattern.compile("traffic behavior (?<name>\\S+)");

    private Cli cli;

    public BehaviorReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<BehaviorKey> getAllIds(@NotNull InstanceIdentifier<Behavior> instanceIdentifier,
                                       @NotNull ReadContext context) throws ReadFailedException {
        final List<BehaviorKey> behaviorKeys = new ArrayList<>();
        String output = blockingRead(SH_ALL_B, cli, instanceIdentifier, context);
        behaviorKeys.addAll(getBehaviorMapKeys(output));
        return behaviorKeys;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Behavior> instanceIdentifier,
                                      @NotNull BehaviorBuilder builder,
                                      @NotNull ReadContext ctx) throws ReadFailedException {
        final String behaviorName = instanceIdentifier.firstKeyOf(Behavior.class).getName();
        builder.setName(behaviorName);
    }

    @VisibleForTesting
    static List<BehaviorKey> getBehaviorMapKeys(String output) {
        return ParsingUtils.parseFields(output, 0, CLASS_LINE::matcher,
                matcher -> matcher.group("name"), BehaviorKey::new);
    }
}