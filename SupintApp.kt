package et.supint.digitalservice

import android.app.Application
import et.supint.digitalservice.security.SecureStore

class SupintApp : Application() {
    lateinit var secureStore: SecureStore
        private set

    override fun onCreate() {
        super.onCreate()
        secureStore = SecureStore(this)
    }
}
