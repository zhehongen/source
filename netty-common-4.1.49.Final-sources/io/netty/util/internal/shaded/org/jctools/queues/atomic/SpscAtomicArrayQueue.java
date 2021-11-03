/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.netty.util.internal.shaded.org.jctools.queues.atomic;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.AtomicLongArray;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueueUtil;
import static io.netty.util.internal.shaded.org.jctools.queues.atomic.AtomicQueueUtil.*;

/**
 * NOTE: This class was automatically generated by io.netty.util.internal.shaded.org.jctools.queues.atomic.JavaParsingAtomicArrayQueueGenerator
 * which can found in the jctools-build module. The original source file is SpscArrayQueue.java.
 */
abstract class SpscAtomicArrayQueueColdField<E> extends AtomicReferenceArrayQueue<E> {

    public static final int MAX_LOOK_AHEAD_STEP = Integer.getInteger("jctools.spsc.max.lookahead.step", 4096);

    final int lookAheadStep;

    SpscAtomicArrayQueueColdField(int capacity) {
        super(capacity);
        lookAheadStep = Math.min(capacity() / 4, MAX_LOOK_AHEAD_STEP);
    }
}

/**
 * NOTE: This class was automatically generated by io.netty.util.internal.shaded.org.jctools.queues.atomic.JavaParsingAtomicArrayQueueGenerator
 * which can found in the jctools-build module. The original source file is SpscArrayQueue.java.
 */
abstract class SpscAtomicArrayQueueL1Pad<E> extends SpscAtomicArrayQueueColdField<E> {

    long p01, p02, p03, p04, p05, p06, p07;

    long p10, p11, p12, p13, p14, p15, p16, p17;

    SpscAtomicArrayQueueL1Pad(int capacity) {
        super(capacity);
    }
}

/**
 * NOTE: This class was automatically generated by io.netty.util.internal.shaded.org.jctools.queues.atomic.JavaParsingAtomicArrayQueueGenerator
 * which can found in the jctools-build module. The original source file is SpscArrayQueue.java.
 */
abstract class SpscAtomicArrayQueueProducerIndexFields<E> extends SpscAtomicArrayQueueL1Pad<E> {

    private static final AtomicLongFieldUpdater<SpscAtomicArrayQueueProducerIndexFields> P_INDEX_UPDATER = AtomicLongFieldUpdater.newUpdater(SpscAtomicArrayQueueProducerIndexFields.class, "producerIndex");

    private volatile long producerIndex;

    protected long producerLimit;

    SpscAtomicArrayQueueProducerIndexFields(int capacity) {
        super(capacity);
    }

    @Override
    public final long lvProducerIndex() {
        return producerIndex;
    }

    final long lpProducerIndex() {
        return producerIndex;
    }

    final void soProducerIndex(final long newValue) {
        P_INDEX_UPDATER.lazySet(this, newValue);
    }
}

/**
 * NOTE: This class was automatically generated by io.netty.util.internal.shaded.org.jctools.queues.atomic.JavaParsingAtomicArrayQueueGenerator
 * which can found in the jctools-build module. The original source file is SpscArrayQueue.java.
 */
abstract class SpscAtomicArrayQueueL2Pad<E> extends SpscAtomicArrayQueueProducerIndexFields<E> {

    long p01, p02, p03, p04, p05, p06, p07;

    long p10, p11, p12, p13, p14, p15, p16, p17;

    SpscAtomicArrayQueueL2Pad(int capacity) {
        super(capacity);
    }
}

/**
 * NOTE: This class was automatically generated by io.netty.util.internal.shaded.org.jctools.queues.atomic.JavaParsingAtomicArrayQueueGenerator
 * which can found in the jctools-build module. The original source file is SpscArrayQueue.java.
 */
abstract class SpscAtomicArrayQueueConsumerIndexField<E> extends SpscAtomicArrayQueueL2Pad<E> {

    private static final AtomicLongFieldUpdater<SpscAtomicArrayQueueConsumerIndexField> C_INDEX_UPDATER = AtomicLongFieldUpdater.newUpdater(SpscAtomicArrayQueueConsumerIndexField.class, "consumerIndex");

    private volatile long consumerIndex;

    SpscAtomicArrayQueueConsumerIndexField(int capacity) {
        super(capacity);
    }

    public final long lvConsumerIndex() {
        return consumerIndex;
    }

    final long lpConsumerIndex() {
        return consumerIndex;
    }

    final void soConsumerIndex(final long newValue) {
        C_INDEX_UPDATER.lazySet(this, newValue);
    }
}

/**
 * NOTE: This class was automatically generated by io.netty.util.internal.shaded.org.jctools.queues.atomic.JavaParsingAtomicArrayQueueGenerator
 * which can found in the jctools-build module. The original source file is SpscArrayQueue.java.
 */
abstract class SpscAtomicArrayQueueL3Pad<E> extends SpscAtomicArrayQueueConsumerIndexField<E> {

    long p01, p02, p03, p04, p05, p06, p07;

    long p10, p11, p12, p13, p14, p15, p16, p17;

    SpscAtomicArrayQueueL3Pad(int capacity) {
        super(capacity);
    }
}

/**
 * NOTE: This class was automatically generated by io.netty.util.internal.shaded.org.jctools.queues.atomic.JavaParsingAtomicArrayQueueGenerator
 * which can found in the jctools-build module. The original source file is SpscArrayQueue.java.
 *
 * A Single-Producer-Single-Consumer queue backed by a pre-allocated buffer.
 * <p>
 * This implementation is a mashup of the <a href="http://sourceforge.net/projects/mc-fastflow/">Fast Flow</a>
 * algorithm with an optimization of the offer method taken from the <a
 * href="http://staff.ustc.edu.cn/~bhua/publications/IJPP_draft.pdf">BQueue</a> algorithm (a variation on Fast
 * Flow), and adjusted to comply with Queue.offer semantics with regards to capacity.<br>
 * For convenience the relevant papers are available in the `resources` folder:<br>
 * <i>
 *     2010 - Pisa - SPSC Queues on Shared Cache Multi-Core Systems.pdf<br>
 *     2012 - Junchang- BQueue- Efﬁcient and Practical Queuing.pdf <br>
 * </i>
 * This implementation is wait free.
 */
public class SpscAtomicArrayQueue<E> extends SpscAtomicArrayQueueL3Pad<E> {

    public SpscAtomicArrayQueue(final int capacity) {
        super(Math.max(capacity, 4));
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation is correct for single producer thread use only.
     */
    @Override
    public boolean offer(final E e) {
        if (null == e) {
            throw new NullPointerException();
        }
        // local load of field to avoid repeated loads after volatile reads
        final AtomicReferenceArray<E> buffer = this.buffer;
        final int mask = this.mask;
        final long producerIndex = this.lpProducerIndex();
        if (producerIndex >= producerLimit && !offerSlowPath(buffer, mask, producerIndex)) {
            return false;
        }
        final int offset = calcCircularRefElementOffset(producerIndex, mask);
        soRefElement(buffer, offset, e);
        // ordered store -> atomic and ordered for size()
        soProducerIndex(producerIndex + 1);
        return true;
    }

    private boolean offerSlowPath(final AtomicReferenceArray<E> buffer, final int mask, final long producerIndex) {
        final int lookAheadStep = this.lookAheadStep;
        if (null == lvRefElement(buffer, calcCircularRefElementOffset(producerIndex + lookAheadStep, mask))) {
            producerLimit = producerIndex + lookAheadStep;
        } else {
            final int offset = calcCircularRefElementOffset(producerIndex, mask);
            if (null != lvRefElement(buffer, offset)) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation is correct for single consumer thread use only.
     */
    @Override
    public E poll() {
        final long consumerIndex = this.lpConsumerIndex();
        final int offset = calcCircularRefElementOffset(consumerIndex, mask);
        // local load of field to avoid repeated loads after volatile reads
        final AtomicReferenceArray<E> buffer = this.buffer;
        final E e = lvRefElement(buffer, offset);
        if (null == e) {
            return null;
        }
        soRefElement(buffer, offset, null);
        // ordered store -> atomic and ordered for size()
        soConsumerIndex(consumerIndex + 1);
        return e;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation is correct for single consumer thread use only.
     */
    @Override
    public E peek() {
        return lvRefElement(buffer, calcCircularRefElementOffset(lpConsumerIndex(), mask));
    }

    @Override
    public boolean relaxedOffer(final E message) {
        return offer(message);
    }

    @Override
    public E relaxedPoll() {
        return poll();
    }

    @Override
    public E relaxedPeek() {
        return peek();
    }

    @Override
    public int drain(final Consumer<E> c) {
        return drain(c, capacity());
    }

    @Override
    public int fill(final Supplier<E> s) {
        return fill(s, capacity());
    }

    @Override
    public int drain(final Consumer<E> c, final int limit) {
        if (null == c)
            throw new IllegalArgumentException("c is null");
        if (limit < 0)
            throw new IllegalArgumentException("limit is negative: " + limit);
        if (limit == 0)
            return 0;
        final AtomicReferenceArray<E> buffer = this.buffer;
        final int mask = this.mask;
        final long consumerIndex = this.lpConsumerIndex();
        for (int i = 0; i < limit; i++) {
            final long index = consumerIndex + i;
            final int offset = calcCircularRefElementOffset(index, mask);
            final E e = lvRefElement(buffer, offset);
            if (null == e) {
                return i;
            }
            soRefElement(buffer, offset, null);
            // ordered store -> atomic and ordered for size()
            soConsumerIndex(index + 1);
            c.accept(e);
        }
        return limit;
    }

    @Override
    public int fill(final Supplier<E> s, final int limit) {
        if (null == s)
            throw new IllegalArgumentException("supplier is null");
        if (limit < 0)
            throw new IllegalArgumentException("limit is negative:" + limit);
        if (limit == 0)
            return 0;
        final AtomicReferenceArray<E> buffer = this.buffer;
        final int mask = this.mask;
        final int lookAheadStep = this.lookAheadStep;
        final long producerIndex = this.lpProducerIndex();
        for (int i = 0; i < limit; i++) {
            final long index = producerIndex + i;
            final int lookAheadElementOffset = calcCircularRefElementOffset(index + lookAheadStep, mask);
            if (null == lvRefElement(buffer, lookAheadElementOffset)) {
                int lookAheadLimit = Math.min(lookAheadStep, limit - i);
                for (int j = 0; j < lookAheadLimit; j++) {
                    final int offset = calcCircularRefElementOffset(index + j, mask);
                    soRefElement(buffer, offset, s.get());
                    // ordered store -> atomic and ordered for size()
                    soProducerIndex(index + j + 1);
                }
                i += lookAheadLimit - 1;
            } else {
                final int offset = calcCircularRefElementOffset(index, mask);
                if (null != lvRefElement(buffer, offset)) {
                    return i;
                }
                soRefElement(buffer, offset, s.get());
                // ordered store -> atomic and ordered for size()
                soProducerIndex(index + 1);
            }
        }
        return limit;
    }

    @Override
    public void drain(final Consumer<E> c, final WaitStrategy w, final ExitCondition exit) {
        if (null == c)
            throw new IllegalArgumentException("c is null");
        if (null == w)
            throw new IllegalArgumentException("wait is null");
        if (null == exit)
            throw new IllegalArgumentException("exit condition is null");
        final AtomicReferenceArray<E> buffer = this.buffer;
        final int mask = this.mask;
        long consumerIndex = this.lpConsumerIndex();
        int counter = 0;
        while (exit.keepRunning()) {
            for (int i = 0; i < 4096; i++) {
                final int offset = calcCircularRefElementOffset(consumerIndex, mask);
                final E e = lvRefElement(buffer, offset);
                if (null == e) {
                    counter = w.idle(counter);
                    continue;
                }
                consumerIndex++;
                counter = 0;
                soRefElement(buffer, offset, null);
                // ordered store -> atomic and ordered for size()
                soConsumerIndex(consumerIndex);
                c.accept(e);
            }
        }
    }

    @Override
    public void fill(final Supplier<E> s, final WaitStrategy w, final ExitCondition e) {
        if (null == w)
            throw new IllegalArgumentException("waiter is null");
        if (null == e)
            throw new IllegalArgumentException("exit condition is null");
        if (null == s)
            throw new IllegalArgumentException("supplier is null");
        final AtomicReferenceArray<E> buffer = this.buffer;
        final int mask = this.mask;
        final int lookAheadStep = this.lookAheadStep;
        long producerIndex = this.lpProducerIndex();
        int counter = 0;
        while (e.keepRunning()) {
            final int lookAheadElementOffset = calcCircularRefElementOffset(producerIndex + lookAheadStep, mask);
            if (null == lvRefElement(buffer, lookAheadElementOffset)) {
                for (int j = 0; j < lookAheadStep; j++) {
                    final int offset = calcCircularRefElementOffset(producerIndex, mask);
                    producerIndex++;
                    soRefElement(buffer, offset, s.get());
                    // ordered store -> atomic and ordered for size()
                    soProducerIndex(producerIndex);
                }
            } else {
                final int offset = calcCircularRefElementOffset(producerIndex, mask);
                if (null != lvRefElement(buffer, offset)) {
                    counter = w.idle(counter);
                    continue;
                }
                producerIndex++;
                counter = 0;
                soRefElement(buffer, offset, s.get());
                // ordered store -> atomic and ordered for size()
                soProducerIndex(producerIndex);
            }
        }
    }
}
