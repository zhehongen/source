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

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceArray;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Supplier;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueueUtil;
import io.netty.util.internal.shaded.org.jctools.queues.QueueProgressIndicators;
import io.netty.util.internal.shaded.org.jctools.queues.IndexedQueueSizeUtil;
import static io.netty.util.internal.shaded.org.jctools.queues.atomic.AtomicQueueUtil.*;

/**
 * NOTE: This class was automatically generated by io.netty.util.internal.shaded.org.jctools.queues.atomic.JavaParsingAtomicLinkedQueueGenerator
 * which can found in the jctools-build module. The original source file is MpscUnboundedArrayQueue.java.
 *
 * An MPSC array queue which starts at <i>initialCapacity</i> and grows indefinitely in linked chunks of the initial size.
 * The queue grows only when the current chunk is full and elements are not copied on
 * resize, instead a link to the new chunk is stored in the old chunk for the consumer to follow.
 */
public class MpscUnboundedAtomicArrayQueue<E> extends BaseMpscLinkedAtomicArrayQueue<E> {

    long p0, p1, p2, p3, p4, p5, p6, p7;

    long p10, p11, p12, p13, p14, p15, p16, p17;

    public MpscUnboundedAtomicArrayQueue(int chunkSize) {
        super(chunkSize);
    }

    @Override
    protected long availableInQueue(long pIndex, long cIndex) {
        return Integer.MAX_VALUE;
    }

    @Override
    public int capacity() {
        return MessagePassingQueue.UNBOUNDED_CAPACITY;
    }

    @Override
    public int drain(Consumer<E> c) {
        return drain(c, 4096);
    }

    @Override
    public int fill(Supplier<E> s) {
        return MessagePassingQueueUtil.fillUnbounded(this, s);
    }

    @Override
    protected int getNextBufferSize(AtomicReferenceArray<E> buffer) {
        return length(buffer);
    }

    @Override
    protected long getCurrentBufferCapacity(long mask) {
        return mask;
    }
}