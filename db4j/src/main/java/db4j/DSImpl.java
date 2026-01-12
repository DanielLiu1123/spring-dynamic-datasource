package db4j;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.sql.DataSource;

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
        try (var conn = dataSource.getConnection()) {
            return fn.apply(new ConnImpl(this, conn));
        } catch (SQLException e) {
            throw new RuntimeException("Error obtaining connection from datasource " + name, e);
        } catch (Exception e) {
            throw new RuntimeException("Error executing connection callback for datasource " + name, e);
        }
    }

    @Override
    public <T> T tx(TxOptions opts, TxCallback<T> fn) {
        try (var conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            var tx = new TxImpl(this, opts);
            try {
                var result = fn.apply(tx);
                conn.commit();
                return result;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error obtaining connection from datasource " + name, e);
        } catch (Exception e) {
            throw new RuntimeException("Error executing transaction callback for datasource " + name, e);
        }
    }

    @Override
    public <C> C client(Class<C> clientType) throws IllegalStateException {
        var className = clientType.getName();
        var client = clientCache.computeIfAbsent(className, k -> {
            for (var resolver : DSImpl.this.clientResolvers) {
                if (resolver.supports(clientType)) {
                    return resolver.resolve(DSImpl.this.dataSource, clientType);
                }
            }
            throw new IllegalStateException("No client resolver for type " + className);
        });
        return clientType.cast(client);
    }

    void addClientResolver(ClientResolver resolver) {
        clientResolvers.add(resolver);
    }
}
