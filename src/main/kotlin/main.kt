import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.request.receive
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.lang.Exception

fun main() {
    embeddedServer(Netty, 37105) {
        routing {
            post("compile") {
                val code = call.receive<String>()
                println(code)

                try {
                    val e = Parser(Lexer(code)).parseExpression()
                    val typechecker = Typechecker()
                    val ty = typechecker.infer(typechecker.initialContext, e)

                    val records = typechecker.dataRecorder.getRecords()

                    call.respond(Gson().toJson(typechecker.dataRecorder.getRecords()))
                }
                catch (e : Exception) {
                    call.respond("{\"error\": \"${e.message}\"}")
                }


            }
        }
    }.start(wait = true)
}