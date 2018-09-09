package io.socket.socketio.server;

import io.socket.engineio.server.EngineIoServer;
import io.socket.parser.Packet;
import io.socket.parser.Parser;
import org.json.JSONArray;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;

public final class SocketIoNamespaceTest {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void test_constructor_new_instance() {
        final SocketIoServer server = Mockito.spy(new SocketIoServer(new EngineIoServer()));

        final SocketIoNamespace namespace = new SocketIoNamespace(server, "/_foo");

        Mockito.verify(server, Mockito.times(1)).getAdapterFactory();
        assertEquals("/_foo", namespace.getName());
        assertEquals(server, namespace.getServer());
        assertNotNull(namespace.getAdapter());
        assertEquals(0, namespace.getConnectedSockets().size());
    }

    @Test
    public void test_broadcast_all_rooms() {
        final SocketIoAdapter adapter = Mockito.mock(SocketIoAdapter.class);
        Mockito.doCallRealMethod().when(adapter)
                .broadcast(Mockito.any(Packet.class), Mockito.<String[]>isNull());
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                final Packet packet = invocationOnMock.getArgument(0);
                final String[] rooms = invocationOnMock.getArgument(1);
                final String[] socketsExcluded = invocationOnMock.getArgument(2);

                assertEquals(Parser.EVENT, packet.type);
                assertNotNull(packet.data);
                assertEquals(JSONArray.class, packet.data.getClass());
                assertEquals(1, ((JSONArray) packet.data).length());
                assertEquals("foo", ((JSONArray) packet.data).get(0));

                assertNull(rooms);
                assertNull(socketsExcluded);

                return null;
            }
        }).when(adapter).broadcast(Mockito.any(Packet.class), Mockito.<String[]>isNull(), Mockito.<String[]>isNull());

        final SocketIoAdapter.AdapterFactory adapterFactory = Mockito.mock(SocketIoAdapter.AdapterFactory.class);
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) {
                return adapter;
            }
        }).when(adapterFactory).createAdapter(Mockito.any(SocketIoNamespace.class));

        final SocketIoServer server = Mockito.spy(new SocketIoServer(
                new EngineIoServer(),
                SocketIoServerOptions.newFromDefault()
                        .setAdapterFactory(adapterFactory)));
        final SocketIoNamespace namespace = server.namespace("/");

        namespace.broadcast(null, "foo");
        Mockito.verify(adapter, Mockito.times(1))
                .broadcast(Mockito.any(Packet.class), Mockito.<String[]>isNull(), Mockito.<String[]>isNull());
    }

    @Test
    public void test_broadcast_one_room() {
        final SocketIoAdapter adapter = Mockito.mock(SocketIoAdapter.class);
        Mockito.doCallRealMethod().when(adapter)
                .broadcast(Mockito.any(Packet.class), Mockito.any(String[].class));
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                final Packet packet = invocationOnMock.getArgument(0);
                final String[] rooms = invocationOnMock.getArgument(1);
                final String[] socketsExcluded = invocationOnMock.getArgument(2);

                assertEquals(Parser.EVENT, packet.type);
                assertNotNull(packet.data);
                assertEquals(JSONArray.class, packet.data.getClass());
                assertEquals(1, ((JSONArray) packet.data).length());
                assertEquals("bar", ((JSONArray) packet.data).get(0));

                assertNotNull(rooms);
                assertEquals(1, rooms.length);
                assertEquals("foo", rooms[0]);

                assertNull(socketsExcluded);

                return null;
            }
        }).when(adapter).broadcast(Mockito.any(Packet.class), Mockito.any(String[].class), Mockito.<String[]>isNull());

        final SocketIoAdapter.AdapterFactory adapterFactory = Mockito.mock(SocketIoAdapter.AdapterFactory.class);
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) {
                return adapter;
            }
        }).when(adapterFactory).createAdapter(Mockito.any(SocketIoNamespace.class));

        final SocketIoServer server = Mockito.spy(new SocketIoServer(
                new EngineIoServer(),
                SocketIoServerOptions.newFromDefault()
                        .setAdapterFactory(adapterFactory)));
        final SocketIoNamespace namespace = server.namespace("/");

        namespace.broadcast("foo", "bar");
        Mockito.verify(adapter, Mockito.times(1))
                .broadcast(Mockito.any(Packet.class), Mockito.any(String[].class), Mockito.<String[]>isNull());
    }

    @Test
    public void test_broadcast_multiple_rooms() {
        final SocketIoAdapter adapter = Mockito.mock(SocketIoAdapter.class);
        Mockito.doCallRealMethod().when(adapter)
                .broadcast(Mockito.any(Packet.class), Mockito.any(String[].class));
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                final Packet packet = invocationOnMock.getArgument(0);
                final String[] rooms = invocationOnMock.getArgument(1);
                final String[] socketsExcluded = invocationOnMock.getArgument(2);

                assertEquals(Parser.EVENT, packet.type);
                assertNotNull(packet.data);
                assertEquals(JSONArray.class, packet.data.getClass());
                assertEquals(1, ((JSONArray) packet.data).length());
                assertEquals("baz", ((JSONArray) packet.data).get(0));

                assertNotNull(rooms);
                assertArrayEquals(new String[] { "foo", "bar" }, rooms);

                assertNull(socketsExcluded);

                return null;
            }
        }).when(adapter).broadcast(Mockito.any(Packet.class), Mockito.any(String[].class), Mockito.<String[]>isNull());

        final SocketIoAdapter.AdapterFactory adapterFactory = Mockito.mock(SocketIoAdapter.AdapterFactory.class);
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) {
                return adapter;
            }
        }).when(adapterFactory).createAdapter(Mockito.any(SocketIoNamespace.class));

        final SocketIoServer server = Mockito.spy(new SocketIoServer(
                new EngineIoServer(),
                SocketIoServerOptions.newFromDefault()
                        .setAdapterFactory(adapterFactory)));
        final SocketIoNamespace namespace = server.namespace("/");

        namespace.broadcast(new String[] { "foo", "bar" }, new Object[] { "baz" });
        Mockito.verify(adapter, Mockito.times(1))
                .broadcast(Mockito.any(Packet.class), Mockito.any(String[].class), Mockito.<String[]>isNull());
    }
}