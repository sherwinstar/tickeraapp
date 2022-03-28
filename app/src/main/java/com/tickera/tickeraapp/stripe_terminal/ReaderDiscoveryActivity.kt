package com.tickera.tickeraapp.stripe_terminal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.*
import com.stripe.stripeterminal.external.models.*
import com.tickera.tickeraapp.R

class ReaderDiscoveryActivity : AppCompatActivity(), DiscoveryListener, TerminalListener,
    BluetoothReaderListener {

    var discoverCancelable: Cancelable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reader_discovery)
        Terminal.getInstance().setTerminalListener(this)
    }

    // Action for a "Discover Readers" button

    fun discoverReadersAction() {
        val config = DiscoveryConfiguration(
            discoveryMethod = DiscoveryMethod.BLUETOOTH_SCAN, isSimulated = true
        )

        Terminal.getInstance().discoverReaders(config, this, object : Callback {
            override fun onSuccess() {
                println("discoverReaders succeeded")
            }

            override fun onFailure(e: TerminalException) {
                e.printStackTrace()
            }
        })

    }

    // DiscoveryListener
    override fun onUpdateDiscoveredReaders(readers: List<Reader>) {
        // In your app, display the discovered reader(s) to the user.
        // Call `connectBluetoothReader` after the user selects a reader to connect to.
        if (readers.isNotEmpty()){
//            For now, we are choosing first item. Later we should show a list of available readers and ask user to choose one of them
            // TODO: 03-02-2022 Need to show list of readers and ask user to choose one of them. SO then change code accordingly
            val tempReader = readers.first()

//    create and use a BluetoothConnectionConfiguration with the locationId, set to the relevant location ID when connecting.
// TODO: 03-02-2022 Need to add custom Location ID later
            val connectionConfig = ConnectionConfiguration.BluetoothConnectionConfiguration(tempReader.location?.id.orEmpty()) //Need to use custom location ID

            Terminal.getInstance().connectBluetoothReader(tempReader,connectionConfig,this,
                object : ReaderCallback {
                    override fun onFailure(e: TerminalException) {
                        e.printStackTrace()
                    }

                    override fun onSuccess(reader: Reader) {
                        println("Successfully connected to reader")
                    }
                })
        }
    }

// TerminalListener
    override fun onUnexpectedReaderDisconnect(reader: Reader) {
// You might want to display UI to notify the user and start re-discovering readers

    }

    // BluetoothReaderListener

    override fun onStartInstallingUpdate(update: ReaderSoftwareUpdate, cancelable: Cancelable?) {
        // Show UI communicating that a required update has started installing
    }

    override fun onReportReaderSoftwareUpdateProgress(progress: Float) {
        // Update the progress of the install
    }

    override fun onFinishInstallingUpdate(update: ReaderSoftwareUpdate?, e: TerminalException?) {
        // Report success or failure of the update
        val estimatedUpdateTime = update?.timeEstimate
    }

    override fun onReportAvailableUpdate(update: ReaderSoftwareUpdate) {
        // An update is available for the connected reader. Show this update in your application.
        // This update can be installed using `Terminal.shared.installAvailableUpdate`.
    }

}