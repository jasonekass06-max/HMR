package et.supint.digitalservice.data

import androidx.annotation.StringRes
import et.supint.digitalservice.R
import kotlinx.serialization.Serializable

/** Every machine type the platform lists. */
enum class MachineType(@StringRes val label: Int) {
    LOADER(R.string.machine_loader),
    TRACTOR(R.string.machine_tractor),
    COMBINE_HARVESTER(R.string.machine_combine),
    EXCAVATOR(R.string.machine_excavator),
    LONG_FORKLIFT(R.string.machine_forklift),
    ISUZU_FSR(R.string.machine_isuzu_fsr),
    SINO_TRUCK(R.string.machine_sino_truck),
    CASONI(R.string.machine_casoni),
}

enum class PricingUnit { PER_HOUR, PER_DAY, PER_HECTARE, PER_TRIP, PER_QUINTAL }

enum class VerificationStatus { PENDING, VERIFIED, REJECTED, SUSPENDED }

/** A provider's registered machine and the service they offer with it. */
@Serializable
data class ProviderVehicle(
    val id: String,
    val machineType: String,          // MachineType.name
    val plateNumber: String,
    val modelYear: Int?,
    val serviceDescription: String,   // e.g. "Land levelling", "Grain harvesting"
    val unit: String,                 // PricingUnit.name
    val unitPriceEtb: String,         // decimal string, never Double for money
    val operatorIncluded: Boolean,
    val fuelIncluded: Boolean,
    val serviceRegion: String,        // e.g. "Oromia / Adama"
    val libreDocUrl: String? = null,  // uploaded vehicle ownership document (libre)
    val verification: String = VerificationStatus.PENDING.name,
)

enum class BookingStatus { REQUESTED, ACCEPTED, IN_PROGRESS, COMPLETED, CANCELLED, DISPUTED }

@Serializable
data class Booking(
    val id: String,
    val customerId: String,
    val providerId: String,
    val vehicleId: String,
    val quantity: String,       // hours / days / hectares
    val serviceAmountEtb: String,
    val status: String,
    val latitude: Double?,
    val longitude: Double?,
    val createdAtEpochMs: Long,
)

enum class PayMethod(val displayName: String) {
    TELEBIRR("telebirr"),
    CBE_BIRR("CBE Birr"),
    AMOLE("Amole"),
    HELLOCASH("HelloCash"),
    MPESA("M-PESA"),
    CHAPA("Chapa (cards/banks)"),
    CASH_ON_COMPLETION("Cash"),
}
