# st_challange
This is my solution to the home assignment.
I had to do some limitations of the scope to juggle family life and still get a decent solution.
## Limitations
The fist major limitation is that there is no Authentication and no Authorization. This leads to
the unfortunate effect that a user can access, modify and delete any message / user.

This also leads to making a user request parameter mandatory for much of the message API.
The user would be read from the principal that would be provided after the request had been authenticated.

The domain objects are kind of uninteresting but they could easily be expanded if needed.

Another limitation is that the API is not HATEOAS.

## How to Build
* Clone the repository
* Install docker.
* Install make
* Run the application by executing <code>make run</make> in the root folder of the project.
* Access the application by going to http://localhost:8080
