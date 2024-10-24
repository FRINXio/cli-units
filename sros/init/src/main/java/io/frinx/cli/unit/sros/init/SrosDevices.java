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

package io.frinx.cli.unit.sros.init;

import java.util.Set;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;

public final class SrosDevices {

    private SrosDevices() {

    }

    public static final Device SROS_GENERIC = new DeviceIdBuilder()
        .setDeviceType("sros")
        .setDeviceVersion("*")
        .build();

    public static final Device SROS_16 = new DeviceIdBuilder()
            .setDeviceType("sros")
            .setDeviceVersion("16.*")
            .build();

    public static final Set<Device> SROS_ALL = Set.of(SROS_16);
}
