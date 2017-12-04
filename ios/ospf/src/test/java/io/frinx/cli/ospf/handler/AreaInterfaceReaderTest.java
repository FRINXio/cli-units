/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ospf.handler;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OspfAreaIdentifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.AreaKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;

public class AreaInterfaceReaderTest {

    private static final String OUTPUT = "Interface    PID   Area            IP Address/Mask    Cost  State Nbrs F/C\n" +
            "Lo0          1     0               10.255.255.1/32    1     LOOP  0/0\n" +
            "Lo0          1     0.0.0.0         10.255.255.1/32    1     LOOP  0/0\n" +
            "Gi1/0        1     0               10.255.2.2/24      1     BDR   1/1\n";

    private static final List<InterfaceKey> AREA_0_IFCS = Lists.newArrayList("Lo0", "Gi1/0")
            .stream()
            .map(InterfaceKey::new)
            .collect(Collectors.toList());

    private static final List<InterfaceKey> AREA_0000_IFCS = Lists.newArrayList("Lo0")
            .stream()
            .map(InterfaceKey::new)
            .collect(Collectors.toList());

    @Test
    public void parseInterfaceIds() throws Exception {
        List<InterfaceKey> interfaceKeys = AreaInterfaceReader.parseInterfaceIds(new AreaKey(new OspfAreaIdentifier(0L)), OUTPUT);
        assertEquals(AREA_0_IFCS, interfaceKeys);
        interfaceKeys = AreaInterfaceReader.parseInterfaceIds(new AreaKey(new OspfAreaIdentifier(new DottedQuad("0.0.0.0"))), OUTPUT);
        assertEquals(AREA_0000_IFCS, interfaceKeys);
    }

}