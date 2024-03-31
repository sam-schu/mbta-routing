package model

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.jasminb.jsonapi.annotations.Id
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