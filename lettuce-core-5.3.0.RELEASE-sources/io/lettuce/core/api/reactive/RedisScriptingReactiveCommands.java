/*
 * Copyright 2017-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.lettuce.core.api.reactive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import io.lettuce.core.ScriptOutputType;

/**
 * Reactive executed commands for Scripting.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 * @author Mark Paluch
 * @since 4.0
 * @generated by io.lettuce.apigenerator.CreateReactiveApi
 */
public interface RedisScriptingReactiveCommands<K, V> {

    /**
     * Execute a Lua script server side.
     *
     * @param script Lua 5.1 script.
     * @param type output type
     * @param keys key names
     * @param <T> expected return type
     * @return script result
     */
    <T> Flux<T> eval(String script, ScriptOutputType type, K... keys);

    /**
     * Execute a Lua script server side.
     *
     * @param script Lua 5.1 script.
     * @param type the type
     * @param keys the keys
     * @param values the values
     * @param <T> expected return type
     * @return script result
     */
    <T> Flux<T> eval(String script, ScriptOutputType type, K[] keys, V... values);

    /**
     * Evaluates a script cached on the server side by its SHA1 digest
     *
     * @param digest SHA1 of the script
     * @param type the type
     * @param keys the keys
     * @param <T> expected return type
     * @return script result
     */
    <T> Flux<T> evalsha(String digest, ScriptOutputType type, K... keys);

    /**
     * Execute a Lua script server side.
     *
     * @param digest SHA1 of the script
     * @param type the type
     * @param keys the keys
     * @param values the values
     * @param <T> expected return type
     * @return script result
     */
    <T> Flux<T> evalsha(String digest, ScriptOutputType type, K[] keys, V... values);

    /**
     * Check existence of scripts in the script cache.
     *
     * @param digests script digests
     * @return Boolean array-reply The command returns an array of integers that correspond to the specified SHA1
     *         digest arguments. For every corresponding SHA1 digest of a script that actually exists in the script cache, an 1
     *         is returned, otherwise 0 is returned.
     */
    Flux<Boolean> scriptExists(String... digests);

    /**
     * Remove all the scripts from the script cache.
     *
     * @return String simple-string-reply
     */
    Mono<String> scriptFlush();

    /**
     * Kill the script currently in execution.
     *
     * @return String simple-string-reply
     */
    Mono<String> scriptKill();

    /**
     * Load the specified Lua script into the script cache.
     *
     * @param script script content
     * @return String bulk-string-reply This command returns the SHA1 digest of the script added into the script cache.
     */
    Mono<String> scriptLoad(V script);

    /**
     * Create a SHA1 digest from a Lua script.
     *
     * @param script script content
     * @return the SHA1 value
     */
    String digest(V script);
}