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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fridaymastermix.message.Message;
import com.fridaymastermix.message.MessageNotFoundException;
import com.fridaymastermix.message.MessageService;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "users", produces = MediaType.APPLICATION_JSON_VALUE)
@SuppressWarnings("UnusedReturnValue")
public class UserController {
    @Autowired
    UserService users;

    @Autowired
    MessageService messages;

    @GetMapping()
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

    @GetMapping("/{user}")
    public User getUser(@PathVariable String user) {
        var description = users.describe(user);

        if (description == User.NONEXISTING) {
            var status = HttpStatus.NOT_FOUND;
            var message = String.format("user with id: %s could not be found.", user);
            throw new ResponseStatusException(status, message);
        }

        return description;
    }

    @GetMapping("/{user}/messages")
    public List<Message> getMessages(@PathVariable String user) {
        return messages.writtenBy(user);
    }

    // TODO: Felhantering och validering av input.
    @PostMapping("{user}/messages")
    public ResponseEntity<ResponseBody> postMessages(@PathVariable String user, @RequestBody @Validated Message message, HttpServletRequest request) {
        String messageId = messages.create(message.getMessage(), user);

        var requestUri = request.getRequestURI();
        var uriBuilder = UriComponentsBuilder.fromUriString(requestUri);
        uriBuilder.path("/{id}");

        var headers = new HttpHeaders();
        headers.setLocation(uriBuilder.build(messageId));

        var status = HttpStatus.CREATED;
        return new ResponseEntity<>(new ResponseBody(status.value(), "Created"), headers, status);
    }

    @GetMapping("{user}/messages/{message}")
    public Message getMessage(@PathVariable String user, @PathVariable String message) {
        var toReturn = messages.describe(message);

        if (toReturn == Message.NONEXISTING) {
            var errorMessage = String.format("Could not find message with id %s for user %s", message, user);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage);
        }

        return messages.describe(message);
    }

    @PutMapping("{user}/messages/{message}")
    public ResponseEntity<ResponseBody> putMessage(@PathVariable("user") String forUser,
                                                     @PathVariable("message") String messageId,
                                                     @RequestBody Message message,
                                                     HttpServletRequest request) {
        var messageWithId = new Message(messageId, message.getMessage());

        var requestUri = request.getRequestURI();
        var uriBuilder = UriComponentsBuilder.fromUriString(requestUri);
        var path = uriBuilder.build("");

        try {
            messages.update(messageWithId, forUser);
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

    @DeleteMapping(value = "{user}/messages/{message}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseBody> deleteMessage(@PathVariable("user") String forUser, @PathVariable String message) {
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseBody> handleValidationErrors(MethodArgumentNotValidException error) {
        var status = HttpStatus.BAD_REQUEST;
        var errorMessage = errorMessage(error);

        return new ResponseEntity<>(new ResponseBody(status.value(), errorMessage), status);
    }

    private String errorMessage(MethodArgumentNotValidException error) {
        var fieldErrors = error.getBindingResult().getFieldErrors();
        var fields = fieldErrors.stream().map(FieldError::getField).collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < fields.size(); i++) {
            sb.append(fields.get(i));

            if (i < fields.size() - 1) {
                sb.append(", ");
            }
        }

        if (fields.size() == 1) {
            sb.append(" is ");
        } else {
            sb.append(" are ");
        }
        sb.append("required");
        return sb.toString();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseBody> handleMissingBodyError(HttpMessageNotReadableException error) {
        var status = HttpStatus.BAD_REQUEST;
        var errorMessage = "Missing JSON body in request";
        return new ResponseEntity<>(new ResponseBody(status.value(), errorMessage), status);
    }

    private static class ResponseBody {
        private String timestamp;
        private int status;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String error;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String message;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String path;

        private ResponseBody(int status, String message) {
            this.status = status;
            this.message = message;
            this.timestamp = new DateTime().toString();
        }

        private ResponseBody(int status, String error, String message, String path) {
            this.status = status;
            this.message = message;
            this.error = error;
            this.path = path;
            this.timestamp = DateTime.now().withZone(DateTimeZone.UTC).toString();
        }

        public String getTimestamp() {
            return timestamp;
        }

        public int getStatus() {
            return status;
        }

        public String getError() {
            return error;
        }

        public String getMessage() {
            return message;
        }

        public String getPath() {
            return path;
        }
    }
}
