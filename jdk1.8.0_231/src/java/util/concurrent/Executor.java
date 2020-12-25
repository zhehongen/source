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

/**
 * An object that executes submitted {@link Runnable} tasks. This
 * interface provides a way of decoupling task submission from the
 * mechanics of how each task will be run, including details of thread
 * use, scheduling, etc.  An {@code Executor} is normally used
 * instead of explicitly creating threads. For example, rather than
 * invoking {@code new Thread(new(RunnableTask())).start()} for each
 * of a set of tasks, you might use:
 *
 * <pre>
 * Executor executor = <em>anExecutor</em>;
 * executor.execute(new RunnableTask1());
 * executor.execute(new RunnableTask2());
 * ...
 * </pre>
 *
 * However, the {@code Executor} interface does not strictly
 * require that execution be asynchronous. In the simplest case, an
 * executor can run the submitted task immediately in the caller's
 * thread:
 *
 *  <pre> {@code
 * class DirectExecutor implements Executor {
 *   public void execute(Runnable r) {
 *     r.run();
 *   }
 * }}</pre>
 *
 * More typically, tasks are executed in some thread other
 * than the caller's thread.  The executor below spawns a new thread
 * for each task.
 *
 *  <pre> {@code
 * class ThreadPerTaskExecutor implements Executor {
 *   public void execute(Runnable r) {
 *     new Thread(r).start();
 *   }
 * }}</pre>
 *
 * Many {@code Executor} implementations impose some sort of
 * limitation on how and when tasks are scheduled.  The executor below
 * serializes the submission of tasks to a second executor,
 * illustrating a composite executor.
 *
 *  <pre> {@code
 * class SerialExecutor implements Executor {
 *   final Queue<Runnable> tasks = new ArrayDeque<Runnable>();
 *   final Executor executor;
 *   Runnable active;
 *
 *   SerialExecutor(Executor executor) {
 *     this.executor = executor;
 *   }
 *
 *   public synchronized void execute(final Runnable r) {
 *     tasks.offer(new Runnable() {
 *       public void run() {
 *         try {
 *           r.run();
 *         } finally {
 *           scheduleNext();
 *         }
 *       }
 *     });
 *     if (active == null) {
 *       scheduleNext();
 *     }
 *   }
 *
 *   protected synchronized void scheduleNext() {
 *     if ((active = tasks.poll()) != null) {
 *       executor.execute(active);
 *     }
 *   }
 * }}</pre>
 *
 * The {@code Executor} implementations provided in this package
 * implement {@link ExecutorService}, which is a more extensive
 * interface.  The {@link ThreadPoolExecutor} class provides an
 * extensible thread pool implementation. The {@link Executors} class
 * provides convenient factory methods for these Executors.
 *
 * <p>Memory consistency effects: Actions in a thread prior to
 * submitting a {@code Runnable} object to an {@code Executor}
 * <a href="package-summary.html#MemoryVisibility"><i>happen-before</i></a>
 * its execution begins, perhaps in another thread.
 *
 * @since 1.5
 * @author Doug Lea
 */
public interface Executor {

    /**
     * Executes the given command at some time in the future.  The command
     * may execute in a new thread, in a pooled thread, or in the calling
     * thread, at the discretion of the {@code Executor} implementation.
     *在将来的某个时间执行给定的命令。 根据执行器的执行情况，命令可以在新线程，池线程或调用线程中执行。
     * @param command the runnable task
     * @throws RejectedExecutionException if this task cannot be
     * accepted for execution
     * @throws NullPointerException if command is null
     */
    void execute(Runnable command);

    /**
     * 执行提交的可运行任务的对象。该接口提供了一种将任务提交与每个任务的运行机制（包括线程使用，调度等详细信息）分离的方法。
     * 通常使用执行程序来代替显式创建线程。例如，可以为每个任务集调用新的Thread（new（RunnableTask（））.start（）而不是：
     Executor executor = anExecutor;
     executor.execute(new RunnableTask1());
     executor.execute(new RunnableTask2());
     *    ...
     *
     * 但是，Executor接口并不严格要求执行是异步的。在最简单的情况下，执行者可以立即在调用者的线程中运行提交的任务：
     *
     * class DirectExecutor implements Executor {
     *    public void execute(Runnable r) {
     *      r.run();
     *    }
     *  }
     * 更典型地，任务在调用者线程之外的某个线程中执行。下面的执行程序为每个任务生成一个新线程。
     *
     * class ThreadPerTaskExecutor implements Executor {
     *   public void execute(Runnable r) {
     *     new Thread(r).start();
     *   }
     * }
     * 许多执行器实现对计划任务的方式和时间施加了某种限制。下面的执行程序将任务提交序列化到第二个执行程序，说明了一个复合执行程序。
     *
     * class SerialExecutor implements Executor {
     *   final Queue<Runnable> tasks = new ArrayDeque<Runnable>();
     *   final Executor executor;
     *   Runnable active;
     *
     *   SerialExecutor(Executor executor) {
     *     this.executor = executor;
     *   }
     *
     *   public synchronized void execute(final Runnable r) {
     *     tasks.offer(new Runnable() {
     *       public void run() {
     *         try {
     *           r.run();
     *         } finally {
     *           scheduleNext();
     *         }
     *       }
     *     });
     *     if (active == null) {
     *       scheduleNext();
     *     }
     *   }
     *
     *   protected synchronized void scheduleNext() {
     *     if ((active = tasks.poll()) != null) {
     *       executor.execute(active);
     *     }
     *   }
     * }
     * 此程序包中提供的Executor实现实现了ExecutorService，这是一个更广泛的接口。 ThreadPoolExecutor类提供了可扩展的线程池实现。 Executors类为这些Executor提供了方便的工厂方法。
     * 内存一致性影响：在将Runnable对象提交给Executor之前，线程中的操作发生在其执行开始之前，也许是在另一个线程中。
     */
}
