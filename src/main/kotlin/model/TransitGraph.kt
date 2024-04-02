package model

import java.util.Objects
import java.util.Queue
import java.util.LinkedList
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
    /**
     * Returns a path from the given source station to the given destination
     * station.
     *
     * The source and destination stations must be given by their names, not
     * their IDs. The path is returned as a list of pairs of strings, where each
     * pair represents (1) the route ID to take to the next stop and (2) the
     * name of the next stop. (The source station is not included in the
     * returned path.) The path returned is a path requiring the least number of
     * stops to get from the source to the destination.
     *
     * Null is returned if no path can be found from the source station to the
     * destination station. This includes if the source and destination stations
     * are the same.
     *
     * @throws IllegalArgumentException if a station with the given source or
     * destination name cannot be found.
     */
    internal fun findPath(
        sourceStationName: String, destStationName: String
    ): List<Pair<String, String>>? {
        val edgePath = bfs(
            getStationNodeFromName(sourceStationName),
            getStationNodeFromName(destStationName)
        ) ?: return null

        return getDirectionsFromEdgePath(edgePath)
    }

    // Gets the station node with the given name, or throws an
    // IllegalArgumentException if no such node exists.
    private fun getStationNodeFromName(name: String): StationNode {
        return stationNodes.find { it.name.lowercase() == name.lowercase() } ?: throw
        IllegalArgumentException(
            "The specified station could not be found."
        )
    }

    // Uses the breadth-first search algorithm to find a list of edges
    // connecting the given source node to the given destination node. Null is
    // returned if no path can be found, including if the source and destination
    // are the same.
    private fun bfs(source: StationNode, dest: StationNode): List<Edge>? {
        // Maps each node to its parent in the BFS tree. A null value indicates
        // that a node has been discovered but has no parent (i.e., is the
        // source node). Nodes with no entries in the map have not yet been
        // discovered.
        val parentMap: HashMap<StationNode, StationNode?> = hashMapOf(
            Pair(source, null)
        )
        val queue: Queue<StationNode> = LinkedList()
        var current: StationNode

        queue.add(source)

        while (queue.isNotEmpty()) {
            current = queue.remove()
            for (toNode in current.outgoingEdges.values.map { it.destNode }) {
                if (!parentMap.containsKey(toNode)) {
                    parentMap[toNode] = current
                    if (toNode === dest) {
                        break
                    }
                    queue.add(toNode)
                }
            }
        }

        // If no path could be found, including if the source and destination
        // nodes were the same, return null.
        if (parentMap[dest] == null) {
            return null
        }

        val path: MutableList<Edge> = mutableListOf()
        var parent: StationNode
        current = dest

        // Traverses backwards through the parent of each node along the path
        // to find the path from the source to the destination.
        while (parentMap.getValue(current) != null) {
            // This non-null assertion is safe because it mirrors the while loop
            // condition.
            parent = parentMap.getValue(current)!!
            path.add(
                parent.outgoingEdges.getValue(current.stationId)
            )
            current = parent
        }

        return path.reversed()
    }

    // Converts each edge in the given path to "directions": a pair consisting
    // of the route ID to take to the next stop, and the name of that stop. Uses
    // a simple greedy algorithm that always stays on the same route until it is
    // forced to switch routes.
    private fun getDirectionsFromEdgePath(path: List<Edge>): List<Pair<String, String>> {
        val directions: MutableList<Pair<String, String>> = mutableListOf()
        var currentRouteId: String? = null

        for (edge in path) {
            // When a transfer (or initial boarding) is required, picks any
            // valid route to board. Otherwise, does not transfer routes if not
            // required to do so.
            if (currentRouteId == null || currentRouteId !in edge.routeIds) {
                currentRouteId = edge.routeIds.first()
            }
            directions.add(Pair(
                currentRouteId,
                edge.destNode.name
            ))
        }

        return directions
    }

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