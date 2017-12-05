/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package io.frinx.cli.iosxr.ospf;

import com.google.common.collect.Lists;
import io.frinx.cli.iosxr.ospf.handler.MaxMetricConfigReader;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.MAXMETRICSUMMARYLSA;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.MAXMETRICINCLUDESTUB;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.MAXMETRICINCLUDETYPE2EXTERNAL;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.timers.max.metric.ConfigBuilder;

public class MaxMetricConfigReaderTest {

    private final String output = "Originating router-LSAs with maximum metric\n" +
            "    Condition: on start-up for 150 seconds, State: inactive\n" +
            "       Advertise stub links with maximum metric in router-LSAs\n" +
            "       Advertise summary-LSAs with metric 16711680\n" +
            "       Advertise external-LSAs with metric 16711680";


    @Test
    public void test() {
        ConfigBuilder builder = new ConfigBuilder();
        MaxMetricConfigReader.parseTimers(output, builder);
        Assert.assertEquals(150, builder.getTimeout().intValue());

        Assert.assertEquals(Lists.newArrayList(MAXMETRICINCLUDESTUB.class, MAXMETRICSUMMARYLSA.class, MAXMETRICINCLUDETYPE2EXTERNAL.class),
                builder.getInclude());
    }
}
