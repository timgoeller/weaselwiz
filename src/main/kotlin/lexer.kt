sealed class Token() {
    override fun toString(): String = javaClass.simpleName + "Span: " + span

    val span: Span = DUMMY_SPAN.copy()
    
    // Keywords
    class IF: Token()
    class THEN: Token()
    class ELSE: Token()
    class LET: Token()
    class REC: Token()
    class IN: Token()

    // Symbols
    class LEFT_PAREN: Token()
    class RIGHT_PAREN: Token()
    class LEFT_BRACKET: Token()
    class RIGHT_BRACKET: Token()
    class LAMBDA: Token()
    class COMMA: Token()
    class RIGHT_ARROW: Token()
    class EQUALS: Token()
    data class OPERATOR(val operator: String): Token()

    // Idents
    data class IDENT(val ident: String): Token()

    // Literals
    data class BOOLEAN(val boolean: Boolean): Token()
    data class NUMBER(val number: Int): Token()

    // EOF
    object END_OF_FILE: Token()
}

class Peekable<T>(val iterator: Iterator<T>) {
    var lookahead: T? = null
    fun next(): T? = when {
        lookahead != null -> lookahead.also { lookahead = null }
        iterator.hasNext() -> iterator.next()
        else -> null
    }

    fun peek(): T? = next().also { lookahead = it }
}

class Lexer(input: String) {
    private val chars = Peekable(input.iterator())
    private var lookahead: Token? = null

    private val currentPositon = Span.SpanPosition(0, -1)

    fun peek(): Token = next().also { lookahead = it }
    fun next(): Token {
        lookahead?.let { lookahead = null; return it }
        consumeWhitespace()
        val c = nextChar() ?: return Token.END_OF_FILE

        val tokenStartSpan: Span.SpanPosition = currentPositon.copy()

        val token: Token = when(c) {
            '(' -> Token.LEFT_PAREN()
            ')' -> Token.RIGHT_PAREN()
            '[' -> Token.LEFT_BRACKET()
            ']' -> Token.RIGHT_BRACKET()
            ',' -> Token.COMMA()
            '+' -> Token.OPERATOR("+")
            '*' -> Token.OPERATOR("*")
            '\\' -> Token.LAMBDA()
            '/' -> if(nextChar()=='/') comment() else throw Exception("Expected seccond '/")
            '-' -> if(nextChar() == '>') Token.RIGHT_ARROW() else Token.OPERATOR("-")
            '=' -> if(nextChar() == '=') Token.OPERATOR("==") else Token.EQUALS()
            else -> when {
                c.isJavaIdentifierStart() -> ident(c)
                c.isDigit() -> number(c)
                else -> throw Exception("Unexpected $c")
            }
        }

        token.span.start = tokenStartSpan
        token.span.end = currentPositon.copy()
        return token
    }

    private fun number(c: Char): Token {
        var res = c.toString()
        while (chars.peek()?.isDigit() == true) res += nextChar()
        return Token.NUMBER(res.toInt())
    }
    private fun comment():Token {
        var c = chars.peek()
        while (true){
            if (c == '\n') break
            c = nextChar()
        }
        return next()
    }

    private fun nextChar() : Char?{
        val nextChar = chars.next()

        if (nextChar == '\n') {
            currentPositon.column = -1 //mit copy?
            currentPositon.line += 1
        }
        else {
            currentPositon.column += 1
        }

        return nextChar
    }

    private fun ident(c: Char): Token {
        var res = c.toString()
        while (chars.peek()?.isJavaIdentifierPart() == true) res += nextChar()
        return when(res) {
            "if" -> Token.IF()
            "then" -> Token.THEN()
            "else" -> Token.ELSE()
            "let" -> Token.LET()
            "rec" -> Token.REC()
            "in" -> Token.IN()
            "true" -> Token.BOOLEAN(true)
            "false" -> Token.BOOLEAN(false)
            else -> Token.IDENT(res)
        }
    }

    private fun consumeWhitespace() {
        while (chars.peek()?.isWhitespace() == true) nextChar()
    }
}

fun main() {
    val input = """
        if (\x1 -> equals 20 x1) 25 // Kommentar
        then true
        else add 3 (4 * 5)
    """.trimIndent()
    val lexer = Lexer(input)
    while(lexer.next().also(::println) !is Token.END_OF_FILE) {}

    /// Uebung: Kommentare als Whitespace lexen
    // Kommentar Syntax: // Hello\n
    // Tipp: / <- kann keine andere tokens starten
}