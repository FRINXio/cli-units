/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.ubnt.es.init;

import java.util.HashSet;
import java.util.Set;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;

public final class UbntEsDevices {

    private UbntEsDevices() {

    }

    public static final Device UBNT_ES_1 = new DeviceIdBuilder()
            .setDeviceType("ubnt es")
            .setDeviceVersion("1.*")
            .build();

    public static final Device UBNT_ES_GENERIC = new DeviceIdBuilder()
            .setDeviceType("ubnt es")
            .setDeviceVersion("*")
            .build();

    public static final Set<Device> UBNT_ES_ALL = new HashSet<Device>() {
        {
            add(UBNT_ES_1);
        }
    };
}