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

package io.frinx.cli.unit.dasan.utils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.collections.Sets;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class DasanCliUtilTest {

    private static String SHOW_ALL_PORTS;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        SHOW_ALL_PORTS = (String) FieldUtils.readStaticField(DasanCliUtil.class, "SHOW_ALL_PORTS", true);
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    @PrepareOnlyThisForTest(DasanCliUtil.class)
    public void testGetPhysicalPorts_001() throws Exception {

        final Cli cli = Mockito.mock(Cli.class);
        final CliReader<Config, ConfigBuilder> cliReader = Mockito.mock(CliReader.class);
        final InstanceIdentifier<Config> id = InstanceIdentifier.create(Config.class);
        final ReadContext readContext = Mockito.mock(ReadContext.class);
        final String showportsStr = StringUtils.join(new String[] {
            "------------------------------------------------------------------------",
            "NO      TYPE     PVID    STATUS        MODE       FLOWCTRL     INSTALLED",
            "                      (ADMIN/OPER)              (ADMIN/OPER)",
            "------------------------------------------------------------------------",
            "1/1   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       Y",
            "1/2   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       Y",
            "2/1   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       Y",
            "2/2   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       N",
            "3/1       None      1     Up/Down  Force/Half/0    N/A/ Off       N",
            "3/2       None      1     Up/Down  Force/Half/0    N/A/ Off       Y",
            "t/11  TrkGrp00      1     Up/Up    NA/NA/3000      N/A/ N/A       Y",
            "t/12  TrkGrp00      1     Up/Up    NA/NA/3000      N/A/ N/A       Y",
            }, "\n");



        final List<String> physportids = new ArrayList<>();

        Mockito.doReturn(showportsStr).when(cliReader).blockingRead(SHOW_ALL_PORTS, cli, id, readContext);
        PowerMockito.mockStatic(DasanCliUtil.class, Mockito.CALLS_REAL_METHODS);
        PowerMockito.doReturn(physportids).when(DasanCliUtil.class, "parsePhysicalPorts", showportsStr);


        List<String> result = DasanCliUtil.getPhysicalPorts(cli, cliReader, id, readContext);


        assertThat(result, sameInstance(physportids));


        Mockito.verify(cliReader).blockingRead(SHOW_ALL_PORTS, cli, id, readContext);
        PowerMockito.verifyStatic();
        DasanCliUtil.parsePhysicalPorts(showportsStr);
    }

    @Test
    @PrepareOnlyThisForTest(DasanCliUtil.class)
    public void testGetPhysicalPorts_002() throws Exception {

        final Cli cli = Mockito.mock(Cli.class);
        final CliReader<Config, ConfigBuilder> cliReader = Mockito.mock(CliReader.class);
        final InstanceIdentifier<Config> id = InstanceIdentifier.create(Config.class);
        final ReadContext readContext = Mockito.mock(ReadContext.class);
        final String showportsStr = StringUtils.join(new String[] {
            "------------------------------------------------------------------------",
            "NO      TYPE     PVID    STATUS        MODE       FLOWCTRL     INSTALLED",
            "                      (ADMIN/OPER)              (ADMIN/OPER)",
            "------------------------------------------------------------------------",
            "1/1   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       Y",
            "1/2   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       Y",
            "2/1   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       Y",
            "2/2   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       N",
            "3/1       None      1     Up/Down  Force/Half/0    N/A/ Off       N",
            "3/2       None      1     Up/Down  Force/Half/0    N/A/ Off       Y",
            "t/11  TrkGrp00      1     Up/Up    NA/NA/3000      N/A/ N/A       Y",
            "t/12  TrkGrp00      1     Up/Up    NA/NA/3000      N/A/ N/A       Y",
            }, "\n");
        final ReadFailedException exception = new ReadFailedException(id, null);


        final List<String> physportids = new ArrayList<>();

        Mockito.doThrow(exception).when(cliReader).blockingRead(SHOW_ALL_PORTS, cli, id, readContext);
        PowerMockito.mockStatic(DasanCliUtil.class, Mockito.CALLS_REAL_METHODS);
        PowerMockito.doReturn(physportids).when(DasanCliUtil.class, "parsePhysicalPorts", showportsStr);


        try {
            DasanCliUtil.getPhysicalPorts(cli, cliReader, id, readContext);
            fail("Expected exception was not occurred.");
        } catch (ReadFailedException e) {
            assertThat(e, sameInstance(exception));
        }


        Mockito.verify(cliReader).blockingRead(SHOW_ALL_PORTS, cli, id, readContext);
        PowerMockito.verifyStatic(never());
        DasanCliUtil.parsePhysicalPorts(showportsStr);
    }

    @Test
    public void testParsePhysicalPorts_001() throws Exception {

        final String showportsStr = StringUtils.join(new String[] {
            "------------------------------------------------------------------------",
            "NO      TYPE     PVID    STATUS        MODE       FLOWCTRL     INSTALLED",
            "                      (ADMIN/OPER)              (ADMIN/OPER)",
            "------------------------------------------------------------------------",
            "1/1   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       Y",
            "1/2   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       Y",
            "2/1   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       Y",
            "2/2   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       N",
            "3/1       None      1     Up/Down  Force/Half/0    N/A/ Off       N",
            "3/2       None      1     Up/Down  Force/Half/0    N/A/ Off       Y",
            "t/11  TrkGrp00      1     Up/Up    NA/NA/3000      N/A/ N/A       Y",
            "t/12  TrkGrp00      1     Up/Up    NA/NA/3000      N/A/ N/A       Y",
            }, "\n");


        List<String> result = DasanCliUtil.parsePhysicalPorts(showportsStr);

        assertThat(result.size(), is(8));
        assertThat(result.get(0), equalTo("1/1"));
        assertThat(result.get(1), equalTo("1/2"));
        assertThat(result.get(2), equalTo("2/1"));
        assertThat(result.get(3), equalTo("2/2"));
        assertThat(result.get(4), equalTo("3/1"));
        assertThat(result.get(5), equalTo("3/2"));
        assertThat(result.get(6), equalTo("t/11"));
        assertThat(result.get(7), equalTo("t/12"));

    }

    @Test
    @PrepareOnlyThisForTest(DasanCliUtil.class)
    public void testContainsPort_001() throws Exception {
        final List<String> ports = new ArrayList<>();
        final String ranges = "1/1";
        final Set<String> parsedPort = new HashSet<String>() {
            {
                add("1/1");
                add("1/3");
            }
        };

        PowerMockito.mockStatic(DasanCliUtil.class, Mockito.CALLS_REAL_METHODS);
        PowerMockito.doReturn(parsedPort).when(DasanCliUtil.class, "parsePortRanges", ports, ranges);

        assertThat(DasanCliUtil.containsPort(ports, ranges, "1/0"), is(false));
        assertThat(DasanCliUtil.containsPort(ports, ranges, "1/1"), is(true));
        assertThat(DasanCliUtil.containsPort(ports, ranges, "1/2"), is(false));
        assertThat(DasanCliUtil.containsPort(ports, ranges, "1/3"), is(true));
        assertThat(DasanCliUtil.containsPort(ports, ranges, "1/4"), is(false));

        PowerMockito.verifyStatic(Mockito.times(5));
        DasanCliUtil.parsePortRanges(ports, ranges);
    }


    @Test
    public void testParsePortRangesA_001() throws Exception {
        final List<String> ports = Lists.newArrayList(
            "1/1", "1/2",
            "2/1", "2/2",
            "t/1", "t/2", "t/3");

        assertThat(DasanCliUtil.parsePortRanges(ports, "1/0"), equalTo(Collections.emptySet()));
        assertThat(DasanCliUtil.parsePortRanges(ports, "1/1"), equalTo(Collections.singleton("1/1")));
        assertThat(DasanCliUtil.parsePortRanges(ports, "1/2"), equalTo(Collections.singleton("1/2")));
        assertThat(DasanCliUtil.parsePortRanges(ports, "1/3"), equalTo(Collections.emptySet()));

        assertThat(DasanCliUtil.parsePortRanges(ports, "1/1,2/2"), equalTo(Sets.newSet("1/1", "2/2")));
        assertThat(DasanCliUtil.parsePortRanges(ports, "1/2-t/1"), equalTo(Sets.newSet("1/2", "2/1", "2/2", "t/1")));

        assertThat(DasanCliUtil.parsePortRanges(ports, "1/1,2/2-t/1,t/3"), equalTo(
            Sets.newSet("1/1", "2/2", "t/1", "t/3")));
    }

    @Test
    @PrepareOnlyThisForTest(DasanCliUtil.class)
    public void testContainsId_001() throws Exception {

        final String ranges = "2,4-6";
        final Set<String> parsedIds = Sets.newSet("2");

        PowerMockito.mockStatic(DasanCliUtil.class, Mockito.CALLS_REAL_METHODS);
        PowerMockito.doReturn(parsedIds).when(DasanCliUtil.class, "parseIdRanges", ranges);


        assertThat(DasanCliUtil.containsId(ranges, "1"), is(false));
        assertThat(DasanCliUtil.containsId(ranges, "2"), is(true));


        PowerMockito.verifyStatic(times(2));
        DasanCliUtil.parseIdRanges(ranges);
    }

    @Test
    public void testParseIdRanges_001() throws Exception {

        assertThat(DasanCliUtil.parseIdRanges("2"), equalTo(Sets.newSet("2")));
        assertThat(DasanCliUtil.parseIdRanges("4,6"), equalTo(Sets.newSet("4", "6")));
        assertThat(DasanCliUtil.parseIdRanges("8-10"), equalTo(Sets.newSet("8", "9", "10")));
        assertThat(DasanCliUtil.parseIdRanges("2,4-6,8-10"), equalTo(Sets.newSet("2", "4", "5", "6", "8", "9", "10")));

    }

    static class Config implements DataObject {
        @Override
        public Class<? extends DataContainer> getImplementedInterface() {
            return null;
        }
    }

    static class ConfigBuilder implements Builder<Config> {
        @Override
        public Config build() {

            return null;
        }
    }


}
