package model.graph

import model.RoutePattern
import model.Stop
import java.util.Objects
import kotlin.collections.HashMap

/**
 * A graph with information about MBTA routes and stops.
 *
 * The graph holds a list of nodes, each of which represents an MBTA station.
 * Edges between nodes are directed and represent the ability to transit from
 * one station to another via one or more MBTA routes.
 *
 * @property stationNodes the nodes of the graph.
 */
internal class MbtaTransitGraph(
    internal val stationNodes: MutableList<StationNode>
) {
    companion object {
        /**
         * Generates a transit graph from a list of route patterns.
         *
         * One node is created for each station (based on station IDs), and one
         * directed edge is created for each direct connection between two
         * stations that is made by at least one of the given route patterns.
         * Each node holds information about all routes that transit through
         * that station, and each edge holds information about all routes that
         * make that connection.
         *
         * @throws IllegalArgumentException if any of the given route patterns
         * has a null property where a value is needed, or any of the given
         * route patterns has a stop or associated parent station with a null
         * ID where a value is needed.
         */
        fun fromRoutePatterns(patterns: List<RoutePattern>): MbtaTransitGraph {
            // A hash map from each (parent) station ID to its corresponding
            // node.
            val stations: HashMap<String, StationNode> = hashMapOf()

            // A list of the full stations (rather than platforms or similar)
            // traveled along on the current route pattern.
            var parentStations: List<Stop>

            var routeId: String
            var destId: String

            for (pattern in patterns) {
                if (pattern.route?.id == null || pattern.representativeTrip?.stops == null) {
                    throw IllegalArgumentException("The given route pattern has a null property.")
                }
                routeId = pattern.route.id
                parentStations = pattern.representativeTrip.stops.map { it.parentStation ?: it }

                // Updates the nodes based on the current route pattern - i.e.,
                // for any station that does not have a node, creates a node for
                // it, and for any station that already has a node, adds to its
                // list of route IDs if necessary. Edges are not dealt with
                // here.
                for (station in parentStations) {
                    if (station.id == null) {
                        throw IllegalArgumentException(
                            "The given route pattern has a stop or associated parent station with "
                                    + "a null ID."
                        )
                    }
                    if (stations.containsKey(station.id)) {
                        stations.getValue(station.id).routeIds.add(routeId)
                    } else {
                        stations[station.id] = StationNode(
                            station.id,
                            station.name,
                            mutableSetOf(routeId)
                        )
                    }
                }

                // Adds edges connecting consecutive stations in the pattern,
                // by adding new edges or updating the route IDs of existing
                // edges as necessary.
                for (i in 0..(parentStations.size - 2)) {
                    // The non-null assertions in these two lines are safe
                    // because the previous for loop would have thrown an
                    // IllegalArgumentException if any of these IDs were null.
                    destId = parentStations[i + 1].id!!
                    stations.getValue(parentStations[i].id!!).let { source ->
                        if (source.outgoingEdges.containsKey(destId)) {
                            source.outgoingEdges.getValue(destId).routeIds.add(routeId)
                        } else {
                            source.outgoingEdges[destId] = Edge(
                                source,
                                stations.getValue(destId),
                                mutableSetOf(routeId)
                            )
                        }
                    }
                }
            }

            return MbtaTransitGraph(stations.values.toMutableList())
        }
    }
}

/**
 * A node in an MbtaTransitGraph, representing a full station (rather than a
 * platform or similar).
 *
 * @property stationId the ID of the station from the API.
 * @property name the name of the station.
 * @property routeIds the set of route IDs corresponding to the routes that
 * have the station as a stop.
 * @property outgoingEdges a hash map where each outgoing edge from the node
 * has an entry, mapping the ID of the destination node to the corresponding
 * Edge object.
 */
internal class StationNode(
    internal val stationId: String,
    internal val name: String,
    internal val routeIds: MutableSet<String>,
    internal val outgoingEdges: HashMap<String, Edge> = hashMapOf()
) {
    /**
     * Returns whether the given object is equal to this node.
     *
     * All properties are compared, except only the set of keys of the
     * [outgoingEdges] hash map (i.e., the set of destination node IDs) is
     * considered.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is StationNode) {
            return false
        }
        return (this.stationId == other.stationId
                && this.name == other.name
                && this.routeIds == other.routeIds
                && this.outgoingEdges.keys.toSet() == other.outgoingEdges.keys.toSet())
    }

    /**
     * Returns a hash code for the node.
     *
     * All properties are taken into account, except only the set of keys of the
     * [outgoingEdges] hash map (i.e., the set of destination node IDs) is used
     * rather than the hash map itself.
     */
    override fun hashCode(): Int {
        return Objects.hash(stationId, name, routeIds, outgoingEdges.keys.toSet())
    }
}

/**
 * A directed edge in an MbtaTransitGraph, connecting one station node to
 * another.
 *
 * @property sourceNode the source node of the edge.
 * @property destNode the destination node of the edge.
 * @property routeIds the set of route IDs corresponding to the routes that make
 * the connection between stations represented by the edge.
 */
internal data class Edge(
    internal val sourceNode: StationNode,
    internal val destNode: StationNode,
    internal val routeIds: MutableSet<String>
)