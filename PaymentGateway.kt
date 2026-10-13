package et.supint.digitalservice.payment

import et.supint.digitalservice.data.PayMethod
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.UUID

/**
 * The app never talks to telebirr, CBE Birr, Amole, HelloCash, M-PESA or Chapa directly and
 * never contains merchant keys. It asks the Supint backend to create the payment; the backend
 * holds the merchant credentials, signs the request, receives the provider's webhook and
 * updates the booking. The app only shows the provider's hosted checkout / USSD push prompt
 * and polls the status.
 */
@Serializable
data class PaymentRequest(
    val bookingId: String,
    val method: String,          // PayMethod.name
    val payerPhone: String,      // 2519XXXXXXXX or 2517XXXXXXXX
)

@Serializable
data class PaymentSession(
    val reference: String,
    val checkoutUrl: String? = null,   // hosted page for telebirr / CBE Birr / Chapa
    val pushPromptSent: Boolean = false, // true when the wallet sent a USSD/STK prompt to the phone
)

@Serializable
data class PaymentStatus(val reference: String, val state: String, val message: String? = null)

interface PaymentApi {
    @POST("payments")
    suspend fun create(
        @Header("Idempotency-Key") key: String,
        @Body body: PaymentRequest,
    ): PaymentSession

    @GET("payments/{ref}")
    suspend fun status(@Path("ref") reference: String): PaymentStatus
}

sealed interface PaymentResult {
    data class Success(val reference: String) : PaymentResult
    data class NeedsUserAction(val session: PaymentSession) : PaymentResult
    data class Failed(val reason: String) : PaymentResult
}

class PaymentRepository(private val api: PaymentApi) {

    /** Same idempotency key for retries of one attempt, so a flaky network can't double-charge. */
    suspend fun start(bookingId: String, method: PayMethod, phone: String, attemptKey: String = UUID.randomUUID().toString()): PaymentResult =
        runCatching {
            if (method == PayMethod.CASH_ON_COMPLETION) return PaymentResult.Success("cash-$bookingId")
            val session = api.create(attemptKey, PaymentRequest(bookingId, method.name, normalizePhone(phone)))
            PaymentResult.NeedsUserAction(session)
        }.getOrElse { PaymentResult.Failed(it.message ?: "Network error") }

    suspend fun check(reference: String): PaymentResult =
        runCatching {
            val s = api.status(reference)
            when (s.state) {
                "SUCCESS" -> PaymentResult.Success(reference)
                "FAILED", "CANCELLED", "EXPIRED" -> PaymentResult.Failed(s.message ?: s.state)
                else -> PaymentResult.NeedsUserAction(PaymentSession(reference))
            }
        }.getOrElse { PaymentResult.Failed(it.message ?: "Network error") }

    companion object {
        /** Accepts 09XXXXXXXX, 07XXXXXXXX, +2519..., 2519... and returns 2519XXXXXXXX form. */
        fun normalizePhone(raw: String): String {
            val d = raw.filter { it.isDigit() }
            return when {
                d.startsWith("251") && d.length == 12 -> d
                d.startsWith("0") && d.length == 10 -> "251" + d.substring(1)
                d.length == 9 -> "251$d"
                else -> throw IllegalArgumentException("Invalid Ethiopian phone number")
            }
        }
    }
}
