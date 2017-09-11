/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.bgp.handler;

import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.types.rev170202.AFISAFITYPE;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;

public class Ipv4Reader {

    static final String COMMAND = "sh bgp ipv4 unicast summ";

    static final Class<? extends AFISAFITYPE> AFI_SAFI = IPV4UNICAST.class;

    private Ipv4Reader() {
    }
}
