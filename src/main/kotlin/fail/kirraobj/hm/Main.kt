package fail.kirraobj.hm

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        val var1 = Type.TypeVariable()
        val var2 = Type.TypeVariable()
        val pairType = Type.TypeOperator("*", listOf(var1, var2))

        val var3 = Type.TypeVariable()

        val env = mapOf(
            "pair" to Type.Function(var1, Type.Function(var2, pairType)),
            "true" to BOOL,
            "false" to BOOL,
            "cond" to Type.Function(BOOL, Type.Function(var3, Type.Function(var3, var3))),
            "zero" to Type.Function(INTEGER, BOOL),
            "pred" to Type.Function(INTEGER, INTEGER),
            "times" to Type.Function(INTEGER, Type.Function(INTEGER, INTEGER))
        )

        val example = Expr.Let(
            "g",
            Expr.Lambda("f", Expr.Identifier("5")),
            Expr.Apply(Expr.Identifier("g"), Expr.Identifier("g"))
        )

        Inference.eval(example, env)
    }
}