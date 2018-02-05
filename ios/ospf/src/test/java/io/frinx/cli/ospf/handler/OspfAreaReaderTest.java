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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.AreaKey;

public class OspfAreaReaderTest {

    public static final String OSPF_1 = " ip ospf 1 area 0\n" +
            " ip ospf 1 area 48\n" +
            " ip ospf 1 area 0.0.0.0\n" +
            " ip ospf 1 area 9.9.9.9\n";

    public static final List<AreaKey> AREAS = Lists.newArrayList("0", "48", "0.0.0.0", "9.9.9.9")
            .stream()
            .map(OspfAreaReader::getAreaIdentifier)
            .map(AreaKey::new)
            .collect(Collectors.toList());

    @Test
    public void testParseArea() throws Exception {
        List<AreaKey> areaKeys = OspfAreaReader.parseAreasIds(OSPF_1);
        assertEquals(AREAS, areaKeys);
    }
}