/*
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/*
 *
 *
 *
 *
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;
import java.util.concurrent.locks.LockSupport;

/**
 * A cancellable asynchronous computation.  This class provides a base
 * implementation of {@link Future}, with methods to start and cancel
 * a computation, query to see if the computation is complete, and
 * retrieve the result of the computation.  The result can only be
 * retrieved when the computation has completed; the {@code get}
 * methods will block if the computation has not yet completed.  Once
 * the computation has completed, the computation cannot be restarted
 * or cancelled (unless the computation is invoked using
 * {@link #runAndReset}).
 *
 * <p>A {@code FutureTask} can be used to wrap a {@link Callable} or
 * {@link Runnable} object.  Because {@code FutureTask} implements
 * {@code Runnable}, a {@code FutureTask} can be submitted to an
 * {@link Executor} for execution.
 *
 * <p>In addition to serving as a standalone class, this class provides
 * {@code protected} functionality that may be useful when creating
 * customized task classes.
 *           可取消的异步计算。此类提供Future的基本实现，其中包含启动和取消计算，查询以查看计算是否完成以及检索计算结果的方法。只有在计算完成后才能检索结果；如果计算尚未完成，则get方法将阻塞。一旦计算完成，就不能重新启动或取消计算（除非使用runAndReset调用计算）。
 * @since 1.5FutureTask可用于包装Callable或Runnable对象。由于FutureTask实现Runnable，因此FutureTask可以提交给执行程序以执行。
 * @author Doug Lea
 * @param <V> The result type returned by this FutureTask's {@code get} methods
 */
public class FutureTask<V> implements RunnableFuture<V> {
    /*
     * Revision notes: This differs from previous versions of this
     * class that relied on AbstractQueuedSynchronizer, mainly to
     * avoid surprising users about retaining interrupt status during
     * cancellation races. Sync control in the current design relies
     * on a "state" field updated via CAS to track completion, along
     * with a simple Treiber stack to hold waiting threads.
     *修订说明：这与依赖AbstractQueuedSynchronizer的此类的早期版本不同，主要是为了避免用户对取消比赛期间保留中断状态感到惊讶。 当前设计中的同步控制依赖于通过CAS更新的“状态”字段来跟踪完成情况，以及一个简单的Treiber堆栈来保存等待线程。 样式说明：与往常一样，我们绕过了使用AtomicXFieldUpdaters的开销，而是直接使用Unsafe内部函数。
     * Style note: As usual, we bypass overhead of using
     * AtomicXFieldUpdaters and instead directly use Unsafe intrinsics.
     */

    /**
     * The run state of this task, initially NEW.  The run state
     * transitions to a terminal state only in methods set,
     * setException, and cancel.  During completion, state may take on
     * transient values of COMPLETING (while outcome is being set) or
     * INTERRUPTING (only while interrupting the runner to satisfy a
     * cancel(true)). Transitions from these intermediate to final
     * states use cheaper ordered/lazy writes because values are unique
     * and cannot be further modified.
     *此任务的运行状态，最初为NEW。 运行状态仅在set，setException和cancel方法中转换为终端状态。 在完成期间，状态可能会采用COMPLETING（正在设置结果时）或INTERRUPTING（仅在中断跑步者满足cancel（true）时）的瞬态值。 从这些中间状态到最终状态的转换使用便宜的有序/惰性写入，因为值是唯一的，无法进一步修改。
     * Possible state transitions:
     * NEW -> COMPLETING -> NORMAL
     * NEW -> COMPLETING -> EXCEPTIONAL
     * NEW -> CANCELLED
     * NEW -> INTERRUPTING -> INTERRUPTED
     */
    private volatile int state;
    private static final int NEW          = 0;
    private static final int COMPLETING   = 1;
    private static final int NORMAL       = 2;
    private static final int EXCEPTIONAL  = 3;
    private static final int CANCELLED    = 4;
    private static final int INTERRUPTING = 5;
    private static final int INTERRUPTED  = 6;

    /** The underlying callable; nulled out after running 底层callable； 运行后消失*/
    private Callable<V> callable;
    /** The result to return or exception to throw from get() */
    private Object outcome; // non-volatile, protected by state reads/writes非易失性，受状态读/写保护
    /** The thread running the callable; CASed during run() */
    private volatile Thread runner;
    /** Treiber stack of waiting threads  Treiber等待线程堆栈    waitnode 1->2->3->4->5  */
    private volatile WaitNode waiters;//变量 callable 存储的是可执行的任务，变量 outcome 存储任务的返回值，变量 runner 指向当前执行该任务的线程，变量 waiters 指向等待链表的头节点。

    /**
     * Returns result or throws exception for completed task.
     *返回结果或引发已完成任务的异常。
     * @param s completed state value
     */
    @SuppressWarnings("unchecked")
    private V report(int s) throws ExecutionException {
        Object x = outcome;
        if (s == NORMAL)
            return (V)x;
        if (s >= CANCELLED)
            throw new CancellationException();
        throw new ExecutionException((Throwable)x);
    }

    /**
     * Creates a {@code FutureTask} that will, upon running, execute the
     * given {@code Callable}.
     *创建一个FutureTask，它将在运行时执行给定的Callable。
     * @param  callable the callable task
     * @throws NullPointerException if the callable is null
     */
    public FutureTask(Callable<V> callable) {
        if (callable == null)
            throw new NullPointerException();
        this.callable = callable;
        this.state = NEW;       // ensure visibility of callable 确保callable的可见性
    }

    /**
     * Creates a {@code FutureTask} that will, upon running, execute the
     * given {@code Runnable}, and arrange that {@code get} will return the
     * given result on successful completion.
     *创建一个FutureTask，它将在运行时执行给定的Runnable，并安排get将在成功完成后返回给定的结果。
     * @param runnable the runnable task
     * @param result the result to return on successful completion. If
     * you don't need a particular result, consider using
     * constructions of the form:成功完成后返回的结果。 如果不需要特定结果，请考虑使用以下形式的构造：Future <？> f = new FutureTask <Void>（runnable，null）
     * {@code Future<?> f = new FutureTask<Void>(runnable, null)}
     * @throws NullPointerException if the runnable is null
     */
    public FutureTask(Runnable runnable, V result) {
        this.callable = Executors.callable(runnable, result);
        this.state = NEW;       // ensure visibility of callable
    }

    public boolean isCancelled() {
        return state >= CANCELLED;
    }

    public boolean isDone() {
        return state != NEW;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        if (!(state == NEW &&//false.返回false代表没有取消成功。（只有状态为new并被修改为interrupting或canceled才代表取消成功，后续还要进行后续操作）
              UNSAFE.compareAndSwapInt(this, stateOffset, NEW,
                  mayInterruptIfRunning ? INTERRUPTING : CANCELLED)))
            return false;
        try {    // in case call to interrupt throws exception 在中断调用引发异常的情形
            if (mayInterruptIfRunning) {
                try {
                    Thread t = runner;
                    if (t != null)
                        t.interrupt();
                } finally { // final state
                    UNSAFE.putOrderedInt(this, stateOffset, INTERRUPTED);
                }
            }
        } finally {
            finishCompletion();
        }
        return true;
    }

    /**
     * @throws CancellationException {@inheritDoc}
     */
    public V get() throws InterruptedException, ExecutionException {
        int s = state;
        if (s <= COMPLETING)
            s = awaitDone(false, 0L);
        return report(s);
    }

    /**
     * @throws CancellationException {@inheritDoc}
     */
    public V get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        if (unit == null)
            throw new NullPointerException();
        int s = state;
        if (s <= COMPLETING &&
            (s = awaitDone(true, unit.toNanos(timeout))) <= COMPLETING)
            throw new TimeoutException();
        return report(s);
    }

    /**
     * Protected method invoked when this task transitions to state
     * {@code isDone} (whether normally or via cancellation). The
     * default implementation does nothing.  Subclasses may override
     * this method to invoke completion callbacks or perform
     * bookkeeping. Note that you can query status inside the
     * implementation of this method to determine whether this task
     * has been cancelled.当此任务转换为状态isDone时调用的受保护方法（无论正常还是通过取消）。 默认实现不执行任何操作。 子类可以重写此方法以调用完成回调或执行簿记。 请注意，您可以在此方法的实现内部查询状态，以确定此任务是否已取消。
     */
    protected void done() { }

    /**
     * Sets the result of this future to the given value unless
     * this future has already been set or has been cancelled.除非已经设置或取消了该未来，否则将此未来的结果设置为给定值。
     *成功完成计算后，run方法会在内部调用此方法。
     * <p>This method is invoked internally by the {@link #run} method
     * upon successful completion of the computation.
     *
     * @param v the value
     */
    protected void set(V v) {
        if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
            outcome = v;
            UNSAFE.putOrderedInt(this, stateOffset, NORMAL); // final state
            finishCompletion();// 任务完成唤醒挂起的线程
        }
    }

    /**
     * Causes this future to report an {@link ExecutionException}
     * with the given throwable as its cause, unless this future has
     * already been set or has been cancelled.
     *
     * <p>This method is invoked internally by the {@link #run} method
     * upon failure of the computation.
     *
     * @param t the cause of failure
     */
    protected void setException(Throwable t) {
        if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
            outcome = t;
            UNSAFE.putOrderedInt(this, stateOffset, EXCEPTIONAL); // final state
            finishCompletion();
        }
    }

    public void run() {
        if (state != NEW ||     //如果不是NEW说明执行完毕        只有状态 state 为 NEW， runner 为空的情况下才可以执行
            !UNSAFE.compareAndSwapObject(this, runnerOffset,//设置runner为当前线程
                                         null, Thread.currentThread())) //通过cas将当前线程指定给runner。这里可以防止callable被执行多次
            return;
        try {
            Callable<V> c = callable;
            if (c != null && state == NEW) {
                V result;
                boolean ran;//是否执行成功
                try {
                    result = c.call();//调用我们实现的callable中的call方法
                    ran = true;
                } catch (Throwable ex) {
                    result = null;
                    ran = false;
                    setException(ex);
                }
                if (ran)
                    set(result);
            }
        } finally {
            // runner must be non-null until state is settled to
            // prevent concurrent calls to run() runner必须为非null，直到解决状态为止，以防止同时调用run（）
            runner = null;
            // state must be re-read after nulling runner to prevent
            // leaked interrupts 清空runner后必须重新读取状态，以防止泄漏中断
            int s = state;
            if (s >= INTERRUPTING)
                handlePossibleCancellationInterrupt(s);
        }
    }

    /**
     * Executes the computation without setting its result, and then
     * resets this future to initial state, failing to do so if the
     * computation encounters an exception or is cancelled.  This is
     * designed for use with tasks that intrinsically execute more
     * than once.
     *
     * @return {@code true} if successfully run and reset
     */
    protected boolean runAndReset() {
        if (state != NEW ||
            !UNSAFE.compareAndSwapObject(this, runnerOffset,
                                         null, Thread.currentThread()))
            return false;
        boolean ran = false;
        int s = state;
        try {
            Callable<V> c = callable;
            if (c != null && s == NEW) {
                try {
                    c.call(); // don't set result
                    ran = true;
                } catch (Throwable ex) {
                    setException(ex);
                }
            }
        } finally {
            // runner must be non-null until state is settled to
            // prevent concurrent calls to run()
            runner = null;
            // state must be re-read after nulling runner to prevent
            // leaked interrupts
            s = state;
            if (s >= INTERRUPTING)
                handlePossibleCancellationInterrupt(s);
        }
        return ran && s == NEW;
    }

    /**
     * Ensures that any interrupt from a possible cancel(true) is only
     * delivered to a task while in run or runAndReset.确保来自可能的cancel（true）的任何中断仅在运行或runAndReset时才传递给任务。
     */
    private void handlePossibleCancellationInterrupt(int s) {
        // It is possible for our interrupter to stall before getting a
        // chance to interrupt us.  Let's spin-wait patiently.我们的灭弧室可能会失速，然后才有机会打断我们。 让我们耐心等待。
        if (s == INTERRUPTING)
            while (state == INTERRUPTING)
                Thread.yield(); // wait out pending interrupt 等待挂起的中断

        // assert state == INTERRUPTED;

        // We want to clear any interrupt we may have received from
        // cancel(true).  However, it is permissible to use interrupts
        // as an independent mechanism for a task to communicate with
        // its caller, and there is no way to clear only the
        // cancellation interrupt.
        //我们想清除从cancel（true）收到的所有中断。 但是，允许将中断用作任务与其调用者进行通信的独立机制，并且无法清除cancellation中断。
        // Thread.interrupted();
    }

    /**
     * Simple linked list nodes to record waiting threads in a Treiber
     * stack.  See other classes such as Phaser and SynchronousQueue
     * for more detailed explanation.简单的链表节点可将等待线程记录在Treiber堆栈中。 有关更多详细说明，请参见其他类，例如Phaser和SynchronousQueue。
     */
    static final class WaitNode {
        volatile Thread thread;
        volatile WaitNode next;
        WaitNode() { thread = Thread.currentThread(); }
    }

    /**
     * Removes and signals all waiting threads, invokes done(), and
     * nulls out callable.删除并发出信号给所有等待线程，调用done()，并使callable无效。
     */
    private void finishCompletion() {
        // assert state > COMPLETING;
        for (WaitNode q; (q = waiters) != null;) { //waitnode 1->2->3->4->5
            if (UNSAFE.compareAndSwapObject(this, waitersOffset, q, null)) {// 将 waiters 重置为空
                for (;;) {// 采用死循环的方式唤醒挂起的线程
                    Thread t = q.thread;// 获取等待节点关联的线程
                    if (t != null) {
                        q.thread = null;
                        LockSupport.unpark(t);//唤醒被park的线程
                    }
                    WaitNode next = q.next; // 获取等待链表的下一个节点继续唤醒
                    if (next == null)
                        break; // 节点为空的时候 跳出循环
                    q.next = null; // unlink to help gc
                    q = next;
                }
                break;
            }
        }

        done();//可以重写此方法

        callable = null;        // to reduce footprint减少足迹
    }

    /**
     * Awaits completion or aborts on interrupt or timeout.
     * 等待完成或因中断或超时而中止
     * @param timed true if use timed waits
     * @param nanos time to wait, if timed
     * @return state upon completion
     */
    private int awaitDone(boolean timed, long nanos)
        throws InterruptedException {
        final long deadline = timed ? System.nanoTime() + nanos : 0L;
        WaitNode q = null;
        boolean queued = false;
        for (;;) {
            if (Thread.interrupted()) {
                removeWaiter(q);
                throw new InterruptedException();
            }

            int s = state;
            if (s > COMPLETING) {//已经完成call操作，直接返回当前state
                if (q != null)
                    q.thread = null;
                return s;
            }
            else if (s == COMPLETING) // cannot time out yet 还不能超时
                Thread.yield(); //自旋的过程，此时还没有完成赋值操作，愿意让出cpu
            else if (q == null)
                q = new WaitNode();//第一次进来
            else if (!queued)//其它线程再来执行get操作时，将waitnode的next指向新线程？？
                queued = UNSAFE.compareAndSwapObject(this, waitersOffset,//waitnode 1->2->3->4->5
                                                     q.next = waiters, q);//原来是waiters，现在设置为左移一位     若未加入等待链表时，将 q 的 next 指向 waiters , 然后将 waiters 移动到 q
            else if (timed) {
                nanos = deadline - System.nanoTime();
                if (nanos <= 0L) {
                    removeWaiter(q);// 超过等待时长 将等待节点移除
                    return state;
                }
                LockSupport.parkNanos(this, nanos);
            }
            else
                LockSupport.park(this);//当任务执行完 执行 finishCompletion 是会被唤醒
        }
    }

    /**
     * Tries to unlink a timed-out or interrupted wait node to avoid尝试取消链接超时或中断的等待节点，以避免积累垃圾。
     * accumulating garbage.  Internal nodes are simply unspliced内部节点在没有CAS的情况下根本不会被拼接，因为如果释放者无论如何都要遍历内部节点，这是无害的。
     * without CAS since it is harmless if they are traversed anyway
     * by releasers.  To avoid effects of unsplicing from already为了避免从已删除的节点取消拆分的影响，在出现明显竞争的情况下会重新遍历该列表。
     * removed nodes, the list is retraversed in case of an apparent
     * race.  This is slow when there are a lot of nodes, but we don't
     * expect lists to be long enough to outweigh higher-overhead
     * schemes.   当节点很多时，这很慢，但是我们不希望列表足够长以超过开销更高的方案。
     */
    private void removeWaiter(WaitNode node) {
        if (node != null) {//waitnode 1->2->3->4->5
            node.thread = null;
            retry:
            for (;;) {          // restart on removeWaiter race 重新启动在removeWaiter出现竞争的情况
                for (WaitNode pred = null, q = waiters, s; q != null; q = s) {
                    s = q.next;//s代表下一个node
                    if (q.thread != null)
                        pred = q;//?
                    else if (pred != null) {
                        pred.next = s;
                        if (pred.thread == null) // check for race
                            continue retry;
                    }
                    else if (!UNSAFE.compareAndSwapObject(this, waitersOffset,
                                                          q, s))//waiters等于next，即右移一个节点
                        continue retry;
                }
                break;
            }
        }
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long stateOffset;//啥
    private static final long runnerOffset;
    private static final long waitersOffset;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = FutureTask.class;
            stateOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("state"));
            runnerOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("runner"));
            waitersOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("waiters"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

}
/**
 * waitnode 1->2->3->4->5
 *
 设置 runner 为当前线程
 回调 callable
 设置状态 state 为 COMPLETING
 设置返回结果 outcome
 设置状态 state 为 NORMAL
 唤醒等待链表 waiters 里的线程

 *
 *
 */
