package fail.kirraobj.hm

sealed class Type {

    // 任意类型, 例如 Int, Bool, etc
    // 也可以用来表示 PolyMorphism, 此时的变量作为一个 hole
    // 例如 f :: a -> a, f(1) = 1 (此时 f 类型为 Int -> Int)
    class TypeVariable : Type() {

        var instance: Type? = null

        private val name by lazy {
            nextName().toString()
        }

        override fun toString() = instance?.toString() ?: name

        companion object {

            private var currentChar = 'a'

            private fun nextName(): Char {
                val name = currentChar
                currentChar++
                return name
            }
        }
    }

    // 类型构造器, 例如 ->, List
    open class TypeOperator(val name: String, val types: List<Type>) : Type() {

        override fun toString(): String = when (types.size) {
            0 -> name
            2 -> "(${types[0]} $name ${types[1]})"
            else -> "$name ${types.joinToString(" ")}"
        }
    }

    // 函数类型
    class Function(from: Type, to: Type) : TypeOperator("->", listOf(from, to))
}

val INTEGER = Type.TypeOperator("Int", emptyList())
val BOOL = Type.TypeOperator("Bool", emptyList())
val STRING = Type.TypeOperator("String", emptyList())