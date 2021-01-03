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

package java.util.concurrent.locks;
import java.util.concurrent.TimeUnit;

/**
 * {@code Lock} implementations provide more extensive locking
 * operations than can be obtained using {@code synchronized} methods
 * and statements.  They allow more flexible structuring, may have
 * quite different properties, and may support multiple associated
 * {@link Condition} objects.
 *
 * <p>A lock is a tool for controlling access to a shared resource by
 * multiple threads. Commonly, a lock provides exclusive access to a
 * shared resource: only one thread at a time can acquire the lock and
 * all access to the shared resource requires that the lock be
 * acquired first. However, some locks may allow concurrent access to
 * a shared resource, such as the read lock of a {@link ReadWriteLock}.
 *
 * <p>The use of {@code synchronized} methods or statements provides
 * access to the implicit monitor lock associated with every object, but
 * forces all lock acquisition and release to occur in a block-structured way:
 * when multiple locks are acquired they must be released in the opposite
 * order, and all locks must be released in the same lexical scope in which
 * they were acquired.
 *
 * <p>While the scoping mechanism for {@code synchronized} methods
 * and statements makes it much easier to program with monitor locks,
 * and helps avoid many common programming errors involving locks,
 * there are occasions where you need to work with locks in a more
 * flexible way. For example, some algorithms for traversing
 * concurrently accessed data structures require the use of
 * &quot;hand-over-hand&quot; or &quot;chain locking&quot;: you
 * acquire the lock of node A, then node B, then release A and acquire
 * C, then release B and acquire D and so on.  Implementations of the
 * {@code Lock} interface enable the use of such techniques by
 * allowing a lock to be acquired and released in different scopes,
 * and allowing multiple locks to be acquired and released in any
 * order.
 *
 * <p>With this increased flexibility comes additional
 * responsibility. The absence of block-structured locking removes the
 * automatic release of locks that occurs with {@code synchronized}
 * methods and statements. In most cases, the following idiom
 * should be used:
 *
 *  <pre> {@code
 * Lock l = ...;
 * l.lock();
 * try {
 *   // access the resource protected by this lock
 * } finally {
 *   l.unlock();
 * }}</pre>
 *
 * When locking and unlocking occur in different scopes, care must be
 * taken to ensure that all code that is executed while the lock is
 * held is protected by try-finally or try-catch to ensure that the
 * lock is released when necessary.
 *
 * <p>{@code Lock} implementations provide additional functionality
 * over the use of {@code synchronized} methods and statements by
 * providing a non-blocking attempt to acquire a lock ({@link
 * #tryLock()}), an attempt to acquire the lock that can be
 * interrupted ({@link #lockInterruptibly}, and an attempt to acquire
 * the lock that can timeout ({@link #tryLock(long, TimeUnit)}).
 *
 * <p>A {@code Lock} class can also provide behavior and semantics
 * that is quite different from that of the implicit monitor lock,
 * such as guaranteed ordering, non-reentrant usage, or deadlock
 * detection. If an implementation provides such specialized semantics
 * then the implementation must document those semantics.
 *
 * <p>Note that {@code Lock} instances are just normal objects and can
 * themselves be used as the target in a {@code synchronized} statement.
 * Acquiring the
 * monitor lock of a {@code Lock} instance has no specified relationship
 * with invoking any of the {@link #lock} methods of that instance.
 * It is recommended that to avoid confusion you never use {@code Lock}
 * instances in this way, except within their own implementation.
 *
 * <p>Except where noted, passing a {@code null} value for any
 * parameter will result in a {@link NullPointerException} being
 * thrown.
 *
 * <h3>Memory Synchronization</h3>
 *
 * <p>All {@code Lock} implementations <em>must</em> enforce the same
 * memory synchronization semantics as provided by the built-in monitor
 * lock, as described in
 * <a href="https://docs.oracle.com/javase/specs/jls/se7/html/jls-17.html#jls-17.4">
 * The Java Language Specification (17.4 Memory Model)</a>:
 * <ul>
 * <li>A successful {@code lock} operation has the same memory
 * synchronization effects as a successful <em>Lock</em> action.
 * <li>A successful {@code unlock} operation has the same
 * memory synchronization effects as a successful <em>Unlock</em> action.
 * </ul>
 *
 * Unsuccessful locking and unlocking operations, and reentrant
 * locking/unlocking operations, do not require any memory
 * synchronization effects.
 *
 * <h3>Implementation Considerations</h3>
 *
 * <p>The three forms of lock acquisition (interruptible,
 * non-interruptible, and timed) may differ in their performance
 * characteristics, ordering guarantees, or other implementation
 * qualities.  Further, the ability to interrupt the <em>ongoing</em>
 * acquisition of a lock may not be available in a given {@code Lock}
 * class.  Consequently, an implementation is not required to define
 * exactly the same guarantees or semantics for all three forms of
 * lock acquisition, nor is it required to support interruption of an
 * ongoing lock acquisition.  An implementation is required to clearly
 * document the semantics and guarantees provided by each of the
 * locking methods. It must also obey the interruption semantics as
 * defined in this interface, to the extent that interruption of lock
 * acquisition is supported: which is either totally, or only on
 * method entry.
 *
 * <p>As interruption generally implies cancellation, and checks for
 * interruption are often infrequent, an implementation can favor responding
 * to an interrupt over normal method return. This is true even if it can be
 * shown that the interrupt occurred after another action may have unblocked
 * the thread. An implementation should document this behavior.
 *
 * @see ReentrantLock
 * @see Condition
 * @see ReadWriteLock
 *
 * @since 1.5
 * @author Doug Lea
 */
public interface Lock {

    /**
     * Acquires the lock.
     *
     * <p>If the lock is not available then the current thread becomes
     * disabled for thread scheduling purposes and lies dormant until the
     * lock has been acquired.
     * 如果该锁不可用，则出于线程调度目的，当前线程将被禁用，并处于休眠状态，直到获得该锁为止。
     * <p><b>Implementation Considerations</b>
     * 锁实现可能能够检测到锁的错误使用，例如可能导致死锁的调用，并且在这种情况下可能引发（未经检查的）异常。 该Lock实现必须记录情况和异常类型。
     * <p>A {@code Lock} implementation may be able to detect erroneous use
     * of the lock, such as an invocation that would cause deadlock, and
     * may throw an (unchecked) exception in such circumstances.  The
     * circumstances and the exception type must be documented by that
     * {@code Lock} implementation.
     */
    void lock();

    /**
     * Acquires the lock unless the current thread is                             除非当前线程被中断，否则获取锁。
     * {@linkplain Thread#interrupt interrupted}.                                 获取锁（如果有）并立即返回。
     *                                                                            如果该锁不可用，则出于线程调度目的，当前线程将被禁用，并在发生以下两种情况之一之前处于休眠状态：
     * <p>Acquires the lock if it is available and returns immediately.           该锁由当前线程获取；要么
     *                                                                            其他一些线程中断当前线程，并支持锁定获取的中断。
     * <p>If the lock is not available then the current thread becomes            如果当前线程：
     * disabled for thread scheduling purposes and lies dormant until             在进入此方法时已设置其中断状态；要么
     * one of two things happens:                                                 获取锁时被中断，并且支持中断获取锁，
     *                                                                            然后抛出InterruptedException并清除当前线程的中断状态。
     * <ul>                                                                       实施注意事项
     * <li>The lock is acquired by the current thread; or                         在某些实现中，中断锁获取的能力可能是不可能的，并且如果可能的话可能是昂贵的操作。程序员应意识到可能是这种情况。在这种情况下，实现应记录在案。
     * <li>Some other thread {@linkplain Thread#interrupt interrupts} the         与正常方法返回相比，实现可能更喜欢对中断做出响应。
     * current thread, and interruption of lock acquisition is supported.         锁实现可能能够检测到锁的错误使用，例如可能导致死锁的调用，并且在这种情况下可能引发（未经检查的）异常。该Lock实现必须记录情况和异常类型。
     * </ul>
     *
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while acquiring the
     * lock, and interruption of lock acquisition is supported,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     *
     * <p><b>Implementation Considerations</b>
     *
     * <p>The ability to interrupt a lock acquisition in some
     * implementations may not be possible, and if possible may be an
     * expensive operation.  The programmer should be aware that this
     * may be the case. An implementation should document when this is
     * the case.
     *
     * <p>An implementation can favor responding to an interrupt over
     * normal method return.
     *
     * <p>A {@code Lock} implementation may be able to detect
     * erroneous use of the lock, such as an invocation that would
     * cause deadlock, and may throw an (unchecked) exception in such
     * circumstances.  The circumstances and the exception type must
     * be documented by that {@code Lock} implementation.
     *
     * @throws InterruptedException if the current thread is
     *         interrupted while acquiring the lock (and interruption
     *         of lock acquisition is supported)
     */
    void lockInterruptibly() throws InterruptedException;

    /**
     * Acquires the lock only if it is free at the time of invocation.         仅在调用时释放锁时才获取锁。
     *                                                                         获取锁（如果有）并立即返回true值。 如果锁不可用，则此方法将立即返回false值。
     * <p>Acquires the lock if it is available and returns immediately         该方法的典型用法是：
     * with the value {@code true}.                                            这种用法可确保在获取锁后将其解锁，并且在未获取锁时不会尝试解锁。
     * If the lock is not available then this method will return
     * immediately with the value {@code false}.
     *
     * <p>A typical usage idiom for this method would be:
     *  <pre> {@code
     * Lock lock = ...;
     * if (lock.tryLock()) {
     *   try {
     *     // manipulate protected state
     *   } finally {
     *     lock.unlock();
     *   }
     * } else {
     *   // perform alternative actions
     * }}</pre>
     *
     * This usage ensures that the lock is unlocked if it was acquired, and
     * doesn't try to unlock if the lock was not acquired.
     *
     * @return {@code true} if the lock was acquired and
     *         {@code false} otherwise
     */
    boolean tryLock();

    /**
     * Acquires the lock if it is free within the given waiting time and the        如果锁在给定的等待时间内是空闲的，并且当前线程尚未中断，则获取该锁。
     * current thread has not been {@linkplain Thread#interrupt interrupted}.       如果锁可用，则此方法立即返回true值。如果该锁不可用，则出于线程调度目的，当前线程将被禁用，并在发生以下三种情况之一之前处于休眠状态：
     *                                                                              该锁由当前线程获取；要么
     * <p>If the lock is available this method returns immediately                  其他一些线程中断当前线程，并支持中断获取锁。要么
     * with the value {@code true}.                                                 经过指定的等待时间
     * If the lock is not available then                                            如果获得了锁，则返回值true。
     * the current thread becomes disabled for thread scheduling                    如果当前线程：
     * purposes and lies dormant until one of three things happens:                 在进入此方法时已设置其中断状态；要么
     * <ul>                                                                         获取锁时被中断，并且支持中断获取锁，
     * <li>The lock is acquired by the current thread; or                           然后抛出InterruptedException并清除当前线程的中断状态。
     * <li>Some other thread {@linkplain Thread#interrupt interrupts} the           如果经过了指定的等待时间，则返回值false。如果时间小于或等于零，则该方法将根本不等待。
     * current thread, and interruption of lock acquisition is supported; or        实施注意事项
     * <li>The specified waiting time elapses                                       在某些实现中，中断锁获取的能力可能是不可能的，并且如果可能的话可能是昂贵的操作。程序员应意识到可能是这种情况。在这种情况下，实现应记录在案。
     * </ul>                                                                        与正常方法返回或报告超时相比，实现可能更喜欢对中断做出响应。
     *                                                                              锁实现可能能够检测到锁的错误使用，例如可能导致死锁的调用，并且在这种情况下可能引发（未经检查的）异常。该Lock实现必须记录情况和异常类型。
     * <p>If the lock is acquired then the value {@code true} is returned.
     *
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while acquiring
     * the lock, and interruption of lock acquisition is supported,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     *
     * <p>If the specified waiting time elapses then the value {@code false}
     * is returned.
     * If the time is
     * less than or equal to zero, the method will not wait at all.
     *
     * <p><b>Implementation Considerations</b>
     *
     * <p>The ability to interrupt a lock acquisition in some implementations
     * may not be possible, and if possible may
     * be an expensive operation.
     * The programmer should be aware that this may be the case. An
     * implementation should document when this is the case.
     *
     * <p>An implementation can favor responding to an interrupt over normal
     * method return, or reporting a timeout.
     *
     * <p>A {@code Lock} implementation may be able to detect
     * erroneous use of the lock, such as an invocation that would cause
     * deadlock, and may throw an (unchecked) exception in such circumstances.
     * The circumstances and the exception type must be documented by that
     * {@code Lock} implementation.
     *
     * @param time the maximum time to wait for the lock
     * @param unit the time unit of the {@code time} argument
     * @return {@code true} if the lock was acquired and {@code false}
     *         if the waiting time elapsed before the lock was acquired
     *
     * @throws InterruptedException if the current thread is interrupted
     *         while acquiring the lock (and interruption of lock
     *         acquisition is supported)
     */
    boolean tryLock(long time, TimeUnit unit) throws InterruptedException;

    /**
     * Releases the lock.
     *
     * <p><b>Implementation Considerations</b>
     *锁实现通常会限制哪些线程可以释放锁（通常只有锁的持有者才能释放锁），并且如果违反该限制，则可能引发（未经检查的）异常。 任何限制和异常类型都必须由那个Lock实现记录下来。
     * <p>A {@code Lock} implementation will usually impose
     * restrictions on which thread can release a lock (typically only the
     * holder of the lock can release it) and may throw
     * an (unchecked) exception if the restriction is violated.
     * Any restrictions and the exception
     * type must be documented by that {@code Lock} implementation.
     */
    void unlock();

    /**
     * Returns a new {@link Condition} instance that is bound to this
     * {@code Lock} instance.返回绑定到此Lock实例的新Condition实例。
     *
     * <p>Before waiting on the condition the lock must be held by the
     * current thread.在等待该条件之前，该锁必须由当前线程持有。 调用Condition.await（）会在等待之前自动释放锁，并在等待返回之前重新获取该锁。
     * A call to {@link Condition#await()} will atomically release the lock
     * before waiting and re-acquire the lock before the wait returns.
     *
     * <p><b>Implementation Considerations</b>
     *
     * <p>The exact operation of the {@link Condition} instance depends on
     * the {@code Lock} implementation and must be documented by that
     * implementation.
     *Condition实例的确切操作取决于Lock实现，并且必须由该实现记录。
     * @return A new {@link Condition} instance for this {@code Lock} instance
     * @throws UnsupportedOperationException if this {@code Lock}
     *         implementation does not support conditions
     */
    Condition newCondition();
}
/**
 * 与使用同步方法和语句相比，锁实现提供了更广泛的锁操作。它们允许更灵活的结构，可以具有完全不同的属性，并且可以支持多个关联的Condition对象。
 * 锁是一种用于控制多个线程对共享资源的访问的工具。通常，锁提供对共享资源的独占访问：一次只能有一个线程可以获取该锁，而对共享资源的所有访问都需要首先获取该锁。但是，某些锁可能允许并发访问共享资源，例如ReadWriteLock的读取锁。
 * 使用同步方法或语句可访问与每个对象关联的隐式监视器锁，但会强制所有锁的获取和释放以块结构方式进行：当获取多个锁时，它们必须以相反的顺序释放，并且所有锁必须在获得它们的相同词汇范围内释放。
 * 尽管用于同步方法和语句的作用域机制使使用监视器锁的编程变得更加容易，并有助于避免许多常见的涉及锁的编程错误，但在某些情况下，您需要以更灵活的方式使用锁。例如，某些用于遍历并发访问的数据结构的算法需要使用“移交”或“链锁”：您获取节点A的锁，然后获取节点B的锁，然后释放A并获取C，然后释放B并获得D等。 Lock接口的实现通过允许在不同范围内获取和释放锁，并允许以任意顺序获取和释放多个锁，从而启用了此类技术。
 * 灵活性的提高带来了额外的责任。缺少块结构锁定将消除在同步方法和语句中自动释放的锁定。在大多数情况下，应使用以下惯用法：

 * 当锁定和解锁发生在不同的范围内时，必须小心确保通过try-finally或try-catch保护持有锁定时执行的所有代码，以确保在必要时释放锁定。
 * 锁实现通过提供获取锁的非阻塞尝试（tryLock（）），获取可被中断的锁的尝试（lockInterruptible，以及获取锁的尝试），提供了比同步方法和语句更多的功能。可能会超时（tryLock（long，TimeUnit））。
 * Lock类还可以提供与隐式监视器锁定完全不同的行为和语义，例如保证顺序，不可重用或死锁检测。如果实现提供了这种特殊的语义，则实现必须记录这些语义。
 * 请注意，Lock实例只是普通对象，它们本身可以用作同步语句中的目标。获取Lock实例的监视器锁与调用该实例的任何锁方法没有指定的关系。建议避免混淆，除非在自己的实现中使用，否则不要以这种方式使用Lock实例。
 * 除非另有说明，否则为任何参数传递null值都将引发NullPointerException。
 * 内存同步
 * 所有锁实现必须强制执行与内置监视器锁所提供的相同的内存同步语义，如Java语言规范（17.4内存模型）中所述：
 * 成功的锁定操作与成功的锁定操作具有相同的内存同步效果。
 * 成功的解锁操作与成功的解锁操作具有相同的内存同步效果。
 * 不成功的锁定和解锁操作以及可重入的锁定/解锁操作不需要任何内存同步效果。
 * 实施注意事项
 * 锁获取的三种形式（可中断，不可中断和定时）在其性能特征，订购保证或其他实现质量上可能有所不同。此外，在给定的Lock类中，可能无法提供中断正在进行的锁定的功能。因此，不需要为所有三种形式的锁获取定义完全相同的保证或语义的实现，也不需要支持正在进行的锁获取的中断的实现。需要一个实现来清楚地记录每个锁定方法提供的语义和保证。在支持锁获取中断的范围内，它还必须服从此接口中定义的中断语义：全部或仅在方法输入时才这样做。
 * 由于中断通常意味着取消，并且通常不经常进行中断检查，因此与正常方法返回相比，实现可能更喜欢对中断做出响应。 即使可以证明中断是在另一个操作取消线程之后发生的，也是如此。 实现应记录此行为。
 * */