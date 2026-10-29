/**
 * Copyright (C) 2026 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.akehurst.omg._simple_mof_for_xmi

/**
 * Translates the OCL derivation of a property into a Kotlin expression over the generated api interfaces,
 * with the owning object named 'self'.
 *
 * Only a subset of OCL is handled: navigation, let, if, literals, boolean and comparison operators,
 * oclIsKindOf/oclAsType and the common collection operations (selectByKind, select, reject, collect, first, ...).
 * Anything else (operation calls, enumeration literals, closure, ...) makes [translate] return null,
 * so that the caller can emit a TODO instead.
 */
class OclToKotlin(private val model: MofModel) {

    private class Untranslatable(message: String) : Exception(message)

    /**
     * A translated expression: its Kotlin code and its static type. For a collection, [type] and the flags describe an element.
     * Integers are Long, as in the api; [isUnlimited] marks an UnlimitedNatural, whose '*' is -1.
     */
    private data class KExpr(
        val code: String, val type: MofType?, val isCollection: Boolean, val nullable: Boolean,
        val isBoolean: Boolean = false, val isInt: Boolean = false, val isString: Boolean = false, val isUnlimited: Boolean = false
    ) {
        fun asCollection(): KExpr = when {
            isCollection -> this
            "null" == code -> copy(code = "emptyList()", isCollection = true, nullable = false)
            nullable -> copy(code = "listOfNotNull($code)", isCollection = true, nullable = false)
            else -> copy(code = "listOf($code)", isCollection = true, nullable = false)
        }
    }

    private data class Token(val kind: String, val text: String)

    private class Scope(val parent: Scope?, val vars: Map<String, KExpr>, val implicit: KExpr?) {
        fun lookup(name: String): KExpr? = vars[name] ?: parent?.lookup(name)
        val implicitIterator: KExpr? get() = implicit ?: parent?.implicitIterator
    }

    private var tokens: List<Token> = emptyList()
    private var pos = 0
    private var varCount = 0
    private lateinit var selfType: MofType
    private var inOperation = false

    /** @return Kotlin code that computes [prop] from 'self', or null when the OCL cannot be translated */
    fun translate(prop: MofProperty, ocl: String): String? = translateOrReason(prop, ocl).first

    /** @return the Kotlin code, or null and the reason the OCL could not be translated */
    fun translateOrReason(prop: MofProperty, ocl: String): Pair<String?, String?> = try {
        Pair(translateOrThrow(prop, ocl), null)
    } catch (e: Untranslatable) {
        Pair(null, e.message)
    }

    private fun translateOrThrow(prop: MofProperty, ocl: String): String {
        val owner = prop.parentType ?: throw Untranslatable("no owner")
        inOperation = false
        val e = parseAll(owner, "self", emptyMap(), ocl)
        return convertTo(Target(prop.type, prop.isCollection, if (prop.isCollection) prop.colTypeName else "", prop.isOptional, prop.qualifiedName), e)
    }

    /**
     * Translates the OCL body of an operation into the Kotlin body of a default method of the api interface:
     * 'self' is 'this' and the parameters are in scope by name.
     * @return the Kotlin code, or null and the reason the OCL could not be translated
     */
    fun translateOperationOrReason(op: MofOperation, ocl: String): Pair<String?, String?> = try {
        val owner = op.parentType ?: throw Untranslatable("no owner")
        val ret = op.returnParameter ?: throw Untranslatable("no result")
        val retType = ret.type ?: throw Untranslatable("unknown result type")
        val params = op.parameters.mapIndexed { i, p ->
            val t = p.type ?: throw Untranslatable("unknown parameter type")
            p.name to typed(KExpr(p.kotlinName(i), t, p.isCollection, p.isOptional))
        }.toMap()
        inOperation = true
        val e = parseAll(owner, "this", params, ocl)
        Pair(convertTo(Target(retType, ret.isCollection, if (ret.isCollection) op.resultColTypeName else "", op.resultOptional, op.toString()), e), null)
    } catch (e: Untranslatable) {
        Pair(null, e.message)
    }

    private fun parseAll(owner: MofType, selfCode: String, vars: Map<String, KExpr>, ocl: String): KExpr {
        selfType = owner
        varCount = 0
        tokens = tokenize(ocl)
        pos = 0
        val self = KExpr(selfCode, selfType, false, false)
        val e = parseExpr(Scope(Scope(null, mapOf("self" to self), null), vars, null))
        if (pos != tokens.size) throw Untranslatable("unexpected '${tokens[pos].text}'")
        return e
    }

    /** what a translated expression must be converted to */
    private data class Target(val type: MofType, val isCollection: Boolean, val colTypeName: String, val isOptional: Boolean, val what: String)

    // --- conversion to the property type ---

    private fun convertTo(t: Target, e: KExpr): String {
        val target = t.type
        return when {
            t.isCollection -> {
                val c = e.asCollection()
                val elems = when {
                    "emptyList()" == c.code -> "emptyList<${target.validName}>()"
                    null != c.type && c.type.isSubtypeOf(target) -> c.code
                    else -> "${c.code}.map { it as ${target.validName} }"
                }
                when (t.colTypeName) {
                    "Set" -> "$elems.toSet()"
                    "OrderedSet" -> "$elems.toOrderedSet()"
                    else -> "$elems.toList()"
                }
            }

            e.isCollection -> throw Untranslatable("collection given for single value")
            else -> {
                val typed = when {
                    e.isBoolean || e.isInt || e.isString -> e.code
                    null != e.type && e.type.isSubtypeOf(target) -> e.code
                    else -> "(${e.code} as ${target.validName}?)"
                }
                when {
                    t.isOptional -> typed
                    e.nullable || typed != e.code -> "($typed ?: error(\"'${t.what}' gave null\"))"
                    else -> typed
                }
            }
        }
    }

    // --- lexer ---

    private fun tokenize(text: String): List<Token> {
        val result = mutableListOf<Token>()
        var i = 0
        while (i < text.length) {
            val c = text[i]
            when {
                c.isWhitespace() -> i++
                text.startsWith("--", i) -> { // comment to end of line
                    while (i < text.length && text[i] != '\n') i++
                }

                text.startsWith("_'", i) -> { // a quoted name, e.g. _'in'
                    val end = text.indexOf('\'', i + 2)
                    if (end < 0) throw Untranslatable("unterminated quoted name")
                    result.add(Token("qid", text.substring(i + 2, end)))
                    i = end + 1
                }

                c == '→' -> { // seen in place of '->'
                    result.add(Token("sym", "->")); i++
                }

                c.isLetter() || c == '_' -> {
                    val s = i
                    while (i < text.length && (text[i].isLetterOrDigit() || text[i] == '_')) i++
                    result.add(Token("id", text.substring(s, i)))
                }

                c.isDigit() -> {
                    val s = i
                    while (i < text.length && text[i].isDigit()) i++
                    if (i < text.length && text[i] == '.' && i + 1 < text.length && text[i + 1].isDigit()) throw Untranslatable("real literal")
                    result.add(Token("int", text.substring(s, i)))
                }

                c == '\'' -> { // a string; OCL escapes such as \' and \\ are allowed
                    val sb = StringBuilder()
                    i++
                    while (i < text.length && text[i] != '\'') {
                        if (text[i] == '\\' && i + 1 < text.length) {
                            sb.append(
                                when (val e = text[i + 1]) {
                                    'n' -> '\n'
                                    't' -> '\t'
                                    'r' -> '\r'
                                    'b' -> '\b'
                                    'f' -> '\u000C'
                                    else -> e
                                }
                            )
                            i += 2
                        } else {
                            sb.append(text[i]); i++
                        }
                    }
                    if (i >= text.length) throw Untranslatable("unterminated string")
                    result.add(Token("str", sb.toString()))
                    i++
                }

                else -> {
                    val two = if (i + 1 < text.length) text.substring(i, i + 2) else ""
                    when (two) {
                        "->", "<>", "<=", ">=", "::", ".." -> {
                            result.add(Token("sym", two)); i += 2
                        }

                        else -> {
                            if (c !in ".,(){}|=<>:;+-*/") throw Untranslatable("unexpected character '$c'")
                            result.add(Token("sym", c.toString())); i++
                        }
                    }
                }
            }
        }
        return result
    }

    // --- parser / translator ---

    private fun peek(offset: Int = 0): Token? = tokens.getOrNull(pos + offset)
    private fun isSym(s: String, offset: Int = 0) = peek(offset)?.let { it.kind == "sym" && it.text == s } ?: false
    private fun isId(s: String, offset: Int = 0) = peek(offset)?.let { it.kind == "id" && it.text == s } ?: false
    private fun expectSym(s: String) {
        if (!isSym(s)) throw Untranslatable("expected '$s'")
        pos++
    }

    private fun expectId(s: String) {
        if (!isId(s)) throw Untranslatable("expected '$s'")
        pos++
    }

    private fun nextId(): String {
        val t = peek() ?: throw Untranslatable("unexpected end")
        if (t.kind != "id" && t.kind != "qid") throw Untranslatable("expected a name, found '${t.text}'")
        pos++
        return t.text
    }

    private fun parseExpr(scope: Scope): KExpr = when {
        isId("let") -> parseLet(scope)
        else -> parseImplies(scope)
    }

    private fun parseLet(scope: Scope): KExpr {
        expectId("let")
        val vars = linkedMapOf<String, KExpr>()
        val decls = mutableListOf<String>()
        var s = scope
        do {
            if (isSym(",")) pos++
            val name = nextId()
            val declared = if (isSym(":")) {
                pos++; parseTypeExpr()
            } else null
            expectSym("=")
            val e = parseExpr(s)
            val kName = "_$name"
            decls.add("val $kName = ${declaredEmpty(e, declared)}")
            vars[name] = e.copy(code = kName)
            s = Scope(s, mapOf(name to vars[name]!!), null)
        } while (isSym(","))
        expectId("in")
        val body = parseExpr(s)
        return body.copy(code = "run { ${decls.joinToString("; ")}; ${body.code} }")
    }

    /** skips a type such as 'Feature', 'Sequence(Feature)' or 'OrderedSet(Type)' */
    private fun skipTypeExpr() {
        parseTypeExpr()
    }

    private fun parseImplies(scope: Scope): KExpr {
        var left = parseOr(scope)
        while (isId("implies")) {
            pos++
            val right = parseOr(scope)
            left = bool("(!${boolCode(left)} || ${boolCode(right)})")
        }
        return left
    }

    private fun parseOr(scope: Scope): KExpr {
        var left = parseAnd(scope)
        while (isId("or") || isId("xor")) {
            val op = if (isId("or")) "||" else "!="
            pos++
            val right = parseAnd(scope)
            left = bool("(${boolCode(left)} $op ${boolCode(right)})")
        }
        return left
    }

    private fun parseAnd(scope: Scope): KExpr {
        var left = parseEquality(scope)
        while (isId("and")) {
            pos++
            val right = parseEquality(scope)
            left = bool("(${boolCode(left)} && ${boolCode(right)})")
        }
        return left
    }

    private fun parseEquality(scope: Scope): KExpr {
        var left = parseRelational(scope)
        while (isSym("=") || isSym("<>")) {
            val op = if (isSym("=")) "==" else "!="
            pos++
            val right = parseRelational(scope)
            if (left.isCollection || right.isCollection) throw Untranslatable("collection equality")
            left = bool("(${left.code} $op ${right.code})")
        }
        return left
    }

    private fun parseRelational(scope: Scope): KExpr {
        var left = parseAdditive(scope)
        while (isSym("<") || isSym(">") || isSym("<=") || isSym(">=")) {
            val op = peek()!!.text
            pos++
            val right = parseAdditive(scope)
            if (left.nullable || right.nullable || !(left.isInt && right.isInt)) throw Untranslatable("comparison of non integers")
            left = bool(compare(left, op, right))
        }
        return left
    }

    private fun parseAdditive(scope: Scope): KExpr {
        var left = parseMultiplicative(scope)
        while (isSym("+") || isSym("-")) {
            val op = peek()!!.text
            pos++
            val right = parseMultiplicative(scope)
            left = when {
                "+" == op && left.isString && right.isString -> str("(${left.code} + ${right.code})")
                left.isUnlimited || right.isUnlimited -> throw Untranslatable("arithmetic on UnlimitedNatural")
                left.isInt && right.isInt && !left.nullable && !right.nullable -> int("(${left.code} $op ${right.code})")
                else -> throw Untranslatable("arithmetic on non integers")
            }
        }
        return left
    }

    private fun parseMultiplicative(scope: Scope): KExpr {
        val left = parseUnary(scope)
        if (isSym("*") || isSym("/")) throw Untranslatable("multiplication")
        return left
    }

    private fun parseUnary(scope: Scope): KExpr = when {
        isId("not") -> {
            pos++
            val e = parseUnary(scope)
            bool("!${boolCode(e)}")
        }

        isSym("-") -> {
            pos++
            val e = parseUnary(scope)
            if (!e.isInt) throw Untranslatable("negation of non integer")
            int("(-${e.code})")
        }

        else -> parsePostfix(scope)
    }

    private fun parsePostfix(scope: Scope): KExpr {
        var e = parsePrimary(scope)
        while (true) {
            e = when {
                isSym(".") -> {
                    pos++; navigate(e, scope)
                }

                isSym("->") -> {
                    pos++; collectionOp(e.asCollection(), scope)
                }

                else -> return e
            }
        }
    }

    private fun parsePrimary(scope: Scope): KExpr {
        val t = peek() ?: throw Untranslatable("unexpected end")
        return when {
            t.kind == "int" -> {
                pos++; int("${t.text}L")
            }

            isSym("*") -> { // UnlimitedNatural unlimited
                pos++; int("-1L").copy(isUnlimited = true)
            }

            isId("let") -> parseLet(scope)

            t.kind == "str" -> {
                pos++
                val s = t.text.replace("\\", "\\\\").replace("\"", "\\\"").replace("$", "\\$").replace("\n", "\\n").replace("\t", "\\t").replace("\r", "\\r")
                str("\"$s\"")
            }

            isSym("(") -> {
                pos++
                val e = parseExpr(scope)
                expectSym(")")
                e.copy(code = "(${e.code})")
            }

            isId("true") || isId("false") -> {
                pos++; bool(t.text)
            }

            isId("null") -> {
                pos++; KExpr("null", null, false, true)
            }

            isId("if") -> parseIf(scope)
            isId("Sequence") || isId("OrderedSet") || isId("Set") || isId("Bag") -> {
                if (isSym("{", 1) || isSym("(", 1)) parseCollectionLiteral(scope) else bareName(scope)
            }

            t.kind == "id" || t.kind == "qid" -> bareName(scope)
            else -> throw Untranslatable("unexpected '${t.text}'")
        }
    }

    private fun parseIf(scope: Scope): KExpr {
        expectId("if")
        val c = parseExpr(scope)
        expectId("then")
        var a = parseExpr(scope)
        expectId("else")
        var b = parseExpr(scope)
        expectId("endif")
        if (a.isCollection || b.isCollection) {
            a = a.asCollection(); b = b.asCollection()
        }
        val type = when {
            null == a.type -> b.type
            null == b.type -> a.type
            a.type == b.type -> a.type
            a.isString && b.isString -> a.type
            a.isBoolean && b.isBoolean -> a.type
            a.isInt && b.isInt -> if (a.isUnlimited) a.type else b.type
            a.type.isSubtypeOf(b.type) -> b.type
            b.type.isSubtypeOf(a.type) -> a.type
            else -> throw Untranslatable("if branches have unrelated types")
        }
        // a null branch takes the kind of the other branch
        fun both(f: (KExpr) -> Boolean) = (f(a) || "null" == a.code) && (f(b) || "null" == b.code)
        return KExpr(
            "(if (${boolCode(c)}) ${a.code} else ${b.code})", type, a.isCollection, a.nullable || b.nullable,
            both { it.isBoolean }, both { it.isInt }, both { it.isString }, a.isUnlimited || b.isUnlimited
        )
    }

    private fun parseCollectionLiteral(scope: Scope): KExpr {
        nextId()
        if (isSym("(")) {
            pos++; skipTypeExpr(); expectSym(")")
        }
        expectSym("{")
        val elems = mutableListOf<KExpr>()
        while (!isSym("}")) {
            if (elems.isNotEmpty()) expectSym(",")
            val e = parseExpr(scope)
            if (e.isCollection) throw Untranslatable("nested collection literal")
            if (isSym("..")) { // a range, e.g. Sequence{1..n}
                pos++
                val to = parseExpr(scope)
                if (!e.isInt || !to.isInt || e.nullable || to.nullable) throw Untranslatable("non integer range")
                expectSym("}")
                if (elems.isNotEmpty()) throw Untranslatable("range after other elements")
                return KExpr("(${e.code}..${to.code}).toList()", null, true, false, isInt = true)
            }
            elems.add(e)
        }
        expectSym("}")
        return when {
            elems.isEmpty() -> KExpr("emptyList()", null, true, false)
            else -> elems.first().copy(code = "listOfNotNull(${elems.joinToString { it.code }})", isCollection = true, nullable = false)
        }
    }

    /** an unqualified name: a variable, or a property of the implicit iterator or of self */
    private fun bareName(scope: Scope): KExpr {
        val name = nextId()
        val itr = scope.implicitIterator
        if (isSym("(") && null != itr && name in oclTypeOps) return oclTypeOp(itr, name) // e.g. select(oclIsKindOf(Package))
        if (isSym("(")) {
            // an operation of the implicit iterator or of self
            itr?.type?.let { t -> findOperation(t, name, peekArgCount()) }?.let { return callOperation(itr, it, scope) }
            val self = scope.lookup("self")!!
            val op = findOperation(selfType, name, peekArgCount()) ?: throw Untranslatable("unknown operation '$name'")
            return callOperation(self, op, scope)
        }
        if (isSym("::")) throw Untranslatable("enumeration literal")
        scope.lookup(name)?.let { return it }
        scope.implicitIterator?.let { itr ->
            itr.type?.let { findProperty(it, name) }?.let { return navigateProperty(itr, it) }
        }
        val self = scope.lookup("self")!!
        val p = findProperty(selfType, name) ?: throw Untranslatable("unknown name '$name'")
        return navigateProperty(self, p)
    }

    private fun navigate(recv: KExpr, scope: Scope): KExpr {
        val name = nextId()
        if (isSym("::")) return qualifiedCall(recv, name, scope)
        if (isSym("(")) {
            if (name in oclTypeOps) return oclTypeOp(recv, name)
            primitiveOp(recv, name, scope)?.let { return it }
            val t = recv.type ?: throw Untranslatable("operation call on unknown type")
            val op = findOperation(t, name, peekArgCount()) ?: throw Untranslatable("unknown operation '$name' of '${t.name}'")
            return callOperation(recv, op, scope)
        }
        val type = recv.type ?: throw Untranslatable("navigation from unknown type")
        val p = findProperty(type, name) ?: throw Untranslatable("unknown property '$name' of '${type.name}'")
        return navigateProperty(recv, p)
    }

    private val oclTypeOps = setOf("oclIsKindOf", "oclIsTypeOf", "oclAsType")

    /** operations of the primitive types; the next token is '(' */
    private fun primitiveOp(recv: KExpr, name: String, scope: Scope): KExpr? = when {
        recv.isCollection -> null
        "toString" == name && (recv.isInt || recv.isBoolean) && !recv.nullable && !recv.isUnlimited -> {
            expectSym("("); expectSym(")"); str("${recv.code}.toString()")
        }

        "concat" == name && recv.isString -> {
            // OCL: concat with null is invalid, so a null here is an error
            expectSym("(")
            val x = parseExpr(scope)
            expectSym(")")
            if (!x.isString || x.isCollection) throw Untranslatable("concat of non String")
            fun nn(e: KExpr) = if (e.nullable) "(${e.code} ?: error(\"concat with null\"))" else e.code
            str("(${nn(recv)} + ${nn(x)})")
        }

        "size" == name && recv.isString && !recv.nullable -> {
            expectSym("("); expectSym(")"); int("${recv.code}.length.toLong()")
        }

        else -> null
    }

    /**
     * 'self.T::op(args)': the operation as defined by supertype T (a Kotlin super call).
     * Only possible in an api default method, on 'this', when T is a direct supertype.
     */
    private fun qualifiedCall(recv: KExpr, typeName: String, scope: Scope): KExpr {
        expectSym("::")
        val name = nextId()
        if (!isSym("(")) throw Untranslatable("qualified property")
        if (!inOperation || "this" != recv.code) throw Untranslatable("qualified operation call on other than self")
        val t = model.allClasses.firstOrNull { it.name == typeName } ?: throw Untranslatable("unknown type '$typeName'")
        if (!selfType.superTypes.contains(t)) throw Untranslatable("qualified call to an indirect supertype")
        val op = findOperation(t, name, peekArgCount()) ?: throw Untranslatable("unknown operation '$name' of '$typeName'")
        return callOperation(recv.copy(code = "super<${t.validName}>"), op, scope)
    }

    /** oclIsKindOf / oclAsType applied to [recv]; the next token is '(' */
    private fun oclTypeOp(recv: KExpr, name: String): KExpr {
        expectSym("(")
        return when (name) {
                "oclIsKindOf" -> {
                    val t = typeArg()
                    if (recv.isCollection) throw Untranslatable("oclIsKindOf on collection")
                    bool("(${recv.code} is ${t.validName})")
                }

                "oclIsTypeOf" -> {
                    // the api types are interfaces, so 'exactly T' is 'a T and none of the subtypes of T'
                    val t = typeArg()
                    if (recv.isCollection) throw Untranslatable("oclIsTypeOf on collection")
                    val subs = model.allClasses.filter { it != t && it.isSubtypeOf(t) }.sortedBy { it.name }
                    bool("(${(listOf("${recv.code} is ${t.validName}") + subs.map { "${recv.code} !is ${it.validName}" }).joinToString(" && ")})")
                }

                "oclAsType" -> {
                    val t = typeArg()
                    when {
                        recv.isCollection -> recv.copy(code = "${recv.code}.map { it as ${t.validName} }", type = t)
                        recv.nullable -> recv.copy(code = "(${recv.code} as ${t.validName}?)", type = t)
                        else -> recv.copy(code = "(${recv.code} as ${t.validName})", type = t)
                    }
                }

                else -> throw Untranslatable("operation call '$name'")
            }
    }

    private fun navigateProperty(recv: KExpr, p: MofProperty): KExpr {
        if (p.type is MofEnum) throw Untranslatable("enumeration typed property")
        return typed(when {
            recv.isCollection -> when {
                p.isCollection -> KExpr("${recv.code}.flatMap { it.${p.validName} }", p.type, true, false)
                else -> KExpr("${recv.code}.mapNotNull { it.${p.validName} }", p.type, true, false)
            }

            recv.nullable -> when {
                p.isCollection -> KExpr("(${recv.code}?.${p.validName} ?: emptyList())", p.type, true, false)
                else -> KExpr("${recv.code}?.${p.validName}", p.type, false, true)
            }

            else -> when {
                p.isCollection -> KExpr("${recv.code}.${p.validName}", p.type, true, false)
                else -> KExpr("${recv.code}.${p.validName}", p.type, false, p.isOptional)
            }
        })
    }

    private fun collectionOp(recv: KExpr, scope: Scope): KExpr {
        val op = nextId()
        expectSym("(")
        val result = when (op) {
            "selectByKind", "selectAsKind" -> {
                val t = typeArgNoClose()
                KExpr("${recv.code}.filterIsInstance<${t.validName}>()", t, true, false)
            }

            "closure" -> {
                // OCL: the elements reached by applying the body transitively, starting from the source;
                // the source elements are included only when reached. The body type must conform to the source element type.
                val (v, body) = iteratorBody(recv, scope)
                val bt = body.type ?: throw Untranslatable("closure body of unknown type")
                if (null == recv.type || !bt.isSubtypeOf(recv.type)) throw Untranslatable("closure body type does not conform to the source")
                val first = if (body.isCollection) "${recv.code}.flatMap { $v -> ${body.code} }" else "${recv.code}.mapNotNull { $v -> ${body.code} }"
                val step = if (body.isCollection) "(${body.code}).toList()" else "listOfNotNull(${body.code})"
                KExpr("$first.distinct().transitiveClosure { $v -> $step }", bt, true, false)
            }

            "select", "reject", "forAll", "exists", "collect" -> {
                val (v, body) = iteratorBody(recv, scope)
                when (op) {
                    "select" -> recv.copy(code = "${recv.code}.filter { $v -> ${boolCode(body)} }")
                    "reject" -> recv.copy(code = "${recv.code}.filterNot { $v -> ${boolCode(body)} }")
                    "forAll" -> bool("${recv.code}.all { $v -> ${boolCode(body)} }")
                    "exists" -> bool("${recv.code}.any { $v -> ${boolCode(body)} }")
                    else -> when {
                        body.isCollection -> KExpr("${recv.code}.flatMap { $v -> ${body.code} }", body.type, true, false)
                        else -> KExpr("${recv.code}.mapNotNull { $v -> ${body.code} }", body.type, true, false)
                    }
                }
            }

            "any" -> {
                val (v, body) = iteratorBody(recv, scope)
                recv.copy(code = "${recv.code}.firstOrNull { $v -> ${boolCode(body)} }", isCollection = false, nullable = true)
            }

            "iterate" -> iterate(recv, scope)

            "excluding" -> {
                val x = parseExpr(scope)
                if (x.isCollection) throw Untranslatable("excluding of collection")
                recv.copy(code = "${recv.code}.filter { it != ${x.code} }")
            }

            "intersection" -> {
                val x = parseExpr(scope).asCollection()
                commonType(recv.type, x.type)
                recv.copy(code = "${recv.code}.filter { it in ${x.code} }.distinct()")
            }

            "includesAll", "excludesAll" -> {
                val x = parseExpr(scope).asCollection()
                bool(if ("includesAll" == op) "${x.code}.all { it in ${recv.code} }" else "${x.code}.none { it in ${recv.code} }")
            }

            // collections of collections are not formed by the translation, so flatten has nothing to do
            "flatten" -> recv
            "isEmpty" -> bool("${recv.code}.none()")
            "notEmpty" -> bool("${recv.code}.any()")
            "size" -> int("${recv.code}.count().toLong()")
            "first" -> recv.copy(code = "${recv.code}.firstOrNull()", isCollection = false, nullable = true)
            "last" -> recv.copy(code = "${recv.code}.lastOrNull()", isCollection = false, nullable = true)
            "at" -> {
                val i = parseExpr(scope)
                if (!i.isInt || i.nullable) throw Untranslatable("non integer index")
                recv.copy(code = "${recv.code}.elementAtOrNull((${i.code}).toInt() - 1)", isCollection = false, nullable = true)
            }

            "indexOf" -> {
                val x = parseExpr(scope)
                int("(${recv.code}.indexOf(${x.code}) + 1).toLong()")
            }

            "union", "including", "append" -> {
                val x = parseExpr(scope).asCollection()
                KExpr("(${recv.code} + ${x.code})", commonType(recv.type, x.type), true, false)
            }

            "prepend" -> {
                val x = parseExpr(scope).asCollection()
                KExpr("(${x.code} + ${recv.code})", commonType(recv.type, x.type), true, false)
            }

            "includes", "excludes" -> {
                val x = parseExpr(scope)
                if (x.isCollection) throw Untranslatable("includes of collection")
                val c = "${recv.code}.contains(${x.code})"
                bool(if (op == "includes") c else "!$c")
            }

            "asOrderedSet", "asSet" -> recv.copy(code = "${recv.code}.distinct()")
            "asSequence", "asBag" -> recv
            "subSequence" -> {
                val from = parseExpr(scope)
                expectSym(",")
                val to = parseExpr(scope)
                if (!from.isInt || !to.isInt) throw Untranslatable("non integer subSequence bounds")
                recv.copy(code = "${recv.code}.toList().subList((${from.code}).toInt() - 1, (${to.code}).toInt())")
            }

            else -> throw Untranslatable("collection operation '$op'")
        }
        expectSym(")")
        return result
    }

    /** parses 'v | body', 'v : T | body' or 'body' (implicit iterator); the closing ')' is left for the caller */
    private fun iteratorBody(recv: KExpr, scope: Scope): Pair<String, KExpr> {
        val elem = recv.copy(code = "", isCollection = false, nullable = false)
        val explicit = peek()?.kind == "id" && (isSym("|", 1) || isSym(":", 1))
        return if (explicit) {
            val name = nextId()
            if (isSym(":")) {
                pos++; parseTypeExpr()
            }
            expectSym("|")
            val kName = "_$name"
            val body = parseExpr(Scope(scope, mapOf(name to elem.copy(code = kName)), null))
            Pair(kName, body)
        } else {
            val kName = "_e${++varCount}"
            val body = parseExpr(Scope(scope, emptyMap(), elem.copy(code = kName)))
            Pair(kName, body)
        }
    }

    private fun typeArg(): MofType {
        val t = typeArgNoClose()
        expectSym(")")
        return t
    }

    private fun typeArgNoClose(): MofType {
        val name = nextId()
        if (isSym("::")) throw Untranslatable("qualified type name")
        if (isSym(".")) throw Untranslatable("type given by an expression (e.g. oclType())")
        return model.allClasses.firstOrNull { it.name == name } ?: throw Untranslatable("unknown type '$name'")
    }

    private fun commonType(a: MofType?, b: MofType?): MofType? = when {
        null == a -> b
        null == b -> a
        a.isSubtypeOf(b) -> b
        b.isSubtypeOf(a) -> a
        else -> throw Untranslatable("unrelated element types")
    }

    /** the number of arguments of the call whose '(' is the next token (counts top level commas) */
    private fun peekArgCount(): Int {
        var depth = 0
        var count = 0
        var i = pos
        while (i < tokens.size) {
            val t = tokens[i]
            if (t.kind == "sym" && (t.text == "(" || t.text == "{")) depth++
            if (t.kind == "sym" && (t.text == ")" || t.text == "}")) {
                depth--
                if (0 == depth) return if (i == pos + 1) 0 else count + 1
            }
            if (1 == depth && t.kind == "sym" && t.text == ",") count++
            i++
        }
        throw Untranslatable("unterminated call")
    }

    /** a generated operation named [name] with [argCount] parameters visible on [type]; the most specific one */
    private fun findOperation(type: MofType, name: String, argCount: Int): MofOperation? {
        val candidates = (listOf(type) + type.allSuperTypes)
            .flatMap { (it as? MofAbstractType)?.operations ?: emptyList() }
            .filter { it.name == name && it.isGenerated && it.parameters.size == argCount }
        return candidates.firstOrNull { c -> candidates.all { o -> c.parentType!!.isSubtypeOf(o.parentType!!) } }
            ?: candidates.firstOrNull()
    }

    /** a call of [op] on [recv]; the next token is '(' */
    private fun callOperation(recv: KExpr, op: MofOperation, scope: Scope): KExpr {
        expectSym("(")
        val args = op.parameters.mapIndexed { i, p ->
            if (i > 0) expectSym(",")
            val a = parseExpr(scope)
            when {
                p.isCollection -> {
                    val c0 = a.asCollection()
                    // an empty literal needs its element type
                    val c = if ("emptyList()" == c0.code) c0.copy(code = "emptyList<${p.type?.validName ?: "Any"}>()") else c0
                    when (p.colTypeName) {
                        "Set" -> "${c.code}.toSet()"
                        "OrderedSet" -> "${c.code}.toOrderedSet()"
                        else -> "${c.code}.toList()"
                    }
                }

                a.isCollection -> throw Untranslatable("collection argument for single valued parameter")
                "null" == a.code && !p.isOptional -> throw Untranslatable("null argument for a required parameter")
                a.nullable && !p.isOptional -> "(${a.code} ?: error(\"argument '${p.name}' of '${op.name}' is null\"))"
                else -> a.code
            }
        }
        expectSym(")")
        val ret = op.returnParameter ?: throw Untranslatable("operation '${op.name}' has no result")
        val rt = ret.type ?: throw Untranslatable("operation '${op.name}' has an unknown result type")
        if (rt is MofEnum) throw Untranslatable("enumeration typed result")
        val call = "${op.validName}(${args.joinToString(", ")})"
        return typed(when {
            recv.isCollection -> when {
                ret.isCollection -> KExpr("${recv.code}.flatMap { it.$call }", rt, true, false)
                else -> KExpr("${recv.code}.mapNotNull { it.$call }", rt, true, false)
            }

            recv.nullable -> when {
                ret.isCollection -> KExpr("(${recv.code}?.$call ?: emptyList())", rt, true, false)
                else -> KExpr("${recv.code}?.$call", rt, false, true)
            }

            ret.isCollection -> KExpr("${recv.code}.$call", rt, true, false)
            else -> KExpr("${recv.code}.$call", rt, false, op.resultOptional)
        })
    }

    /** the property named [name] (as in the XMI) visible on [type]; the most specific one when redefined */
    private fun findProperty(type: MofType, name: String): MofProperty? {
        val candidates = type.allOwnedAttributes.filter { it.nameInXmi == name }
        return candidates.firstOrNull { c -> candidates.all { o -> c.parentType!!.isSubtypeOf(o.parentType!!) } }
            ?: candidates.firstOrNull()
    }

    private fun bool(code: String) = KExpr(code, null, false, false, isBoolean = true)
    private fun int(code: String) = KExpr(code, null, false, false, isInt = true)
    private fun str(code: String) = KExpr(code, model.allClasses.firstOrNull { it.name == "String" }, false, false, isString = true)

    /** sets the Boolean / integer / String flags from the type */
    private fun typed(e: KExpr): KExpr = when (e.type?.name) {
        "Boolean" -> e.copy(isBoolean = true)
        "Integer", "Long" -> e.copy(isInt = true)
        "UnlimitedNatural" -> e.copy(isInt = true, isUnlimited = true)
        "String" -> e.copy(isString = true)
        else -> e
    }

    /** a comparison of integers; an UnlimitedNatural '*' (-1) is greater than every other value */
    private fun compare(l: KExpr, op: String, r: KExpr): String = when {
        !l.isUnlimited && !r.isUnlimited -> "(${l.code} $op ${r.code})"
        ">" == op -> compare(r, "<", l)
        ">=" == op -> compare(r, "<=", l)
        "<" == op -> "((${l.code} != -1L) && ((${r.code} == -1L) || (${l.code} < ${r.code})))"
        else -> "((${r.code} == -1L) || ((${l.code} != -1L) && (${l.code} <= ${r.code})))"
    }

    /** a type in a let or iterator declaration: whether it is a collection, and its (element) type if known */
    private data class DeclaredType(val isCollection: Boolean, val type: MofType?)

    private fun parseTypeExpr(): DeclaredType {
        val name = nextId()
        return if (isSym("(")) {
            pos++
            val inner = parseTypeExpr()
            expectSym(")")
            DeclaredType(name in setOf("Sequence", "OrderedSet", "Set", "Bag", "Collection") || inner.isCollection, inner.type)
        } else {
            DeclaredType(false, model.allClasses.firstOrNull { it.name == name })
        }
    }

    /** an empty collection literal given a declared element type, so that Kotlin can infer it */
    private fun declaredEmpty(e: KExpr, declared: DeclaredType?): String = when {
        "emptyList()" == e.code && null != declared?.type -> "emptyList<${declared.type.validName}>()"
        else -> e.code
    }

    /** 'iterate(v [: T]; acc [: T] = init | body)', as a fold; the closing ')' is left for the caller */
    private fun iterate(recv: KExpr, scope: Scope): KExpr {
        val v = nextId()
        if (isSym(":")) {
            pos++; parseTypeExpr()
        }
        expectSym(";")
        val acc = nextId()
        val declared = if (isSym(":")) {
            pos++; parseTypeExpr()
        } else null
        expectSym("=")
        val init = parseExpr(scope)
        expectSym("|")
        val kv = "_$v"
        val ka = "_$acc"
        val accExpr0 = when {
            null != declared?.type -> typed(KExpr(ka, declared.type, declared.isCollection, false))
            else -> init.copy(code = ka)
        }
        val accExpr = if (init.isCollection || accExpr0.isCollection) accExpr0.copy(isCollection = true, nullable = false) else accExpr0.copy(nullable = init.nullable)
        val body = parseExpr(Scope(scope, mapOf(v to recv.copy(code = kv, isCollection = false, nullable = false), acc to accExpr), null))
        if (body.isCollection != accExpr.isCollection) throw Untranslatable("iterate body and accumulator differ")
        val initCode = when {
            accExpr.isCollection -> {
                val c = declaredEmpty(init.asCollection(), declared)
                "$c.toList()"
            }

            else -> init.code
        }
        val bodyCode = if (accExpr.isCollection) "(${body.code}).toList()" else body.code
        return accExpr.copy(code = "${recv.code}.fold($initCode) { $ka, $kv -> $bodyCode }")
    }

    /** a Boolean expression that may be null is treated as false */
    private fun boolCode(e: KExpr): String = when {
        e.isCollection -> throw Untranslatable("collection used as Boolean")
        e.isBoolean && e.nullable -> "(${e.code} == true)"
        e.isBoolean -> e.code
        else -> throw Untranslatable("non Boolean condition")
    }
}
