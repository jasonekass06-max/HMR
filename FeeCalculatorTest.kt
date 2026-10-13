package et.supint.digitalservice.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class FeeCalculatorTest {
    private val calc = FeeCalculator()

    @Test
    fun commission_is_ten_percent_and_provider_gets_the_rest() {
        val f = calc.calculate(BigDecimal("10000"))
        assertEquals(BigDecimal("1000.00"), f.platformCommission)
        assertEquals(BigDecimal("9000.00"), f.providerPayout)
    }

    @Test
    fun customer_total_adds_fees_and_vat() {
        val f = calc.calculate(BigDecimal("5000"))
        // booking 2% = 100.00, processing 1% = 50.00, VAT 15% of 150 = 22.50
        assertEquals(BigDecimal("100.00"), f.bookingFee)
        assertEquals(BigDecimal("50.00"), f.processingFee)
        assertEquals(BigDecimal("22.50"), f.vatOnCustomerFees)
        assertEquals(BigDecimal("5172.50"), f.customerTotal)
    }

    @Test
    fun small_jobs_hit_the_minimum_fees() {
        val f = calc.calculate(BigDecimal("100"))
        assertEquals(BigDecimal("5.00"), f.bookingFee)
        assertEquals(BigDecimal("2.00"), f.processingFee)
    }

    @Test
    fun booking_fee_is_capped() {
        val f = calc.calculate(BigDecimal("1000000"))
        assertEquals(BigDecimal("150.00"), f.bookingFee)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejects_zero_amount() {
        calc.calculate(BigDecimal.ZERO)
    }
}
