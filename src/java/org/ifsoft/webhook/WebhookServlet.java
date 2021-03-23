/**
 * Copyright (C) 2016 McFoggy [https://github.com/McFoggy/xhub4j] (matthieu@brouillard.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ifsoft.webhook;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.*;
import org.slf4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WebhookServlet extends HttpServlet {
    private static final Logger Log = LoggerFactory.getLogger(WebhookServlet.class);
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Log.info(String.format("handling: %s on %s", req.getRequestURI(), WebhookServlet.class.getSimpleName()));
        Log.info("HTTP Headers");

        Collections.list(req.getHeaderNames())
            .stream()
            .map(header -> header + ": " + req.getHeader(header))
            .forEach(Log::debug);

        String type = req.getParameter("type");
        String user = req.getParameter("user");
        String group = req.getParameter("group");
        Log.info("HTTP Route " + type + " " + user + " " + group);

        String body = req.getReader().lines().collect(Collectors.joining());
        Log.info("HTTP Payload\n" + body);

        resp.setStatus(HttpServletResponse.SC_ACCEPTED);
    }
}