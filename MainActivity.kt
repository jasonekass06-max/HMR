package et.supint.digitalservice

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import et.supint.digitalservice.ui.SupintNavHost
import et.supint.digitalservice.ui.theme.SupintTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SupintTheme {
                SupintNavHost(activity = this)
            }
        }
    }
}
