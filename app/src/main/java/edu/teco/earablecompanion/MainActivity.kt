package edu.teco.earablecompanion

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import edu.teco.earablecompanion.bluetooth.EarableService
import edu.teco.earablecompanion.databinding.MainActivityBinding
import edu.teco.earablecompanion.overview.OverviewFragment
import edu.teco.earablecompanion.overview.connection.ConnectionFragment

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val navController: NavController by lazy { findNavController(R.id.main_content) }
    private lateinit var binding: MainActivityBinding

    private val currentFragment: Fragment?
        get() = supportFragmentManager.primaryNavigationFragment?.childFragmentManager?.fragments?.first()
    private val bottomSheetDialogFragment: BottomSheetDialogFragment?
        get() = currentFragment?.childFragmentManager?.fragments?.first() as? BottomSheetDialogFragment

    private val enableBluetoothRegistration = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> earableService?.startScan()
            else -> {
                bottomSheetDialogFragment?.requireDialog()?.cancel()

                Snackbar.make(binding.root, R.string.bluetooth_disclaimer, Snackbar.LENGTH_LONG)
                    .setAnchorView(binding.bottomNavView)
                    .show()
            }
        }
    }
    private val requestPermissionsRegistration = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
        when {
            // all permissions granted, start/bind service
            map.all { it.value } -> earableService?.let { enableBluetoothIfDisabled() } ?: startAndBindService()
            else -> {
                bottomSheetDialogFragment?.requireDialog()?.cancel()

                Snackbar.make(binding.root, R.string.permissions_disclaimer, Snackbar.LENGTH_LONG)
                    .setAnchorView(binding.bottomNavView)
                    .show()
            }
        }
    }


    var bottomNavigationVisible: Boolean = true
        set(value) {
            lifecycleScope.launchWhenCreated {
                binding.bottomNavView.isVisible = value
                field = value
            }
        }
    var earableService: EarableService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<MainActivityBinding>(this, R.layout.main_activity).apply {
            bottomNavView.setupWithNavController(navController)
            bottomNavView.setOnNavigationItemReselectedListener { } // no-op
            setupActionBarWithNavController(navController)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isChangingConfigurations) {
            stopService(Intent(this, EarableService::class.java))
        }
    }

    override fun onSupportNavigateUp(): Boolean = navController.navigateUp() || super.onSupportNavigateUp()

    fun requestPermissions() {
        requestPermissionsRegistration.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.BLUETOOTH_ADMIN,
            )
        )
    }

    private fun startAndBindService() {
        Intent(this, EarableService::class.java).also {
            try {
                startService(it)
                bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
            } catch (t: Throwable) {
                Log.e(TAG, Log.getStackTraceString(t))
            }
        }
    }

    private fun enableBluetoothIfDisabled() {
        when (earableService?.isBluetoothEnabled) {
            true -> earableService?.startScan()
            else -> enableBluetoothRegistration.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            earableService = (service as EarableService.LocalBinder).service
            enableBluetoothIfDisabled()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            earableService = null
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}