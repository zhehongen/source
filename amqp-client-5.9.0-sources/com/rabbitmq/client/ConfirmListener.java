// Copyright (c) 2007-2020 VMware, Inc. or its affiliates.  All rights reserved.
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

import java.io.IOException;

/**
 * Implement this interface in order to be notified of Confirm events.
 * Acks represent messages handled successfully; Nacks represent
 * messages lost by the broker.  Note, the lost messages could still
 * have been delivered to consumers, but the broker cannot guarantee
 * this.
 * For a lambda-oriented syntax, use {@link ConfirmCallback}.
 */
public interface ConfirmListener {
    void handleAck(long deliveryTag, boolean multiple)
        throws IOException;

    void handleNack(long deliveryTag, boolean multiple)
        throws IOException;
}
