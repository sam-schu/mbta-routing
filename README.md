# MBTA Routing

A Kotlin program to report MBTA subway route information and find routes between
subway stations using data from the MBTA API.

### Getting Started

To run the program, first clone the repository and ensure that all Gradle
dependencies have been installed. Then, while connected to the Internet, simply
run the main function in the MbtaRouting.kt file (located in the src/main/kotlin
directory, outside of any packages) to start the program. The program will
provide instructions regarding the input that should be entered to use the
different features of the program.

### Project Overview

The project provides a console interface to enter commands that activate
different features of the program. The main features supported are:
- Listing all subway routes.
- Listing the subway route with the most stops, and how many stops it has.
- Listing the subway route with the fewest stops, and how many stops it has.
- Listing all stops that enable transfer between different subway routes, as
well as the routes connected by each of these stops.
- *Displaying a path with a minimal number of stops to travel from any
subway station to any other subway station in the system,* as well as the
transfers between routes needed to take such a path.

The project, and its directories, are organized according to the
model-view-controller (MVC) architecture. The model obtains and parses data
from the MBTA API using the Retrofit and JSONAPI-Converter libraries, and
constructs a graph for route-finding and other features. Filtering is performed
using the API to avoid downloading significant amounts of unnecessary data from
the server, which could waste network bandwidth and potentially degrade the
speed of the application (due to a slow download speed and/or the time required
for local filtering work). The controller is
implemented using the command pattern. Tests are provided in the src/test
directory; test classes starting with "Server" obtain data from the true API
for testing, while the tests in the remaining test classes are able to run
successfully regardless of the API's availability or output.