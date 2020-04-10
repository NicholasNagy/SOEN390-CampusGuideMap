package com.droidhats.mapprocessor

class Vertex(circle: Circle, endPoint: Pair<Double, Double>) {
    val pos: Pair<Double, Double> = Pair(circle.cx, circle.cy)
    val heuristic: Double = getDistance(pos, endPoint)
    var prev: Vertex? = null
    private var value: Double? = null
    private var sum: Double? = null
    val neighbors: MutableList<Vertex> = mutableListOf()

    fun setValue(value: Double) {
        this.value = value
        sum = value + heuristic
    }
    fun getValue(): Double? = value
    fun getSum(): Double? = sum
    fun removeFromNeighbors(poppedNode: Vertex) {
        for (neighborInd in neighbors.indices) {
            if (neighbors[neighborInd] == poppedNode) {
                neighbors.removeAt(neighborInd)
                return
            }
        }
    }
}

/**
 * A priority queue of next nodes to examine.
 * They are ordered from lowest sum to highest
 */
class PriorityQueue {
    private val queue: MutableList<Vertex> = mutableListOf()
    fun insert(vertex: Vertex) {
        for (indVertex in queue.indices) {
            if (vertex.getSum()!! < queue[indVertex].getSum()!!) {
                // set it in its proper position
                queue.add(indVertex, vertex)
                return
            }
        }
        // case that it has the biggest sum
        queue.add(vertex)
    }

    fun pop(): Vertex {
        return queue.removeAt(0)
    }

    fun isNotWithin(vertex: Vertex): Boolean {
        return vertex !in queue
    }

    fun removeVertex(vertex: Vertex) {
        queue.remove(vertex)
    }
}

fun findNearestPoint(mapElement: MapElement, pathElements: List<Vertex>): Vertex {
    var nearestNode: Vertex = pathElements[0]
    var smallestDistance: Double = getDistance(mapElement.getCenter(), nearestNode.pos)
    for (vertex in pathElements) {
        val distanceToNode = getDistance(mapElement.getCenter(), vertex.pos)
        if(distanceToNode < smallestDistance) {
            nearestNode = vertex
            smallestDistance = distanceToNode
        }
    }
    return nearestNode
}

fun A_Star(start: MapElement, end: MapElement, pathElements: MutableList<Vertex>): String {
    val startVertex = findNearestPoint(start, pathElements)
    val endVertex = findNearestPoint(end, pathElements)
    val queue = PriorityQueue()

    // A* algorithm part
    var poppedNode = startVertex
    while (poppedNode != endVertex) {
        for (neighbor in poppedNode.neighbors) {
            if (neighbor.getValue() == null || poppedNode.getValue()!! < neighbor.getValue()!!) {
                neighbor.setValue(getDistance(poppedNode.pos, neighbor.pos))
                neighbor.prev = poppedNode
                if (!queue.isNotWithin(neighbor)) {
                    queue.removeVertex(neighbor)
                }
            }
            neighbor.removeFromNeighbors(poppedNode)
            if (queue.isNotWithin(neighbor)) {
                queue.insert(neighbor)
            }
        }
        poppedNode = queue.pop()
    }

    // converting path to string
    var cur: Vertex? = endVertex
    val string: StringBuilder = StringBuilder()
    while (cur?.prev != null) {
        string.append(Path.createPath(cur.pos!!, cur.prev!!.pos))
        cur = cur.prev
    }
    string.append(Circle.getPoint(endVertex.pos.first, endVertex.pos.second))
    string.append(Circle.getPoint(startVertex.pos.first, startVertex.pos.second))

    return string.toString()
}
