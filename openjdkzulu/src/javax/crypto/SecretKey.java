/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package javax.crypto;

/**秘密（对称）密钥。该接口的目的是对所有秘密接口进行分组（并提供类型安全性）。
 * A secret (symmetric) key.
 * The purpose of this interface is to group (and provide type safety
 * for) all secret key interfaces.
 * <p>此接口的提供程序实现必须覆盖从Object继承的equals和hashCode方法，以便根据秘密密钥的基础密钥材料而不是引用对秘密密钥进行比较。实现应重写javax.security.auth.Destroyable接口中的默认destroy和isDestroyed方法，以使敏感密钥信息能够被销毁，清除，或者在此类信息是不可变的，未引用的情况下。最后，由于SecretKey是可序列化的，因此实现还应该重写java.io.ObjectOutputStream.writeObject（Object）以防止序列化销毁的密钥。
 * Provider implementations of this interface must overwrite the
 * {@code equals} and {@code hashCode} methods inherited from
 * {@link java.lang.Object}, so that secret keys are compared based on
 * their underlying key material and not based on reference.
 * Implementations should override the default {@code destroy} and
 * {@code isDestroyed} methods from the
 * {@link javax.security.auth.Destroyable} interface to enable
 * sensitive key information to be destroyed, cleared, or in the case
 * where such information is immutable, unreferenced.
 * Finally, since {@code SecretKey} is {@code Serializable}, implementations
 * should also override
 * {@link java.io.ObjectOutputStream#writeObject(java.lang.Object)}
 * to prevent keys that have been destroyed from being serialized.
 *实现此接口的键返回字符串RAW作为其编码格式（请参见getFormat），并返回原始键字节，作为getEncoded方法调用的结果。 （getFormat和getEncoded方法是从java.security.Key父接口继承的。）
 * <p>Keys that implement this interface return the string {@code RAW}
 * as their encoding format (see {@code getFormat}), and return the
 * raw key bytes as the result of a {@code getEncoded} method call. (The
 * {@code getFormat} and {@code getEncoded} methods are inherited
 * from the {@link java.security.Key} parent interface.)
 *
 * @author Jan Luehe
 *
 * @see SecretKeyFactory
 * @see Cipher
 * @since 1.4
 */

public interface SecretKey extends
    java.security.Key, javax.security.auth.Destroyable {

    /**
     * The class fingerprint that is set to indicate serialization
     * compatibility since J2SE 1.4.
     */
    static final long serialVersionUID = -4795878709595146952L;
}
