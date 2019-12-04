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

package com.fridaymastermix.user;

import com.fridaymastermix.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "users", produces = MediaType.APPLICATION_JSON_VALUE)
@SuppressWarnings("UnusedReturnValue")
public class UserController {
    @Autowired
    UserService users;

    @GetMapping
    public UserWrapper listUsers() {
        var userList = users.all();
        return new UserWrapper(userList);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseBody> postUser(@RequestBody @Validated User user, HttpServletRequest request) {
        var requestUri = request.getRequestURI();
        var uriBuilder = UriComponentsBuilder.fromUriString(requestUri);
        uriBuilder.path("/{id}");

        var headers = new HttpHeaders();
        headers.setLocation(uriBuilder.build(user.getUserName()));

        try {
            users.create(user);
        } catch (NonUniqueUserException e) {
            headers.setContentType(MediaType.APPLICATION_JSON);

            var status = HttpStatus.SEE_OTHER;
            var message = String.format("User with user name %s does already exist. Try another user name", user.getUserName());

            return new ResponseEntity<>(new ResponseBody(status.value(), message), headers, status);
        }

        var status = HttpStatus.CREATED;
        var message = "Created";
        return new ResponseEntity<>(new ResponseBody(status.value(), message), headers, status);
    }

    @GetMapping("{user}")
    public User getUser(@PathVariable String user) {
        var description = users.describe(user);

        if (description == User.NONEXISTING) {
            var status = HttpStatus.NOT_FOUND;
            var message = String.format("user with id: %s could not be found.", user);
            throw new ResponseStatusException(status, message);
        }

        return description;
    }
}
