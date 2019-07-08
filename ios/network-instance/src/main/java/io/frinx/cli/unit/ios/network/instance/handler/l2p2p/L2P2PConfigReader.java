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

package io.frinx.cli.unit.ios.network.instance.handler.l2p2p;

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ni.base.handler.l2p2p.AbstractL2P2ConfigReader;

public final class L2P2PConfigReader extends AbstractL2P2ConfigReader {

    public L2P2PConfigReader(Cli cli) {
        super(new L2P2PReader(cli));
    }
}
