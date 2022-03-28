package com.tickera.tickeraapp.stripe_terminal

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.*
import com.stripe.stripeterminal.external.models.*
import com.stripe.stripeterminal.log.LogLevel
import com.tickera.tickeraapp.Constant.stripeBaseUrl
import com.tickera.tickeraapp.R
import com.tickera.tickeraapp.retrofit.ApiSetup
import com.tickera.tickeraapp.retrofit.CommonResponseClass
import kotlinx.android.synthetic.main.activity_stripe_terminal_integration.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.DialogInterface

import android.location.LocationManager

import android.R.attr.name
import android.app.AlertDialog
import android.content.Context
import android.provider.Settings
import java.lang.Exception


class StripeTerminalIntegrationActivity : AppCompatActivity(), DiscoveryListener,
    BluetoothReaderListener {
    private val REQUEST_CODE_LOCATION: Int = 100

    // Choose the level of messages that should be logged to your console
    val logLevel = LogLevel.VERBOSE
    val TAG = "Tag"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stripe_terminal_integration)

    }

    fun connectWithReader(view: View) {
        if (checkLocationEnabledOrNot()) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
                // REQUEST_CODE_LOCATION should be defined on your app level
                ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_LOCATION)
            } else {
                btn_connect.visibility = View.GONE
                getSecretFromConnectionToken()
            }
        }
    }

    fun proceedToPayment(view: View) {
        startActivity(Intent(this, PaymentActivity::class.java))
    }

    fun checkLocationEnabledOrNot(): Boolean {
        val lm: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gps_enabled = false
        var network_enabled = false

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
        }

        if (!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder(this)
                .setMessage(R.string.gps_network_not_enabled)
                .setPositiveButton(R.string.open_location_settings,
                    DialogInterface.OnClickListener { paramDialogInterface, paramInt ->
                        startActivity(
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        )
                    })
                .setNegativeButton(R.string.Cancel, null)
                .show()
            return false
        } else {
            return true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_LOCATION && grantResults.isNotEmpty()
            && grantResults[0] != PackageManager.PERMISSION_GRANTED
        ) {
            throw RuntimeException("Location services are required in order to " + "connect to a reader.")
        }
    }

    private fun getSecretFromConnectionToken() {
        progress_text.text = "Please Wait.."
        progress_circular.visibility = View.VISIBLE
        progress_text.visibility = View.VISIBLE
        ApiSetup().getRetrofitForStripe(stripeBaseUrl)
            .getSecretKey("Bearer sk_test_iEscjQPlmvyjxQaTQEKH5zuU008UQhVhep").enqueue(
                object : Callback<CommonResponseClass> {
                    override fun onResponse(
                        call: Call<CommonResponseClass>,
                        response: Response<CommonResponseClass>
                    ) {
                        progress_circular.visibility = View.GONE
                        progress_text.visibility = View.GONE
                        if (response.isSuccessful && response.body() != null) {
                            Log.d("TAG", "onResponse: secret " + response.body()!!.secret)
                            val secretKey = response.body()!!.secret
                            // Create your token provider.
                            val tokenProvider = TokenProvider(secretKey)
                            // Pass in the current application context, your desired logging level, your token provider, and the listener you created
                            if (!Terminal.isInitialized()) {
                                Terminal.initTerminal(
                                    applicationContext,
                                    logLevel,
                                    tokenProvider,
                                    listener
                                )
                                discoverReadersAction()
                            }
                        }else{
                            tv_error.text = "Error in getting Secret Key"
                        }

                    }

                    override fun onFailure(call: Call<CommonResponseClass>, t: Throwable) {
                       runOnUiThread{
                        progress_circular.visibility = View.GONE
                        progress_text.visibility = View.GONE
                        tv_error.text = "Error getting Secret Key"
                       }
                        Log.d("TAG", "onFailure: called ")
                    }
                })
    }

    //      Create your listener object. Override any methods that you want to be notified about
    private val listener = object : TerminalListener {
        override fun onUnexpectedReaderDisconnect(reader: Reader) {
// You might want to display UI to notify the user and start re-discovering readers
            Log.d(TAG, "onUnexpectedReaderDisconnect: called")
        }

        override fun onConnectionStatusChange(status: ConnectionStatus) {
            super.onConnectionStatusChange(status)
            Log.d(TAG, "onConnectionStatusChange: called with status " + status)
        }

        override fun onPaymentStatusChange(status: PaymentStatus) {
            super.onPaymentStatusChange(status)
            Log.d(TAG, "onPaymentStatusChange: called")
        }
    }

    // Action for a "Discover Readers" button

    fun discoverReadersAction() {
        progress_text.text = "Discovering Readers.."
        progress_circular.visibility = View.VISIBLE
        progress_text.visibility = View.VISIBLE
        val config = DiscoveryConfiguration(
            0,
            discoveryMethod = DiscoveryMethod.BLUETOOTH_SCAN, isSimulated = true
        )

        Terminal.getInstance().discoverReaders(config, this, object :
            com.stripe.stripeterminal.external.callable.Callback {
            override fun onSuccess() {
                runOnUiThread {
                    println("discoverReaders succeeded")
                    Log.d(TAG, "discoverReaders succeed: ")
                    findViewById<TextView>(R.id.text_status).text =
                        "Readers discovered successfully"
                }
            }

            override fun onFailure(e: TerminalException) {
                runOnUiThread {
                    tv_error.text = "Error Discovering Readers"
                    progress_circular.visibility = View.GONE
                    progress_text.visibility = View.GONE
                }
                e.printStackTrace()
                Log.d(TAG, "onFailure: for discover readers")
            }
        })

    }

    // DiscoveryListener
    override fun onUpdateDiscoveredReaders(readers: List<Reader>) {
        // In your app, display the discovered reader(s) to the user.
        // Call `connectBluetoothReader` after the user selects a reader to connect to.
        var text = ""
        for (reader in readers) {
            if (text.isEmpty())
                text = "Found readers are: " +
                        "\n${reader.configVersion} ${reader.deviceType.deviceName}"
            else
                text = "$text, \n${reader.configVersion} ${reader.deviceType.deviceName}"
        }
        runOnUiThread {
            text1.text = text
            progress_circular.visibility = View.VISIBLE
        }

        if (readers.isNotEmpty()) {
//            For now, we are choosing first item. Later we should show a list of available readers and ask user to choose one of them
            // TODO: 03-02-2022 Need to show list of readers and ask user to choose one of them. SO then change code accordingly
            val tempReader = readers.first()
            runOnUiThread {
                progress_circular.visibility = View.VISIBLE
                progress_text.visibility = View.VISIBLE
                progress_text.text = "Connecting to First reader "
            }

//    create and use a BluetoothConnectionConfiguration with the locationId, set to the relevant location ID when connecting.
// TODO: 03-02-2022 Need to add custom Location ID later
            val tempLocationId = tempReader.location?.id.orEmpty()
            Log.d(TAG, "onUpdateDiscoveredReaders: templocal id " + tempLocationId)
            val connectionConfig =
                ConnectionConfiguration.BluetoothConnectionConfiguration(tempLocationId) //Need to use custom location ID

            Terminal.getInstance().connectBluetoothReader(tempReader, connectionConfig, this,
                object : ReaderCallback {
                    override fun onFailure(e: TerminalException) {
                        e.printStackTrace()
                        runOnUiThread{
                            tv_error.text = "Error Connecting Reader"
                        progress_circular.visibility = View.GONE
                        progress_text.visibility = View.GONE
                        }
                        Log.d(TAG, "onFailure: called for connectBluetoothReader")
                    }

                    override fun onSuccess(reader: Reader) {
                        runOnUiThread {
                            progress_circular.visibility = View.GONE
                            progress_text.visibility = View.GONE
                            text2.text = "Connected to reader successfully"
                            Toast.makeText(
                                this@StripeTerminalIntegrationActivity,
                                "Connected to reader successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.d(TAG, "onSuccess: Successfully connected to reader")
                            btn_payment.visibility = View.VISIBLE
                        }
                        /*startActivity(
                            Intent(
                                this@StripeTerminalIntegrationActivity,
                                PaymentActivity::class.java
                            )
                        )*/
                    }
                })
        }
    }

    // BluetoothReaderListener

    override fun onStartInstallingUpdate(update: ReaderSoftwareUpdate, cancelable: Cancelable?) {
        // Show UI communicating that a required update has started installing
        Log.d(TAG, "onStartInstallingUpdate: ")
    }

    override fun onReportReaderSoftwareUpdateProgress(progress: Float) {
        // Update the progress of the install
        Log.d(TAG, "onReportReaderSoftwareUpdateProgress: ")
    }

    override fun onFinishInstallingUpdate(update: ReaderSoftwareUpdate?, e: TerminalException?) {
        // Report success or failure of the update
        val estimatedUpdateTime = update?.timeEstimate
        Log.d(TAG, "onFinishInstallingUpdate: ")
    }

    override fun onReportAvailableUpdate(update: ReaderSoftwareUpdate) {
        // An update is available for the connected reader. Show this update in your application.
        // This update can be installed using `Terminal.shared.installAvailableUpdate`.
        Log.d(TAG, "onReportAvailableUpdate: ")
    }

}