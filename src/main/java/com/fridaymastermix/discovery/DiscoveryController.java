/*
 *    Copyright 2019 Love Löfdahl
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.fridaymastermix.discovery;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Controller that handles discovery requests.
 */
@RestController
public class DiscoveryController implements InitializingBean {

    private String discoveryJson = "";

    /**
     * Handles discovery requests.
     *
     * @return the discovery json.
     */
    @RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public String discovery() {
        return discoveryJson;
    }

    /**
     * Reads the discovery file and loads it into memory.
     *
     * @throws Exception if anything goes wrong while loading the discovery file.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        var discoveryResource = new ClassPathResource("discovery.json", DiscoveryController.class);
        try (var inputStream = discoveryResource.getInputStream(); var outputStream = new ByteArrayOutputStream()) {
            try (var inputReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                discoveryJson = FileCopyUtils.copyToString(inputReader);
            }
        } catch (IOException e) {
            System.out.println(String.format("Could not read %s. The service will not work.", discoveryResource.getURL()));
        }
    }
}
