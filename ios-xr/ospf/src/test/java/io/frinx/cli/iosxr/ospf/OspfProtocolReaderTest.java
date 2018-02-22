/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.ospf;

import com.google.common.collect.Lists;
import io.frinx.cli.handlers.ospf.OspfReader;
import io.frinx.cli.iosxr.ospf.handler.OspfProtocolReader;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;

public class OspfProtocolReaderTest {
    private static final String SH_RUN_OSPF = "Mon Feb 12 14:57:11.223 UTC\n" +
            "router ospf 97\n" +
            "router ospf EXAMPLE_OSPF\n";

    private static final List<ProtocolKey> EXPECTED_KEYES = Lists.newArrayList("97", "EXAMPLE_OSPF")
            .stream().map(ospfId -> new ProtocolKey(OspfReader.TYPE, ospfId)).collect(Collectors.toList());

    @Test
    public void testParseOspfIds() {
        Assert.assertEquals(EXPECTED_KEYES, OspfProtocolReader.parseOspfIds(SH_RUN_OSPF));
    }

}
