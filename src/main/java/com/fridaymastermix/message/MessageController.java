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

package com.fridaymastermix.message;

import com.fridaymastermix.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Handles requests for messages.
 */
@RestController
@RequestMapping(value = "messages", produces = MediaType.APPLICATION_JSON_VALUE)
public class MessageController {

    @Autowired
    MessageService messages;

    /**
     * Returns a list of all messages or a list of messages for a provided user.
     * @param user the user. Optional.
     *
     * @return a list of messages.
     */
    @GetMapping
    public MessageWrapper getMessages(@RequestParam(value = "user", required = false) String user) {
        List<Message> messageList;

        if (StringUtils.isNotBlank(user)) {
             messageList = messages.writtenBy(user);
        } else {
            messageList = messages.all();
        }

        return new MessageWrapper(messageList);
    }

    /**
     * Handles POST requests for messages.
     *
     * @param message the message to create.
     * @param user the user to create the message for.
     * @param request the servlet request used for creating the location header.
     * @return a response entity that contains information about the created message or an error.
     */
    @PostMapping
    public ResponseEntity<ResponseBody> postMessages(@RequestBody @Validated Message message, @RequestParam String user,  HttpServletRequest request) {
        var requestUri = request.getRequestURI();
        var uriBuilder = UriComponentsBuilder.fromUriString(requestUri);

        try {
            String messageId = messages.create(message.getMessage(), user);

            uriBuilder.path("/{id}");
            var headers = new HttpHeaders();
            headers.setLocation(uriBuilder.build(messageId));

            var status = HttpStatus.CREATED;
            return new ResponseEntity<>(new ResponseBody(status.value(), "Created"), headers, status);

        } catch (UserNotFoundException e) {
            var status = HttpStatus.NOT_ACCEPTABLE;
            var error = "Not Acceptable";
            var errorMessage = e.getMessage();
            var path = uriBuilder.build("");
            return new ResponseEntity<>(new ResponseBody(status.value(), error, errorMessage, path.toString()), status);
        }
    }

    /**
     * Handles GET requests for messages.
     *
     * @param message the id of the message to GET.
     * @return the message or an error.
     */
    @GetMapping("{message}")
    public Message getMessage(@PathVariable String message) {
        var toReturn = messages.describe(message);

        if (toReturn == Message.NONEXISTING) {
            var errorMessage = String.format("Could not find message with id %s", message);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage);
        }

        return messages.describe(message);
    }

    /**
     * Handles PUT requests for messages.
     *
     * @param messageId the id of the message to update.
     * @param message the message to update to.
     * @param request the request used to create a path in the error response.
     * @return a response entity that contains the information about the response or error.
     */
    @PutMapping("{message}")
    public ResponseEntity<ResponseBody> putMessage(@PathVariable("message") String messageId,
                                                   @RequestBody Message message,
                                                   HttpServletRequest request) {
        var messageWithId = new Message(messageId, message.getMessage());

        var requestUri = request.getRequestURI();
        var uriBuilder = UriComponentsBuilder.fromUriString(requestUri);
        var path = uriBuilder.build("");

        try {
            messages.update(messageWithId);
        } catch (MessageNotFoundException e) {
            var statusCode = HttpStatus.NOT_FOUND;
            var error = "Not Found";
            var errorMessage = e.getMessage();

            return new ResponseEntity<>(new ResponseBody(statusCode.value(), error, errorMessage, path.toString()), HttpStatus.NOT_FOUND);
        }

        var statusCode = HttpStatus.OK;
        var responseMessage = "Updated";
        return new ResponseEntity<>(new ResponseBody(statusCode.value(), responseMessage), statusCode);
    }

    /**
     * Handle delete requests for messages.
     *
     * @param message the id of the message to delete.
     * @param forUser the user to delete the message for.
     * @return a response entity that contains information about the result of the delete.
     */
    @DeleteMapping(value = "{message}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseBody> deleteMessage(@PathVariable String message, @RequestParam("user") String forUser) {
        boolean success = messages.delete(message, forUser);
        if (success) {
            var status = HttpStatus.OK;
            return new ResponseEntity<>(new ResponseBody(status.value(), "Deleted"), status);
        } else {
            var status = HttpStatus.NOT_FOUND;
            var responseMessage = String.format("Could not find item with id: %s for user %s", message, forUser);
            return new ResponseEntity<>(new ResponseBody(status.value(), responseMessage), status);
        }
    }
}
