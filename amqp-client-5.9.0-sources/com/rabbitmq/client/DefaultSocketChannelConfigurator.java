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
import java.nio.channels.SocketChannel;

public class DefaultSocketChannelConfigurator implements SocketChannelConfigurator {

    /**
     *  Provides a hook to insert custom configuration of the {@link SocketChannel}s
     *  used to connect to an AMQP server before they connect.
     *
     *  The default behaviour of this method is to disable Nagle's
     *  algorithm to get more consistently low latency.  However it
     *  may be overridden freely and there is no requirement to retain
     *  this behaviour.
     *
     *  @param socketChannel The socket channel that is to be used for the Connection
     */
    @Override
    public void configure(SocketChannel socketChannel) throws IOException {
        socketChannel.socket().setTcpNoDelay(true);
    }
}
