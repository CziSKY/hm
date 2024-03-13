package fail.kirraobj.hm

sealed class Expr {

    class Lambda(val variable: String, val body: Expr) : Expr() {

        override fun toString() = "(Function $variable => $body)"
    }

    class Identifier(val name: String) : Expr() {

        override fun toString() = name
    }

    class Apply(val function: Expr, val argument: Expr) : Expr() {

        override fun toString() = "($function $argument)"
    }

    class Let(val variable: String, val definition: Expr, val body: Expr) : Expr() {

        override fun toString() = "(Let $variable = $definition in $body)"
    }

    class LetRec(val variable: String, val definition: Expr, val body: Expr) : Expr() {

        override fun toString() = "(Letrec $variable = $definition in $body)"
    }
}