package io.frinx.cli.ios.local.routing.common;

import io.frinx.cli.registry.common.TypedListReader;
import io.frinx.cli.unit.utils.CliListReader;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;

public interface LrListReader<O extends DataObject & Identifiable<K>, K extends Identifier<O>, B extends Builder<O>>
        extends LrReader<O, B>, TypedListReader<O, K, B>, CliListReader<O, K, B> {
}
