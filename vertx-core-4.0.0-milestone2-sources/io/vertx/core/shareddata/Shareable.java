/*
 * Copyright (c) 2011-2017 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.core.shareddata;
//允许您将任意对象放入LocalMap.
/**
 * An interface which allows you to put arbitrary objects into a {@link io.vertx.core.shareddata.LocalMap}.
 * <p>
 * Normally local maps only allow immutable or copiable objects in order to avoid shared access to mutable state.
 * <p>
 * However if you have an object that you know is thread-safe you can mark it with this interface and then you
 * will be able to add it to {@link io.vertx.core.shareddata.LocalMap} instances.
 * <p>
 * Mutable object that you want to store in a {@link io.vertx.core.shareddata.LocalMap}
 * should override {@link Shareable#copy()} method.
 * <p>
 * Use this interface with caution.
 * <p>
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public interface Shareable {
//通常本地映射只允许不可变或可复制的对象，以避免对可变状态的共享访问.
  /**
   * Returns a copy of the object.
   * Only mutable objects should provide a custom implementation of the method.
   */
  default Shareable copy() {
    return this;
  }
}
//然而，如果你有一个你知道是线程安全的对象，你可以用这个接口标记它，然后你就可以将它添加到LocalMap实例中。
//
