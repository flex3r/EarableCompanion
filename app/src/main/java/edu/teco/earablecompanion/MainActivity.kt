package edu.teco.earablecompanion

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
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
import dagger.hilt.android.AndroidEntryPoint
import edu.teco.earablecompanion.bluetooth.EarableService
import edu.teco.earablecompanion.databinding.MainActivityBinding
import edu.teco.earablecompanion.utils.extensions.showLongSnackbar

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val navController: NavController by lazy { findNavController(R.id.main_content) }
    private val bluetoothAdapter: BluetoothAdapter by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private lateinit var binding: MainActivityBinding

    private inline val Fragment?.childFragment get() = this?.childFragmentManager?.fragments?.first()
    private inline val currentFragment get() = supportFragmentManager.primaryNavigationFragment?.childFragment
    private inline val bottomSheetDialogFragment get() = currentFragment?.childFragment as? BottomSheetDialogFragment

    private val enableBluetoothRegistration = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> earableService?.startScan()
            else -> {
                bottomSheetDialogFragment?.requireDialog()?.cancel()
                binding.root.showLongSnackbar(getString(R.string.bluetooth_disclaimer)) {
                    anchorView = binding.bottomNavView
                }
            }
        }
    }
    private val requestPermissionsRegistration = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
        when {
            // all permissions granted, try to start scan
            map.all { it.value } -> startScanOrEnableBluetooth()
            else -> {
                bottomSheetDialogFragment?.requireDialog()?.cancel()
                binding.root.showLongSnackbar(getString(R.string.permissions_disclaimer)) {
                    anchorView = binding.bottomNavView
                }
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

    private var isBound = false
    var earableService: EarableService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<MainActivityBinding>(this, R.layout.main_activity).apply {
            bottomNavView.setupWithNavController(navController)
            bottomNavView.setOnNavigationItemReselectedListener { } // no-op
            setupActionBarWithNavController(navController)
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isBound) Intent(this, EarableService::class.java).also {
            try {
                isBound = true
                startService(it)
                bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
            } catch (t: Throwable) {
                Log.e(TAG, Log.getStackTraceString(t))
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            isBound = false
            try {
                unbindService(serviceConnection)
            } catch (t: Throwable) {
                Log.e(TAG, Log.getStackTraceString(t))
            }
        }
    }

    override fun onDestroy() {
        if (!isChangingConfigurations) {
            stopService(Intent(this, EarableService::class.java))
            android.os.Process.killProcess(android.os.Process.myPid())
        }

        super.onDestroy()
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

    private fun startScanOrEnableBluetooth() {
        when {
            bluetoothAdapter.isEnabled -> earableService?.startScan()
            else -> enableBluetoothRegistration.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            isBound = true
            earableService = (service as EarableService.LocalBinder).service
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            earableService = null
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}