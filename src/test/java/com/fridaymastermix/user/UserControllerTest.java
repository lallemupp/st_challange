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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserControllerTest {

    private UserController userController;
    private UserService userService;
    private HttpServletRequest request;

    @Before
    public void setup() {
        userController = new UserController();
        userService = mock(UserService.class);
        request = mock(HttpServletRequest.class);
        userController.users = userService;
    }

    @After
    public void teardown() {
        userController = null;
        userService = null;
    }

    @Test
    public void listUsers() {
        userController.listUsers();
        verify(userService).all();
    }

    @Test
    public void getUser() {
        userController.getUser("lalle");
        verify(userService).describe("lalle");
    }



    @Test
    public void postMessageNonUniqueUser() throws UserAlreadyExistsException {
        var user = new User("lalle", "password");

        when(request.getRequestURI()).thenReturn("http://example.org/users/");
        doThrow(new UserAlreadyExistsException("DANGER !!!!!! TERROR HORROR")).when(userService).create(user);

        var result = userController.postUser(user, request);
        var expectedStatus = HttpStatus.SEE_OTHER;
        var expectedLocation = "http://example.org/users/lalle";
        assertEquals(expectedStatus, result.getStatusCode());
        assertEquals(expectedLocation, result.getHeaders().getLocation().toString());
    }
}