package et.supint.digitalservice.domain

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * All fee rules live here so they can be audited, unit-tested and changed in one place.
 * Money is BigDecimal in ETB, rounded HALF_UP to 2 decimals. Never use Double for money.
 *
 * The server is the source of truth: the app uses this to *preview* the price, and the
 * backend recomputes the same numbers before creating the payment.
 */
data class FeeConfig(
    /** Platform commission taken from the provider's service amount. */
    val commissionRate: BigDecimal = BigDecimal("0.10"),
    /** Booking (service) fee paid by the customer on top of the service amount. */
    val bookingFeeRate: BigDecimal = BigDecimal("0.02"),
    val bookingFeeMin: BigDecimal = BigDecimal("5.00"),
    val bookingFeeMax: BigDecimal = BigDecimal("150.00"),
    /** Payment processing fee (covers wallet / bank gateway costs). */
    val processingRate: BigDecimal = BigDecimal("0.01"),
    val processingMin: BigDecimal = BigDecimal("2.00"),
    /** Ethiopian VAT, applied to the fees the platform charges the customer. */
    val vatRate: BigDecimal = BigDecimal("0.15"),
    /** Whether VAT is also charged on the provider commission. Confirm with an accountant. */
    val vatOnCommission: Boolean = false,
)

data class FeeBreakdown(
    val serviceAmount: BigDecimal,
    val bookingFee: BigDecimal,
    val processingFee: BigDecimal,
    val vatOnCustomerFees: BigDecimal,
    /** What the customer pays in total. */
    val customerTotal: BigDecimal,
    val platformCommission: BigDecimal,
    val vatOnCommission: BigDecimal,
    /** What the provider receives after commission. */
    val providerPayout: BigDecimal,
    /** Total platform revenue (commission + booking fee, before gateway cost). */
    val platformRevenue: BigDecimal,
)

class FeeCalculator(private val cfg: FeeConfig = FeeConfig()) {

    fun calculate(serviceAmount: BigDecimal): FeeBreakdown {
        require(serviceAmount.signum() > 0) { "Amount must be positive" }
        val amount = serviceAmount.money()

        val booking = (amount * cfg.bookingFeeRate).money()
            .coerceIn(cfg.bookingFeeMin, cfg.bookingFeeMax)
        val processing = (amount * cfg.processingRate).money().max(cfg.processingMin)
        val vatCustomer = ((booking + processing) * cfg.vatRate).money()
        val total = amount + booking + processing + vatCustomer

        val commission = (amount * cfg.commissionRate).money()
        val vatComm = if (cfg.vatOnCommission) (commission * cfg.vatRate).money() else BigDecimal.ZERO.money()
        val payout = amount - commission - vatComm

        return FeeBreakdown(
            serviceAmount = amount,
            bookingFee = booking,
            processingFee = processing,
            vatOnCustomerFees = vatCustomer,
            customerTotal = total,
            platformCommission = commission,
            vatOnCommission = vatComm,
            providerPayout = payout,
            platformRevenue = commission + booking,
        )
    }

    private fun BigDecimal.money(): BigDecimal = setScale(2, RoundingMode.HALF_UP)
    private fun BigDecimal.coerceIn(lo: BigDecimal, hi: BigDecimal) = max(lo).min(hi)
}
