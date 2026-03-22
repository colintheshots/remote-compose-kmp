package androidx.compose.remote.core.operations.layout.utils

object DebugLog {

    const val DEBUG_LAYOUT_ON = false

    open class Node(val parent: Node?, val name: String) {
        var endString: String = "$name DONE"
        val list: ArrayList<Node> = ArrayList()

        init {
            parent?.add(this)
        }

        fun add(node: Node) {
            list.add(node)
        }
    }

    class LogNode(parent: Node?, name: String) : Node(parent, name)

    var node: Node = Node(null, "Root")
    var currentNode: Node = node

    fun clear() {
        node = Node(null, "Root")
        currentNode = node
    }

    fun s(valueSupplier: StringValueSupplier) {
        if (DEBUG_LAYOUT_ON) {
            currentNode = Node(currentNode, valueSupplier.getString())
        }
    }

    fun log(valueSupplier: StringValueSupplier) {
        if (DEBUG_LAYOUT_ON) {
            LogNode(currentNode, valueSupplier.getString())
        }
    }

    fun e() {
        if (DEBUG_LAYOUT_ON) {
            currentNode = currentNode.parent ?: node
        }
    }

    fun e(valueSupplier: StringValueSupplier) {
        if (DEBUG_LAYOUT_ON) {
            currentNode.endString = valueSupplier.getString()
            currentNode = currentNode.parent ?: node
        }
    }

    fun printNode(indent: Int, node: Node, builder: StringBuilder) {
        if (DEBUG_LAYOUT_ON) {
            val indentation = buildString {
                repeat(indent) { append("| ") }
            }
            if (node.list.isNotEmpty()) {
                builder.append(indentation).append(node.name).append("\n")
                for (c in node.list) {
                    printNode(indent + 1, c, builder)
                }
                builder.append(indentation).append(node.endString).append("\n")
            } else {
                if (node is LogNode) {
                    builder.append(indentation).append("     ").append(node.name).append("\n")
                } else {
                    builder.append(indentation).append("-- ").append(node.name)
                        .append(" : ").append(node.endString).append("\n")
                }
            }
        }
    }

    fun display() {
        if (DEBUG_LAYOUT_ON) {
            val builder = StringBuilder()
            printNode(0, node, builder)
            println("\n$builder")
        }
    }
}
