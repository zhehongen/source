/*
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
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

package javax.security.auth;

/**
 * Objects such as credentials may optionally implement this interface
 * to provide the capability to destroy its contents.
 * 诸如凭据之类的对象可以选择实现此接口，以提供销毁其内容的功能。
 * @see javax.security.auth.Subject
 */
public interface Destroyable {

    /**
     * Destroy this {@code Object}.
     *
     * <p> Sensitive information associated with this {@code Object}
     * is destroyed or cleared.  Subsequent calls to certain methods
     * on this {@code Object} will result in an
     * {@code IllegalStateException} being thrown.
     *销毁该对象。与该对象关联的敏感信息被破坏或清除。 随后对该对象的某些方法的调用将导致抛出IllegalStateException。
     * <p>
     * The default implementation throws {@code DestroyFailedException}.
     *
     * @exception DestroyFailedException if the destroy operation fails. <p>
     *
     * @exception SecurityException if the caller does not have permission
     *          to destroy this {@code Object}.
     */
    public default void destroy() throws DestroyFailedException {
        throw new DestroyFailedException();
    }

    /**
     * Determine if this {@code Object} has been destroyed.
     *
     * <p>
     * The default implementation returns false.
     *
     * @return true if this {@code Object} has been destroyed,
     *          false otherwise.
     */
    public default boolean isDestroyed() {
        return false;
    }
}
