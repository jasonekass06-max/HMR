# Supint Digital Service

Android app (Kotlin, Jetpack Compose) that connects owners of heavy machines (loader, tractor,
combine harvester, excavator, long forklift, ISUZU FSR, Sino Truck, Casoni) with people who need
them. Currency is ETB. Languages: English, አማርኛ, ትግርኛ, Afaan Oromoo, Qafar af.

## What is in this project

| Area | Where | Notes |
|---|---|---|
| Machine types, bookings, provider vehicles | `data/Models.kt` | Money is always a decimal string / `BigDecimal`, never `Double` |
| Fees | `domain/FeeCalculator.kt` + unit tests | See table below |
| Payments | `payment/PaymentGateway.kt` | telebirr, CBE Birr, Amole, HelloCash, M-PESA, Chapa (cards/banks), cash. All go through the Supint backend |
| Short code (no internet) | `ussd/UssdLauncher.kt` | Opens the dialer with `*811#`; the menu runs on the server |
| Security | `security/SecureStore.kt`, `network_security_config.xml` | Encrypted token storage, biometric/PIN before paying, HTTPS only, certificate pinning |
| UI | `ui/` | Orange / teal / yellow theme, bottom navigation, language switcher |
| Strings | `res/values*/strings.xml` | en, am, ti, om, aa |

## Fee model (all rates are in `FeeConfig`)

| Item | Who pays | Rule |
|---|---|---|
| Platform commission | Provider | 10% of service amount |
| Booking (service) fee | Customer | 2%, min 5 ETB, max 150 ETB |
| Processing fee | Customer | 1%, min 2 ETB |
| VAT | Customer | 15% on booking + processing fee |

Example, service amount 5,000 ETB: customer pays 5,172.50, provider receives 4,500.00,
platform keeps 500 commission + 100 booking fee. Confirm the VAT treatment (including whether
VAT applies to the commission: `vatOnCommission`) with an Ethiopian tax accountant.

## Short-code flow (server side)

1. Customer dials `*811#` and chooses language.
2. Menu: 1 Request a machine, 2 My bookings, 3 Pay, 4 Become a provider.
3. Server asks machine type, then woreda/region, then hours or hectares, shows the price with fees,
   creates the booking, and sends a payment push to the same phone number.

## What you still need before launch

- **A backend.** The app expects `https://api.supint.et/v1/` with `/payments`, auth (phone OTP),
  bookings, vehicles, and webhooks from each wallet. The app holds no merchant keys.
- **Merchant agreements and API credentials** from telebirr, CBE Birr, Amole, HelloCash, M-PESA
  and/or Chapa. Each has its own onboarding and test sandbox.
- **A USSD short code** from ethio telecom and/or Safaricom Ethiopia. `*811#` is a placeholder.
- **Licensing and legal review** for holding or moving customer money (National Bank of Ethiopia
  payment-system rules), VAT registration, and terms/privacy text.
- **Afar translation.** `values-aa` is intentionally almost empty and falls back to English until a
  native speaker translates it. The Amharic, Tigrigna and Oromo strings are a first pass and should
  also be reviewed by native speakers.
- Replace the certificate pins in `network_security_config.xml` and design a real launcher icon.
- Wire the screens to ViewModels and the API (the buttons currently have placeholder handlers),
  and add maps/location, provider verification (libre upload + admin approval), ratings, chat,
  and push notifications.

## Build

Open the `supint` folder in Android Studio (Koala or newer, JDK 17) and run the `app` configuration,
or `./gradlew :app:testDebugUnitTest` for the fee tests. This scaffold has not been compiled yet.
