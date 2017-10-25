/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.frinx.cli.ios.it;

import static io.frinx.cli.utils.NoopDataBroker.NOOP_DATA_BROKER;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.gson.stream.JsonWriter;
import io.fd.honeycomb.data.ModifiableDataManager;
import io.fd.honeycomb.data.ReadableDataManager;
import io.fd.honeycomb.data.impl.ModifiableDataTreeDelegator;
import io.fd.honeycomb.data.impl.ReadableDataTreeDelegator;
import io.fd.honeycomb.rpc.RpcRegistryBuilder;
import io.fd.honeycomb.translate.impl.read.registry.CompositeReaderRegistryBuilder;
import io.fd.honeycomb.translate.impl.write.registry.FlatWriterRegistryBuilder;
import io.fd.honeycomb.translate.read.registry.ReaderRegistry;
import io.fd.honeycomb.translate.util.YangDAG;
import io.fd.honeycomb.translate.write.registry.WriterRegistry;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.impl.IOFactory;
import io.frinx.cli.io.impl.cli.KeepaliveCli;
import io.frinx.cli.ios.bgp.BgpUnit;
import io.frinx.cli.ios.local.routing.LocalRoutingUnit;
import io.frinx.cli.ospf.OspfUnit;
import io.frinx.cli.registry.api.TranslateContext;
import io.frinx.cli.registry.impl.TranslateRegistryImpl;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.topology.RemoteDeviceId;
import io.frinx.cli.unit.generic.GenericTranslateUnit;
import io.frinx.cli.unit.ios.cdp.IosCdpUnit;
import io.frinx.cli.unit.ios.essential.IosEssentialUnit;
import io.frinx.cli.unit.ios.ifc.IosInterfaceUnit;
import io.frinx.cli.unit.ios.lldp.LldpUnit;
import io.frinx.cli.unit.ios.network.instance.IosNetworkInstanceUnit;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.security.Security;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.impl.BindingDOMAdapterLoader;
import org.opendaylight.controller.md.sal.binding.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.controller.md.sal.dom.api.DOMDataBroker;
import org.opendaylight.controller.md.sal.dom.api.DOMDataReadOnlyTransaction;
import org.opendaylight.controller.md.sal.dom.api.DOMService;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Host;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNodeConnectionParameters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.cli.node.credentials.credentials.LoginPasswordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TipProducingDataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactory;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.LoggerFactory;

public class IosAll {

    private static final Device IOS_ALL = new DeviceIdBuilder()
            .setDeviceType("ios")
            .setDeviceVersion("*")
            .build();

    private static final String IOS_ID = "ios-it";
    private static final int PORT = 22;
    private static final String HOST = "192.168.1.230";
    private static final InetSocketAddress IOS_ADDR = new InetSocketAddress(HOST, PORT);
    private static final CliNode CLI_CFG = new CliNodeBuilder()
            .setPort(new PortNumber(PORT))
            .setHost(new Host(new IpAddress(new Ipv4Address(HOST))))
            .setDeviceType(IOS_ALL.getDeviceType())
            .setDeviceVersion(IOS_ALL.getDeviceVersion())
            .setTransportType(CliNodeConnectionParameters.TransportType.Ssh)
            .setCredentials(new LoginPasswordBuilder()
                    .setUsername("cisco")
                    .setPassword("cisco")
                    .build())
            .setKeepaliveDelay(120)
            .setKeepaliveTimeout(30)
            .setKeepaliveInitialDelay(120)
            .build();

    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(4);

    private static final KeepaliveCli.ReconnectListener RECONNECT_LISTENER = init -> {
        throw new RuntimeException("Disconnected !");
    };

    @Mock
    private DataBroker mockBroker;
    @Mock
    private ReadWriteTransaction mockTx;
    @Mock
    private BindingTransactionChain mockTxChain;

    private DataBroker bindingBroker;
    private DOMDataBroker domBroker;
    private SchemaContext schemaCtx;

    @Before
    public void setUp() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        setRootLogLevel();
        MockitoAnnotations.initMocks(this);
        mockBroker();

        TranslateRegistryImpl reg = getTranslateRegistry(mockBroker);

        TranslateContext translateContext = reg.getTranslateContext(IOS_ALL);

        RemoteDeviceId remoteId = new RemoteDeviceId(IOS_ID, IOS_ADDR);
        Cli io = IOFactory.getIO(remoteId, CLI_CFG, translateContext.getInitializer(remoteId, CLI_CFG), EXECUTOR, RECONNECT_LISTENER)
                .toCompletableFuture()
                .get();

        schemaCtx = translateContext.getSchemaContext();
        BindingToNormalizedNodeCodec codec = translateContext.getCodec();

        // Get & register CRUD handlers
        CompositeReaderRegistryBuilder rRegistryBuilder = new CompositeReaderRegistryBuilder(new YangDAG());
        FlatWriterRegistryBuilder wRegistryBuilder = new FlatWriterRegistryBuilder(new YangDAG());
        TranslateUnit.Context transportContext = () -> io;

        translateContext.provideHandlers(rRegistryBuilder, wRegistryBuilder, transportContext);
        ReaderRegistry rRegistry = rRegistryBuilder.build();
        WriterRegistry wRegistry = wRegistryBuilder.build();

        // Get DOM brokers
        TipProducingDataTree dataTree = getDataTree(schemaCtx, TreeType.CONFIGURATION);
        domBroker = getDomBroker(schemaCtx, NOOP_DATA_BROKER, codec, rRegistry, wRegistry, dataTree);

        // Get & register RPC handlers
        RpcRegistryBuilder rpcRegBuilder = new RpcRegistryBuilder();
        translateContext.getRpcs(transportContext).forEach(rpcRegBuilder::addService);

        bindingBroker = ((DataBroker) new BindingDOMAdapterLoader(codec) {
            protected DOMService getDelegate(Class<? extends DOMService> reqDeleg) {
                return domBroker;
            }
        }.load(DataBroker.class).get());
    }

    private void setRootLogLevel() {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        Logger cli = (Logger) LoggerFactory.getLogger("io.frinx.cli");
        cli.setLevel(Level.DEBUG);
    }

    private void mockBroker() {
        doReturn(Futures.immediateCheckedFuture(null)).when(mockTx).submit();
        doReturn(mock(ReadOnlyTransaction.class)).when(mockBroker).newReadOnlyTransaction();
        doReturn(mockTx).when(mockBroker).newReadWriteTransaction();
        doReturn(mockTx).when(mockBroker).newWriteOnlyTransaction();
        doReturn(mockTxChain).when(mockBroker).createTransactionChain(any(TransactionChainListener.class));
        doReturn(mock(ReadOnlyTransaction.class)).when(mockTxChain).newReadOnlyTransaction();
        doReturn(mockTx).when(mockTxChain).newReadWriteTransaction();
        doReturn(mockTx).when(mockTxChain).newWriteOnlyTransaction();
    }

    private static TranslateRegistryImpl getTranslateRegistry(DataBroker mockBroker) {
        TranslateRegistryImpl reg = new TranslateRegistryImpl(mockBroker);

        new GenericTranslateUnit(reg).init();
        new IosEssentialUnit(reg).init();
        new IosInterfaceUnit(reg).init();
        new BgpUnit(reg).init();
        new OspfUnit(reg).init();
        new LocalRoutingUnit(reg).init();
        new IosNetworkInstanceUnit(reg).init();
        new IosCdpUnit(reg).init();
        new LldpUnit(reg).init();
        return reg;
    }

    private static TipProducingDataTree getDataTree(SchemaContext ctx, TreeType configuration) {
        final TipProducingDataTree dataTree = InMemoryDataTreeFactory.getInstance().create(configuration);
        dataTree.setSchemaContext(ctx);
        return dataTree;
    }

    private static DOMDataBroker getDomBroker(SchemaContext schemaContext,
                                              DataBroker contextBroker,
                                              BindingNormalizedNodeSerializer serializer,
                                              ReaderRegistry readerRegistry,
                                              WriterRegistry writerRegistry,
                                              DataTree dataTree) {
        ModifiableDataManager modTree = new ModifiableDataTreeDelegator(
                serializer, dataTree, schemaContext, writerRegistry, contextBroker);
        ReadableDataManager readTree = new ReadableDataTreeDelegator(
                serializer, schemaContext, readerRegistry, contextBroker);

        return io.fd.honeycomb.data.impl.DataBroker.create(modTree, readTree);
    }

    @Ignore
    @Test
    public void getInterfacesBA() throws Exception {
        ReadOnlyTransaction readOnlyTransaction = bindingBroker.newReadOnlyTransaction();
        CheckedFuture<Optional<Interfaces>, ReadFailedException> read =
                readOnlyTransaction.read(LogicalDatastoreType.OPERATIONAL, IIDs.INTERFACES);
        Interfaces interfaces = read.checkedGet().get();

        System.err.println(interfaces);
    }

    /**
     * Get all data from the device. Can serve for basic testing but also for performance tuning.
     */
    @Ignore
    @Test
    public void getAllDOM() throws Exception {
        DOMDataReadOnlyTransaction readOnlyTransaction = domBroker.newReadOnlyTransaction();
        CheckedFuture<Optional<NormalizedNode<?, ?>>, ReadFailedException> read =
                readOnlyTransaction.read(LogicalDatastoreType.OPERATIONAL, YangInstanceIdentifier.EMPTY);
        NormalizedNode<?, ?> root = read.checkedGet().get();

        System.err.println(toJson(root));
    }

    @Ignore
    @Test
    public void getAllDOMBenchmark() throws Exception {
        benchmark(2, 100, () -> {
            try {
                getAllDOM();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void benchmark(int warmups, int rounds, Runnable code) throws Exception {
        long averageTime = 0;

        for (int i = 0; i < warmups; i++) {
            code.run();
        }

        for (int i = 0; i < rounds; i++) {
            Stopwatch stopwatch = Stopwatch.createStarted();

            code.run();

            stopwatch.stop();
            averageTime += stopwatch.elapsed(TimeUnit.MILLISECONDS);
        }

        System.err.println("Average execution time: " + averageTime / (rounds * 1.0) + " seconds");
    }

    private String toJson(NormalizedNode<?, ?> root) throws Exception {
        JSONCodecFactory codecFac = JSONCodecFactory.getShared(schemaCtx);
        StringWriter out = new StringWriter();

        try (final JsonWriter jsonWriter = new JsonWriter(out)) {
            jsonWriter.setIndent("  ");
            jsonWriter.beginObject();
            final NormalizedNodeWriter nnWriter = NormalizedNodeWriter.forStreamWriter(
                    JSONNormalizedNodeStreamWriter.createNestedWriter(codecFac, SchemaPath.ROOT, null, jsonWriter),
                    true);


            for (final NormalizedNode<?, ?> child : ((ContainerNode) root).getValue()) {
                nnWriter.write(child);
                nnWriter.flush();
            }
            jsonWriter.endObject();
            return out.toString();
        }
    }
}
