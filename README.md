# st_challange
This is my solution to the home assignment.
I had to do some limitations of the scope to juggle family life and still get a decent solution.
## Limitations
The fist major limitation is that there is no Authentication and no Authorization. This leads to
the unfortunate effect that a user can access, modify and delete any message / user.

This also leads to making a user request parameter mandatory for much of the message API.
The user would be read from the principal that would be provided after the request had been authenticated.

The domain objects are kind of uninteresting but they could easily be expanded if needed.

The user API does not support delete or update but since that was not part of the task I chose not to include it.

None of the APIs have support for paging witch would be needed in a "real life" implementation.

Another limitation is that the API is not HATEOAS.

# Building
The project is built by gradle and some extra features has been added by including make.

## How to Build
* Clone the repository.
* Install docker.
* Install make.
* Run the application by executing <code>make run</code> in the root folder of the project.
* Access the application by going to http://localhost:8080

## How to Create a Docker Image
* Clone the repository.
* Install docker.
* Install make.
* Execute <code>make docker</code>.

# APIs
There is discovery and two main APIs. The APIs should be more or less self explanatory by using the discovery and by 
following error messages.

None of the APIs are protected but should be in a "real life" implementation.
### User API
The first is the user API where a user can be created and read (not deleted or updated).
### Message API
The second api is the message API. It supports create, read, update and delete of messages and read of all messages with
user as optional filter.