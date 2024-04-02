package model

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type

/**
 * A single route on the MBTA system.
 *
 * A route represents what is typically thought of as a "line" - e.g., the Red
 * Line, or a single numbered bus route. A route does not necessarily represent
 * a single sequence of stops, but rather a group of such "route patterns" that
 * are all described under the same name.
 *
 * @property id the ID of the route from the API.
 * @property name the name of the route, corresponding to its long_name from the
 * API.
 */
@Type("route")
data class Route(
    @Id val id: String?,
    @JsonProperty("long_name") val name: String
)

/**
 * A single route pattern on the MBTA system.
 *
 * A route pattern represents a generalized description of a single sequence of
 * stops that can be transited along on a route.
 *
 * @property id the ID of the route pattern from the API.
 * @property route the RouteId the route pattern belongs to.
 * @property representativeTrip the route pattern's representative trip, which
 * includes its sequence of stops.
 */
@Type("route_pattern")
data class RoutePattern(
    @Id val id: String?,
    @Relationship("route") val route: RouteId?,
    @Relationship("representative_trip") val representativeTrip: Trip?
)

/**
 * Holds the ID of a single route on the MBTA system.
 *
 * A route represents what is typically thought of as a "line" - e.g., the Red
 * Line, or a single numbered bus route. A route does not necessarily represent
 * a single sequence of stops, but rather a group of such "route patterns" that
 * are all described under the same name.
 *
 * @property id the ID of the route from the API.
 */
@Type("route")
data class RouteId(
    @Id val id: String?
)

/**
 * A single trip on the MBTA system.
 *
 * A trip represents a particular instance of the sequence of stops traveled
 * along a route pattern.
 *
 * @property id the ID of the trip from the API.
 * @property stops the list of stops traveled along.
 */
@Type("trip")
data class Trip(
    @Id val id: String?,
    @Relationship("stops") val stops: List<Stop>?
)

/**
 * A single stop on the MBTA system.
 *
 * A stop represents a station or a part of a station.
 *
 * @property id the ID of the stop from the API.
 * @property name the name of the stop.
 * @property parentStation the station the stop is a part of. If null, the stop
 * is a full station.
 */
@Type("stop")
data class Stop(
    @Id val id: String?,
    val name: String,
    @Relationship("parent_station") val parentStation: Stop?
)