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

import com.fridaymastermix.message.MessageService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class UserControllerTest {

    private UserController userController;
    private UserService userService;
    private MessageService messageService;

    @Before
    public void setup() {
        userController = new UserController();
        userService = mock(UserService.class);
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
    public void getMessages() {
        userController.getMessages("lalle");
        verify(userService).messagesWrittenBy("lalle");
    }
}