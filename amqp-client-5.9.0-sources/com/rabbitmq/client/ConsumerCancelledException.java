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

import com.rabbitmq.utility.SensibleClone;

public class ConsumerCancelledException extends RuntimeException implements
        SensibleClone<ConsumerCancelledException> {

    /** Default for non-checking. */
    private static final long serialVersionUID = 1L;

    @Override
    public ConsumerCancelledException sensibleClone() {
        try {
            return (ConsumerCancelledException) super.clone();
        } catch (CloneNotSupportedException e) {
            // You've got to be kidding me
            throw new RuntimeException(e);
        }
    }

}
