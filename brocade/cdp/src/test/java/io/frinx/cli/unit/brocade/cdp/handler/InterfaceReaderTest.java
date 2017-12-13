package io.frinx.cli.unit.brocade.cdp.handler;

import org.junit.Test;

public class InterfaceReaderTest {

    @Test
    public void testParseCdpIfcs() throws Exception {
        InterfaceReader.parseCdpInterfaces("interface loopback 1\n" +
                "interface loopback 2\n" +
                "interface management 1\n" +
                "interface ethernet 1/1\n" +
                "interface ethernet 1/2\n" +
                "interface ethernet 1/3\n" +
                "interface ethernet 1/4\n" +
                "interface ethernet 1/5\n" +
                "interface ethernet 1/6\n" +
                "interface ethernet 1/7\n" +
                "interface ethernet 1/8\n" +
                "interface ethernet 1/9\n" +
                "interface ethernet 1/10\n" +
                "interface ethernet 1/11\n" +
                "interface ethernet 1/12\n" +
                "interface ethernet 1/13\n" +
                "interface ethernet 1/14\n" +
                "interface ethernet 1/15\n" +
                "interface ethernet 1/17\n" +
                "interface ethernet 1/18\n" +
                "interface ethernet 1/19\n" +
                " no cdp enable\n" +
                "interface ethernet 1/20\n" +
                "interface ethernet 2/1                                            \n" +
                "interface ethernet 2/2\n" +
                " no cdp enable\n" +
                "interface ethernet 2/3\n" +
                "interface ethernet 2/4\n" +
                "interface ethernet 2/5\n" +
                "interface ethernet 2/6\n" +
                "interface ethernet 2/7\n" +
                "interface ethernet 2/8\n" +
                "interface ethernet 2/9\n" +
                "interface ethernet 2/10\n" +
                " no cdp enable\n" +
                "interface ethernet 2/11\n" +
                " no cdp enable\n" +
                "interface ethernet 2/12\n" +
                " no cdp enable\n" +
                "interface ethernet 2/13\n" +
                "interface ethernet 2/14\n" +
                " no cdp enable\n" +
                "interface ethernet 2/15\n" +
                "interface ethernet 2/16\n" +
                "interface ethernet 2/17\n" +
                " no cdp enable\n" +
                "interface ethernet 2/18                                           \n" +
                " no cdp enable\n" +
                "interface ethernet 2/19\n" +
                " no cdp enable\n" +
                "interface ethernet 2/20\n" +
                " no cdp enable\n" +
                "interface ethernet 3/1\n" +
                "interface ethernet 3/6\n" +
                "interface ethernet 3/7\n" +
                "interface ethernet 3/8\n" +
                "interface ethernet 3/10\n" +
                "interface ethernet 3/14\n" +
                "interface ethernet 3/15\n" +
                "interface ethernet 3/16\n" +
                "interface ethernet 3/17\n" +
                "interface ethernet 3/18\n" +
                "interface ethernet 3/19\n" +
                "interface ethernet 3/20\n" +
                "interface ethernet 4/1\n" +
                " no cdp enable\n" +
                "interface ethernet 4/2\n" +
                " no cdp enable\n" +
                "interface ve 3\n" +
                "interface ve 4                                                    \n" +
                "interface ve 7\n" +
                "interface ve 9\n" +
                "interface ve 12\n" +
                "interface ve 32\n" +
                "interface ve 44\n" +
                "interface ve 77\n" +
                "interface ve 100\n" +
                "interface ve 112\n" +
                "interface ve 150\n" +
                "interface ve 200\n" +
                "interface ve 210\n" +
                "interface ve 212\n");
    }
}