package fail.kirraobj.hm

object Inference {

    private fun analyse(node: Expr, env: Map<String, Type>, scoped: Set<Type.TypeVariable> = emptySet()): Type {
        return when (node) {
            is Expr.Identifier -> getType(node.name, env, scoped)
            is Expr.Apply -> {
                val function = analyse(node.function, env, scoped)
                val arg = analyse(node.argument, env, scoped)
                val result = Type.TypeVariable()
                unify(Type.Function(arg, result), function)
                result
            }
            is Expr.Lambda -> {
                val arg = Type.TypeVariable()
                val newEnv = env + (node.variable to arg)
                val newScoped = scoped + arg
                val result = analyse(node.body, newEnv, newScoped)
                Type.Function(arg, result)
            }
            is Expr.Let -> {
                val def = analyse(node.definition, env, scoped)
                val newEnv = env + (node.variable to def)
                analyse(node.body, newEnv, scoped)
            }
            is Expr.LetRec -> {
                val type = Type.TypeVariable()
                val newEnv = env + (node.variable to type)
                val newScoped = scoped + type
                val def = analyse(node.definition, newEnv, newScoped)
                unify(type, def)
                analyse(node.body, newEnv, newScoped)
            }
        }
    }

    private fun getType(name: String, env: Map<String, Type>, scoped: Set<Type.TypeVariable>): Type {
        val type = env[name]
        if (type == null && isIntegerLiteral(name)) {
            return INTEGER
        }
        return fresh(type ?: error("Undefined symbol: $name"), scoped)
    }

    private fun fresh(
        type: Type,
        scoped: Set<Type.TypeVariable> = emptySet(),
        mappings: MutableMap<Type.TypeVariable, Type.TypeVariable> = mutableMapOf(),
    ): Type {
        return when (type) {
            is Type.TypeVariable -> when {
                type in scoped -> type
                else -> mappings.getOrPut(type) { Type.TypeVariable() }
            }
            is Type.TypeOperator -> Type.TypeOperator(type.name, type.types.map { fresh(it, scoped, mappings) })
        }
    }

    private fun unify(a: Type, b: Type) {
        val typeA = prune(a)
        val typeB = prune(b)

        when {
            typeA is Type.TypeVariable && typeA != typeB -> {
                if (isCyclicType(typeA, typeB)) {
                    error("Recursive unification: $typeA vs $typeB")
                }
                typeA.instance = typeB
            }
            typeB is Type.TypeVariable && typeB != typeA -> {
                unify(typeB, typeA)
            }
            typeA is Type.TypeOperator && typeB is Type.TypeOperator -> {
                if (typeA.name != typeB.name || typeA.types.size != typeB.types.size) {
                    error("Type mismatch: $typeA vs $typeB")
                }
                typeA.types
                    .zip(typeB.types)
                    .forEach { (a, b) -> unify(a, b) }
            }
            else -> error("Unification failed between $typeA and $typeB")
        }
    }

    private fun prune(type: Type): Type {
        if (type is Type.TypeVariable && type.instance != null) {
            type.instance = prune(type.instance!!)
            return type.instance ?: type
        }
        return type
    }

    private fun isCyclicType(typeVar: Type.TypeVariable, type: Type): Boolean {
        return when (val pruned = prune(type)) {
            is Type.TypeVariable -> pruned == typeVar
            is Type.TypeOperator -> pruned.types.any { isCyclicType(typeVar, it) }
        }
    }

    private fun isIntegerLiteral(value: String) = value.toIntOrNull() != null

    fun eval(expr: Expr, env: Map<String, Type>) {
        val result = runCatching {
            val type = analyse(expr, env)
            "$expr : $type"
        }
        println(result.getOrThrow())
    }
}