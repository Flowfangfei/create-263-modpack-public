package dev.simulated_team.simulated.multiloader.transfer;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** One journal per actual storage object, shared by every capability wrapper and routed endpoint.
 * Weak keys and weak values avoid retaining unloaded block entities through journal closures.
 * The transaction retains each active journal until it closes.
 */
public final class BackendTransactions {
    private static final WeakHashMap<Object, WeakReference<SnapshotJournal<?>>> JOURNALS = new WeakHashMap<>();
    private BackendTransactions() {}

    public static <S> void snapshot(Object backend, TransactionContext transaction,
            Supplier<S> capture, Consumer<S> restore, Runnable committed) {
        snapshot(backend, transaction, capture, restore, state -> committed.run());
    }

    @SuppressWarnings("unchecked")
    public static synchronized <S> void snapshot(Object backend, TransactionContext transaction,
            Supplier<S> capture, Consumer<S> restore, Consumer<S> committed) {
        var reference = JOURNALS.get(backend);
        SnapshotJournal<S> journal = reference == null ? null : (SnapshotJournal<S>) reference.get();
        if (journal == null) {
            journal = new SnapshotJournal<>() {
                @Override protected S createSnapshot() { return capture.get(); }
                @Override protected void revertToSnapshot(S state) { restore.accept(state); }
                @Override protected void onRootCommit(S state) { committed.accept(state); }
            };
            JOURNALS.put(backend, new WeakReference<>(journal));
        }
        journal.updateSnapshots(transaction);
    }
}
