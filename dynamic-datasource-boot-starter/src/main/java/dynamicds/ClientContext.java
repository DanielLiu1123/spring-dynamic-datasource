package dynamicds;

import org.springframework.transaction.PlatformTransactionManager;

/**
 * Context containing the client and its associated transaction manager.
 *
 * @param client             the client instance (e.g., MyBatis Mapper)
 * @param transactionManager the transaction manager associated with the client's data source
 */
record ClientContext(Object client, PlatformTransactionManager transactionManager) {}
