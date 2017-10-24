/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.lldp.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.neighbors.NeighborKey;

import java.util.List;
import java.util.stream.Collectors;

public class NeighborReaderTest {

    private static final String SH_LLDP_NEIGHBOR = "System Name: PE3.demo.frinx.io\n" +
            "System Name: XE2.FRINX\n" +
            "System Name: PE1.demo.frinx.io\n" +
            "System Name: Router2";

    private static final List<NeighborKey> EXPECTED_IDS =
            Lists.newArrayList("Router2", "PE3.demo.frinx.io", "XE2.FRINX", "PE1.demo.frinx.io")
            .stream()
            .map(NeighborKey::new)
            .collect(Collectors.toList());

    @Test
    public void testParseNeighborIds() {
        List<NeighborKey> actualIds = NeighborReader.parseNeighborIds(SH_LLDP_NEIGHBOR);
        Assert.assertEquals(Sets.newHashSet(EXPECTED_IDS), Sets.newHashSet(actualIds));
    }

}