// Copyright (c) 2018-2020 VMware, Inc. or its affiliates.  All rights reserved.
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

package com.rabbitmq.client.impl;

import com.rabbitmq.client.Connection;

import java.io.IOException;

/**
 * Listener called when a connection gets an IO error trying to write on the socket.
 * This can be used to trigger connection recovery.
 *
 * @since 4.5.0
 */
public interface ErrorOnWriteListener {

    /**
     * Called when writing to the socket failed
     * @param connection the owning connection instance
     * @param exception the thrown exception
     */
    void handle(Connection connection, IOException exception) throws IOException;

}
