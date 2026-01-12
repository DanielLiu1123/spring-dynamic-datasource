package db4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;

/**
 * A handle bound to one datasource (aka "DB").
 */
final class DSImpl implements DS {

    private final String name;
    private final DataSource dataSource;
    private final List<ClientResolver> clientResolvers = Collections.synchronizedList(new ArrayList<>());
    private final ConcurrentMap<String, Object> clientCache = new ConcurrentHashMap<>();

    DSImpl(String name, DataSource dataSource) {
        this.name = name;
        this.dataSource = dataSource;
    }

    @Override
    public <T> T conn(ConnCallback<T> fn) {
        try (var connection = dataSource.getConnection()) {
            return fn.apply(new ConnImpl(this, connection));
        } catch (SQLException e) {
            throw new RuntimeException("Error obtaining connection from datasource " + name, e);
        } catch (Exception e) {
            throw new RuntimeException("Error executing connection callback for datasource " + name, e);
        }
    }

    @Override
    public <T> T tx(TxOptions opts, TxCallback<T> fn) {
        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            var tx = new TxImpl(this, connection, opts);
            try {
                var result = fn.apply(tx);
                connection.commit();
                return result;
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error obtaining connection from datasource " + name, e);
        } catch (Exception e) {
            throw new RuntimeException("Error executing transaction callback for datasource " + name, e);
        }
    }

    @Override
    public <C> C client(Class<C> clientType) throws IllegalStateException {
        return client(null, clientType);
    }

    void addClientResolver(ClientResolver resolver) {
        clientResolvers.add(resolver);
    }

    <C> C client(@Nullable Connection connection, Class<C> clientType) throws IllegalStateException {
        if (connection != null) {
            return resolveClient(connection, clientType);
        }
        var className = clientType.getName();
        var client = clientCache.computeIfAbsent(className, k -> resolveClient(null, clientType));
        return clientType.cast(client);
    }

    private <C> C resolveClient(@Nullable Connection connection, Class<C> clientType) {
        for (var resolver : DSImpl.this.clientResolvers) {
            if (resolver.supports(clientType)) {
                return resolver.resolve(dataSource, connection, clientType);
            }
        }
        throw new IllegalStateException("No client resolver for type " + clientType.getName());
    }
}
