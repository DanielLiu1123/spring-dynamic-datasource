# db4j Requirements (Concise)

## Goal
- Simplify database operations and multi-datasource transactions.
- Avoid annotation-driven transactions that are hard to debug.
- Prefer explicit, controllable, and traceable execution.

## Entry and Registry
- `DB` is the entry point.
- `DB.ds(name)` returns a 

## Datasource Handle (DbRef)
- `withConn(fn)` runs a callback with a short-lived connection (no transaction).
- `inTx(opts, fn)` runs a callback in a transaction, auto-commit on success, rollback on exception.
- `mapper(type)` provides a non-transactional mapper (each call may use its own connection).

## Connection and Transaction
- `Conn` is bound to exactly one `DbRef`.
- `Conn.mapper(type)` returns a mapper bound to this connection.
- Optional `Conn.tag(key, value)` for logging/tracing metadata.
- `Tx` extends `Conn` with transactional lifecycle and metadata (`txId`, `TxOptions`).
- Optional `setRollbackOnly()` and `isRollbackOnly()`.

## Mapper Rebinding
- `ConnBound<T>.use(conn)` rebinds the same mapper type to another connection.

## Transaction Options
- `TxOptions`: `isolation`, `readOnly`, `timeout`.
- Default: READ_COMMITTED, 30 seconds timeout.

## Constraints and Errors
- Transactions are bound to a single datasource (no implicit cross-datasource work).
- Optional errors:
  - `ConnectionClosed` for use after close.
  - `CrossDbAccessInTx` for cross-datasource access inside a transaction.
