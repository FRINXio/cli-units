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

package io.frinx.cli.unit.huawei.bgp.handler;

import io.frinx.cli.unit.utils.CliFormatter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class GlobalAfiSafiConfigWriterTest implements CliFormatter{

    private String input;
    private String output;

    public GlobalAfiSafiConfigWriterTest(String input, String output) {
        this.input = input;
        this.output = output;
    }

    static CliFormatter cliF = new CliFormatter() {};

    static final String EXP_GLOBAL_BGP_AFISAFI_D = "system-view\n" +
            "bgp as_name\n" +
            "undo ipv4-family vrf" +
            "commit\n" +
            "return";

    static final String EXP_GLOBAL_BGP_AFISAFI = "system-view\n" +
            "bgp as_name\n" +
            "ipv4-family vrf\n" +
            "commit\n" +
            "return";

    static final String EXP_VRF_BGP_AFI_SAFI = "system-view\n" +
            "bgp as_name\n" +
            "ipv4-family vpn-instance vrf\n" +
            "commit\n" +
            "return";

    static final String EXP_VRF_BGP_AFI_SAFI_D = "system-view\n" +
            "bgp as_name\n" +
            "undo ipv4-family vpn-instance vrf\n" +
            "commit\n" +
            "return";


    static String name = "as_name";
    static String vrfName = "vrf";

    static String outputD = cliF.fT(GlobalAfiSafiConfigWriter.GLOBAL_BGP_AFI_SAFI,
            "as", name,
            "vrfName", vrfName,
            "delete", true);

    static String outp = cliF.fT(GlobalAfiSafiConfigWriter.GLOBAL_BGP_AFI_SAFI,
            "as", name,
            "vrfName", vrfName);

    static String outpVrf = cliF.fT(GlobalAfiSafiConfigWriter.VRF_BGP_AFI_SAFI,
            "as", name,
            "vrfName", vrfName);

    static String outpVrfD = cliF.fT(GlobalAfiSafiConfigWriter.VRF_BGP_AFI_SAFI,
            "as", name,
            "delete", true,
            "vrfName", vrfName);

    @Parameterized.Parameters(name = "name: {index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {EXP_GLOBAL_BGP_AFISAFI_D, outputD},
                {EXP_GLOBAL_BGP_AFISAFI, outp},
                {EXP_VRF_BGP_AFI_SAFI, outpVrf},
                {EXP_VRF_BGP_AFI_SAFI_D, outpVrfD}
        });
    }

    @Test
    public void testOfGlobalBGP() {
        Assert.assertEquals(input, output);
    }
}