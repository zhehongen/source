// Copyright (c) 2017-2020 VMware, Inc. or its affiliates.  All rights reserved.
//
// This software, the RabbitMQ Java client library, is triple-licensed under the
// Mozilla Public License 1.1 ("MPL"), the GNU General Public License version 2
// ("GPL") and the Apache License version 2 ("ASL"). For the MPL, please see
// LICENSE-MPL-RabbitMQ. For the GPL, please see LICENSE-GPL2.  For the ASL,
// please see LICENSE-APACHE2.
//
// This software is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND,
// either express or implied. See the LICENSE file for specific language governing
// rights and limitations of this software.
//
// If you have any questions regarding licensing, please contact us at
// info@rabbitmq.com.

package com.rabbitmq.client;

import java.util.Map;

/**
 * Callback interface to be notified when either the consumer channel
 * or the underlying connection has been shut down.
 * Prefer it over {@link Consumer} for a lambda-oriented syntax,
 * if you don't need to implement all the application callbacks.
 * @see CancelCallback
 * @see DeliverCallback
 * @see Channel#basicConsume(String, boolean, String, boolean, boolean, Map, DeliverCallback, CancelCallback)
 * @see Channel#basicConsume(String, boolean, String, boolean, boolean, Map, DeliverCallback, ConsumerShutdownSignalCallback)
 * @see Channel#basicConsume(String, boolean, String, boolean, boolean, Map, DeliverCallback, CancelCallback, ConsumerShutdownSignalCallback)
 * @since 5.0
 */
@FunctionalInterface
public interface ConsumerShutdownSignalCallback {

    /**
     * Called when either the channel or the underlying connection has been shut down.
     * @param consumerTag the <i>consumer tag</i> associated with the consumer
     * @param sig a {@link ShutdownSignalException} indicating the reason for the shut down
     */
    void handleShutdownSignal(String consumerTag, ShutdownSignalException sig);

}
