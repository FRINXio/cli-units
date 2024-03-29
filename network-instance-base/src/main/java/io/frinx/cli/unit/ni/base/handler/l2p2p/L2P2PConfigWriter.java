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

package io.frinx.cli.unit.ni.base.handler.l2p2p;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2P2PConfigWriter implements CompositeWriter.Child<Config> {

    private final Cli cli;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    public L2P2PConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(InstanceIdentifier<Config> instanceIdentifier, Config config,
                                                 WriteContext writeContext)
        throws WriteFailedException.CreateFailedException {
        // NOOP at this level
        return true;
    }

    @Override
    public boolean updateCurrentAttributesWResult(InstanceIdentifier<Config> id, Config dataBefore, Config dataAfter,
        WriteContext writeContext)
        throws WriteFailedException {
        // NOOP at this level
        return true;
    }

    @Override
    public boolean deleteCurrentAttributesWResult(InstanceIdentifier<Config> instanceIdentifier, Config config,
        WriteContext writeContext)
        throws WriteFailedException.DeleteFailedException {
        // NOOP at this level
        return true;
    }
}
