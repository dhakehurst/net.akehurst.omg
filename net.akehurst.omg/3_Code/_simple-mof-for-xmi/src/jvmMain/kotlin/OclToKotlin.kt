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

    /** A translated expression: its Kotlin code and its static type. For a collection, [type] is the element type. */
    private data class KExpr(val code: String, val type: MofType?, val isCollection: Boolean, val nullable: Boolean, val isBoolean: Boolean = false, val isInt: Boolean = false) {
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

    /** @return Kotlin code that computes [prop] from 'self', or null when the OCL cannot be translated */
    fun translate(prop: MofProperty, ocl: String): String? = try {
        selfType = prop.parentType ?: throw Untranslatable("no owner")
        tokens = tokenize(ocl)
        pos = 0
        val self = KExpr("self", selfType, false, false)
        val e = parseExpr(Scope(null, emptyMap(), null).let { Scope(it, mapOf("self" to self), null) })
        if (pos != tokens.size) throw Untranslatable("unexpected '${tokens[pos].text}'")
        convertTo(prop, e)
    } catch (e: Untranslatable) {
        null
    }

    // --- conversion to the property type ---

    private fun convertTo(prop: MofProperty, e: KExpr): String {
        val target = prop.type
        return when {
            prop.isCollection -> {
                val c = e.asCollection()
                val elems = when {
                    null != c.type && c.type.isSubtypeOf(target) -> c.code
                    else -> "${c.code}.map { it as ${target.validName} }"
                }
                when (prop.colTypeName) {
                    "Set" -> "$elems.toSet()"
                    "OrderedSet" -> "$elems.toOrderedSet()"
                    else -> "$elems.toList()"
                }
            }

            e.isCollection -> throw Untranslatable("collection given for single valued property")
            else -> {
                val typed = when {
                    e.isBoolean || e.isInt -> e.code
                    null != e.type && e.type.isSubtypeOf(target) -> e.code
                    else -> "(${e.code} as ${target.validName}?)"
                }
                when {
                    prop.isOptional -> typed
                    e.nullable || typed != e.code -> "($typed ?: error(\"Derivation of '${prop.qualifiedName}' gave null\"))"
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

                c.isLetter() || c == '_' -> {
                    if (text.startsWith("_'", i)) throw Untranslatable("quoted name")
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

                c == '\'' -> {
                    val end = text.indexOf('\'', i + 1)
                    if (end < 0) throw Untranslatable("unterminated string")
                    result.add(Token("str", text.substring(i + 1, end)))
                    i = end + 1
                }

                else -> {
                    val two = if (i + 1 < text.length) text.substring(i, i + 2) else ""
                    when (two) {
                        "->", "<>", "<=", ">=", "::" -> {
                            result.add(Token("sym", two)); i += 2
                        }

                        else -> {
                            if (c !in ".,(){}|=<>:+-*/") throw Untranslatable("unexpected character '$c'")
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
        if (t.kind != "id") throw Untranslatable("expected a name, found '${t.text}'")
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
            if (isSym(":")) {
                pos++; skipTypeExpr()
            }
            expectSym("=")
            val e = parseExpr(s)
            val kName = "_$name"
            decls.add("val $kName = ${e.code}")
            vars[name] = e.copy(code = kName)
            s = Scope(s, mapOf(name to vars[name]!!), null)
        } while (isSym(","))
        expectId("in")
        val body = parseExpr(s)
        return body.copy(code = "run { ${decls.joinToString("; ")}; ${body.code} }")
    }

    /** skips a type such as 'Feature', 'Sequence(Feature)' or 'OrderedSet(Type)' */
    private fun skipTypeExpr() {
        nextId()
        if (isSym("(")) {
            pos++; skipTypeExpr(); expectSym(")")
        }
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
            left = bool("(${left.code} $op ${right.code})")
        }
        return left
    }

    private fun parseAdditive(scope: Scope): KExpr {
        var left = parseMultiplicative(scope)
        while (isSym("+") || isSym("-")) {
            val op = peek()!!.text
            pos++
            val right = parseMultiplicative(scope)
            if (!(left.isInt && right.isInt)) throw Untranslatable("arithmetic on non integers")
            left = int("(${left.code} $op ${right.code})")
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
                pos++; int(t.text)
            }

            t.kind == "str" -> {
                pos++
                val s = t.text.replace("\\", "\\\\").replace("\"", "\\\"").replace("$", "\\$")
                KExpr("\"$s\"", model.allClasses.firstOrNull { it.name == "String" }, false, false)
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

            t.kind == "id" -> bareName(scope)
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
            a.type.isSubtypeOf(b.type) -> b.type
            b.type.isSubtypeOf(a.type) -> a.type
            else -> throw Untranslatable("if branches have unrelated types")
        }
        return KExpr(
            "(if (${boolCode(c)}) ${a.code} else ${b.code})", type, a.isCollection, a.nullable || b.nullable,
            a.isBoolean && b.isBoolean, a.isInt && b.isInt
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
            elems.add(e)
        }
        expectSym("}")
        return when {
            elems.isEmpty() -> KExpr("emptyList()", null, true, false)
            else -> KExpr("listOfNotNull(${elems.joinToString { it.code }})", elems.first().type, true, false)
        }
    }

    /** an unqualified name: a variable, or a property of the implicit iterator or of self */
    private fun bareName(scope: Scope): KExpr {
        val name = nextId()
        if (isSym("(")) throw Untranslatable("operation call '$name'")
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
        if (isSym("(")) {
            return when (name) {
                "oclIsKindOf" -> {
                    val t = typeArg()
                    if (recv.isCollection) throw Untranslatable("oclIsKindOf on collection")
                    bool("(${recv.code} is ${t.validName})")
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
        val type = recv.type ?: throw Untranslatable("navigation from unknown type")
        val p = findProperty(type, name) ?: throw Untranslatable("unknown property '$name' of '${type.name}'")
        return navigateProperty(recv, p)
    }

    private fun navigateProperty(recv: KExpr, p: MofProperty): KExpr {
        if (p.type is MofEnum) throw Untranslatable("enumeration typed property")
        val isBool = p.type.name == "Boolean"
        val isInt = false // integers in the api are Long, so they are not mixed with Int literals
        return when {
            recv.isCollection -> when {
                p.isCollection -> KExpr("${recv.code}.flatMap { it.${p.validName} }", p.type, true, false)
                else -> KExpr("${recv.code}.mapNotNull { it.${p.validName} }", p.type, true, false)
            }

            recv.nullable -> when {
                p.isCollection -> KExpr("(${recv.code}?.${p.validName} ?: emptyList())", p.type, true, false)
                else -> KExpr("${recv.code}?.${p.validName}", p.type, false, true, isBool, isInt)
            }

            else -> when {
                p.isCollection -> KExpr("${recv.code}.${p.validName}", p.type, true, false)
                else -> KExpr("${recv.code}.${p.validName}", p.type, false, p.isOptional, isBool, isInt)
            }
        }
    }

    private fun collectionOp(recv: KExpr, scope: Scope): KExpr {
        val op = nextId()
        expectSym("(")
        val result = when (op) {
            "selectByKind", "selectAsKind" -> {
                val t = typeArgNoClose()
                KExpr("${recv.code}.filterIsInstance<${t.validName}>()", t, true, false)
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

            "isEmpty" -> bool("${recv.code}.none()")
            "notEmpty" -> bool("${recv.code}.any()")
            "size" -> int("${recv.code}.count()")
            "first" -> KExpr("${recv.code}.firstOrNull()", recv.type, false, true)
            "last" -> KExpr("${recv.code}.lastOrNull()", recv.type, false, true)
            "at" -> {
                val i = parseExpr(scope)
                if (!i.isInt) throw Untranslatable("non integer index")
                KExpr("${recv.code}.elementAtOrNull((${i.code}) - 1)", recv.type, false, true)
            }

            "indexOf" -> {
                val x = parseExpr(scope)
                int("(${recv.code}.indexOf(${x.code}) + 1)")
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
                recv.copy(code = "${recv.code}.toList().subList((${from.code}) - 1, ${to.code})")
            }

            else -> throw Untranslatable("collection operation '$op'")
        }
        expectSym(")")
        return result
    }

    /** parses 'v | body', 'v : T | body' or 'body' (implicit iterator); the closing ')' is left for the caller */
    private fun iteratorBody(recv: KExpr, scope: Scope): Pair<String, KExpr> {
        val elem = KExpr("", recv.type, false, false)
        val explicit = peek()?.kind == "id" && (isSym("|", 1) || isSym(":", 1))
        return if (explicit) {
            val name = nextId()
            if (isSym(":")) {
                pos++; skipTypeExpr()
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
        return model.allClasses.firstOrNull { it.name == name } ?: throw Untranslatable("unknown type '$name'")
    }

    private fun commonType(a: MofType?, b: MofType?): MofType? = when {
        null == a -> b
        null == b -> a
        a.isSubtypeOf(b) -> b
        b.isSubtypeOf(a) -> a
        else -> throw Untranslatable("unrelated element types")
    }

    /** the property named [name] (as in the XMI) visible on [type]; the most specific one when redefined */
    private fun findProperty(type: MofType, name: String): MofProperty? {
        val candidates = type.allOwnedAttributes.filter { it.nameInXmi == name }
        return candidates.firstOrNull { c -> candidates.all { o -> c.parentType!!.isSubtypeOf(o.parentType!!) } }
            ?: candidates.firstOrNull()
    }

    private fun bool(code: String) = KExpr(code, null, false, false, isBoolean = true)
    private fun int(code: String) = KExpr(code, null, false, false, isInt = true)

    /** a Boolean expression that may be null is treated as false */
    private fun boolCode(e: KExpr): String = when {
        e.isCollection -> throw Untranslatable("collection used as Boolean")
        e.isBoolean && e.nullable -> "(${e.code} == true)"
        e.isBoolean -> e.code
        else -> throw Untranslatable("non Boolean condition")
    }
}
