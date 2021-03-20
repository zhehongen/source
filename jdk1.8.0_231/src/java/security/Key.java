/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.security;

/**
 * The Key interface is the top-level interface for all keys. It
 * defines the functionality shared by all key objects. All keys
 * have three characteristics:
 *
 * <UL>
 *
 * <LI>An Algorithm
 *
 * <P>This is the key algorithm for that key. The key algorithm is usually
 * an encryption or asymmetric operation algorithm (such as DSA or
 * RSA), which will work with those algorithms and with related
 * algorithms (such as MD5 with RSA, SHA-1 with RSA, Raw DSA, etc.)
 * The name of the algorithm of a key is obtained using the
 * {@link #getAlgorithm() getAlgorithm} method.
 *
 * <LI>An Encoded Form
 *
 * <P>This is an external encoded form for the key used when a standard
 * representation of the key is needed outside the Java Virtual Machine,
 * as when transmitting the key to some other party. The key
 * is encoded according to a standard format (such as
 * X.509 {@code SubjectPublicKeyInfo} or PKCS#8), and
 * is returned using the {@link #getEncoded() getEncoded} method.
 * Note: The syntax of the ASN.1 type {@code SubjectPublicKeyInfo}
 * is defined as follows:
 *
 * <pre>
 * SubjectPublicKeyInfo ::= SEQUENCE {
 *   algorithm AlgorithmIdentifier,
 *   subjectPublicKey BIT STRING }
 *
 * AlgorithmIdentifier ::= SEQUENCE {
 *   algorithm OBJECT IDENTIFIER,
 *   parameters ANY DEFINED BY algorithm OPTIONAL }
 * </pre>
 *
 * For more information, see
 * <a href="http://www.ietf.org/rfc/rfc3280.txt">RFC 3280:
 * Internet X.509 Public Key Infrastructure Certificate and CRL Profile</a>.
 *
 * <LI>A Format
 *
 * <P>This is the name of the format of the encoded key. It is returned
 * by the {@link #getFormat() getFormat} method.
 *
 * </UL>
 *
 * Keys are generally obtained through key generators, certificates,
 * or various Identity classes used to manage keys.
 * Keys may also be obtained from key specifications (transparent
 * representations of the underlying key material) through the use of a key
 * factory (see {@link KeyFactory}).
 *
 * <p> A Key should use KeyRep as its serialized representation.
 * Note that a serialized Key may contain sensitive information
 * which should not be exposed in untrusted environments.  See the
 * <a href="../../../platform/serialization/spec/security.html">
 * Security Appendix</a>
 * of the Serialization Specification for more information.
 *
 * @see PublicKey
 * @see PrivateKey
 * @see KeyPair
 * @see KeyPairGenerator
 * @see KeyFactory
 * @see KeyRep
 * @see java.security.spec.KeySpec
 * @see Identity
 * @see Signer
 *
 * @author Benjamin Renaud
 */

public interface Key extends java.io.Serializable {

    // Declare serialVersionUID to be compatible with JDK1.1

   /**
    * The class fingerprint that is set to indicate
    * serialization compatibility with a previous
    * version of the class.
    */
    static final long serialVersionUID = 6603384152749567654L;

    /**返回此密钥的标准算法名称。 例如，“ DSA”将指示此密钥是DSA密钥。 有关标准算法名称的信息，请参见《 Java密码学体系结构API规范和参考》中的附录A。
     * Returns the standard algorithm name for this key. For
     * example, "DSA" would indicate that this key is a DSA key.
     * See Appendix A in the <a href=
     * "../../../technotes/guides/security/crypto/CryptoSpec.html#AppA">
     * Java Cryptography Architecture API Specification &amp; Reference </a>
     * for information about standard algorithm names.
     * 与此密钥关联的算法的名称。
     * @return the name of the algorithm associated with this key.
     */
    public String getAlgorithm();

    /**
     * Returns the name of the primary encoding format of this key,
     * or null if this key does not support encoding.
     * The primary encoding format is
     * named in terms of the appropriate ASN.1 data format, if an
     * ASN.1 specification for this key exists.
     * For example, the name of the ASN.1 data format for public
     * keys is <I>SubjectPublicKeyInfo</I>, as
     * defined by the X.509 standard; in this case, the returned format is
     * {@code "X.509"}. Similarly,
     * the name of the ASN.1 data format for private keys is
     * <I>PrivateKeyInfo</I>,
     * as defined by the PKCS #8 standard; in this case, the returned format is
     * {@code "PKCS#8"}.
     *返回此密钥的主要编码格式的名称；如果此密钥不支持编码，则返回null。 如果存在针对此密钥的ASN.1规范，则根据适当的ASN.1数据格式来命名主要编码格式。 例如，公共密钥的ASN.1数据格式的名称是X.509标准定义的SubjectPublicKeyInfo。 在这种情况下，返回的格式为“ X.509”。 同样，私钥的ASN.1数据格式的名称是PrivateKeyInfo，如PKCS＃8标准所定义； 在这种情况下，返回的格式为“ PKCS＃8”。
     * @return the primary encoding format of the key.
     */
    public String getFormat();

    /**
     * Returns the key in its primary encoding format, or null
     * if this key does not support encoding.
     * 以其主要编码格式返回密钥；如果此密钥不支持编码，则返回null。
     * @return the encoded key, or null if the key does not support
     * encoding.
     */
    public byte[] getEncoded();
}
/**
 * Key接口是所有key的顶级接口。它定义了所有key对象共享的功能。所有key具有三个特征：
 * 一种算法
 * 这是该密钥的密钥算法。密钥算法通常是加密或非对称操作算法（例如DSA或RSA），
 * 可与这些算法和相关算法（例如具有RSA的MD5，具有RSA的SHA-1，原始DSA等）一起使用。使用getAlgorithm方法获得密钥算法的密钥。
 * 编码形式
 * 这是密钥的一种外部编码形式，用于在Java虚拟机外部需要密钥的标准表示形式时（例如，将密钥传输给其他方时）。
 * 密钥根据标准格式（例如X.509 SubjectPublicKeyInfo或PKCS＃8）进行编码，并使用getEncoded方法返回。
 * 注意：ASN.1类型SubjectPublicKeyInfo的语法定义如下：
 SubjectPublicKeyInfo ::= SEQUENCE {
 algorithm AlgorithmIdentifier,
 subjectPublicKey BIT STRING }

 AlgorithmIdentifier ::= SEQUENCE {
 algorithm OBJECT IDENTIFIER,
 parameters ANY DEFINED BY algorithm OPTIONAL }
 *
 * 有关更多信息，请参见RFC 3280：Internet X.509公钥基础结构证书和CRL配置文件。
 * 格式
 * 这是编码密钥格式的名称。它由getFormat方法返回。
 * 密钥通常是通过密钥生成器，证书或用于管理密钥的各种Identity类获得的。也可以通过使用密钥工厂（请参见KeyFactory）从密钥规范（基础密钥材料的透明表示）中获取密钥。
 * 密钥应使用KeyRep作为其序列化表示形式。请注意，序列化的密钥可能包含敏感信息，这些信息不应在不受信任的环境中公开。有关更多信息，请参见序列化规范的安全性附录。
 */
