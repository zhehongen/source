/*
 * Copyright 2019-2020 Azul Systems,
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.azul.crs.agent;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

class AgentLoader {

    /**
     * Load Connected Runtime Services agent.
     *
     * @return Class of CRS agent or null if CRS agent is not bundles with JRE
     * @throws Exception if CRS agent is present but cannot be loaded
     */
    private static Object main() throws Exception {
        String agentJarName = System.getProperty("java.home")+"/lib/crs-agent.jar";
        if (!(new File(agentJarName)).exists())
            return null;
        URL url = new URL("file:///"+agentJarName);
        ClassLoader loader = new URLClassLoader(new URL[] { url }, null);
        Class agentClass = loader.loadClass("com.azul.crs.client.Agent001");

        registerNatives(agentClass);

        return agentClass;
    }

    /**
     * Register implementations for native methods of CRS agent.
     */
    private static native void registerNatives(Class agentClass);
}
