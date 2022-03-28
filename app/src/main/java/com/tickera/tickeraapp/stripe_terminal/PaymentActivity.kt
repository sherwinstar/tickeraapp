package com.tickera.tickeraapp.stripe_terminal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.widget.Toast
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.BluetoothReaderListener
import com.stripe.stripeterminal.external.callable.PaymentIntentCallback
import com.stripe.stripeterminal.external.models.*
import com.tickera.tickeraapp.Constant
import com.tickera.tickeraapp.R
import com.tickera.tickeraapp.retrofit.ApiSetup
import com.tickera.tickeraapp.retrofit.CommonResponseClass
import kotlinx.android.synthetic.main.activity_payment.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PaymentActivity : AppCompatActivity(), BluetoothReaderListener {

    private lateinit var retrievedPaymentIntent: PaymentIntent
    private val TAG = "Tag"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        progressBar.visibility = View.VISIBLE
        progress_text.text = "Please Wait..."
        progress_text.visibility = View.VISIBLE

        val params = PaymentIntentParameters.Builder()
            .setAmount(1000)
            .setCurrency("usd")
            .build()

        Terminal.getInstance().createPaymentIntent(params, object : PaymentIntentCallback {
            override fun onSuccess(paymentIntent: PaymentIntent) {
                Log.d(TAG, "onSuccess: createPaymentIntent")
                // Placeholder for collecting a payment method with paymentIntent
                retrievePaymentIntent(paymentIntent.clientSecret)
            }

            override fun onFailure(e: TerminalException) {
                // Placeholder for handling exception
                Log.d(TAG, "onFailure: createPaymentIntent")
                runOnUiThread {
                    tv_error.text = "Create Payment Failure"
                    progress_text.visibility = View.GONE
                    progressBar.visibility = View.GONE
                }
            }

            private fun retrievePaymentIntent(clientSecret: String?) {
                if (clientSecret != null)
                    Terminal.getInstance().retrievePaymentIntent(clientSecret,
                        object : PaymentIntentCallback {
                            override fun onSuccess(paymentIntent: PaymentIntent) {
                                // Placeholder for collecting a payment method with paymentIntent
                                retrievedPaymentIntent = paymentIntent
                                Log.d(TAG, "onSuccess: retrievePaymentIntent")
                                runOnUiThread {
                                    progress_text.text = "Collecting Payment"
                                }
                                collectPayment()
                            }

                            override fun onFailure(e: TerminalException) {
                                // Placeholder for handling exception
                                Log.d(TAG, "onFailure: retrievePaymentIntent")
                                runOnUiThread {
                                    tv_error.text = "Retrieve Payment Failure"
                                    progress_text.visibility = View.GONE
                                    progressBar.visibility = View.GONE
                                }
                            }
                        })
                else {
                    runOnUiThread {
                        progress_text.visibility = View.GONE
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@PaymentActivity, "Error!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        })
    }

    fun collectPayment() {
        if (::retrievedPaymentIntent.isInitialized) {
//            If you want then You can cancel collecting a payment method using the Cancelable object returned by the Android SDK.
            val cancelable = Terminal.getInstance().collectPaymentMethod(retrievedPaymentIntent,
                object : PaymentIntentCallback {
                    override fun onFailure(e: TerminalException) {
                        // Placeholder for handling exception
                        Log.d(TAG, "onFailure: collectPaymentMethod")
                        runOnUiThread {
                            tv_error.text = "Collect Payment Failure"
                            progress_text.visibility = View.GONE
                            progressBar.visibility = View.GONE
                        }
                    }

                    override fun onSuccess(paymentIntent: PaymentIntent) {
                        runOnUiThread {
                            payment_collected.visibility = View.VISIBLE
                            progress_text.text = "Processing Payment"
                        }
                        // Placeholder for processing paymentIntent
//                        Here you can either process the payment automatically or can show a confirmation screen. For now we are doing it automatically
                        processPayment(paymentIntent)
                        Log.d(TAG, "onSuccess: collectPaymentMethod")
                    }
                })
        }
    }

    private fun processPayment(paymentIntent: PaymentIntent) {
        Terminal.getInstance().processPayment(paymentIntent, object : PaymentIntentCallback {
            override fun onFailure(e: TerminalException) {
                // Placeholder for handling the exception
                Log.d(TAG, "onFailure: processPayment")
                runOnUiThread {
                    tv_error.text = "Process Payment Failure"
                    progress_text.visibility = View.GONE
                    progressBar.visibility = View.GONE
                }
            }

            override fun onSuccess(paymentIntent: PaymentIntent) {
//                You must manually capture payments processed by the Terminal SDKs.
//                Set up your backend to capture the payment within two days. Otherwise,
//                the authorization expires and funds get released back to the customer.
                Log.d(TAG, "onSuccess: processPayment")
                runOnUiThread {
                    payment_processed.visibility = View.VISIBLE
                    progress_text.text = "Capturing Payment"
                }

                // Placeholder for notifying your backend to capture paymentIntent.id
                ApiSetup().getRetrofitForStripe(Constant.stripeBaseUrl).capturePayment(
                    "Bearer sk_test_iEscjQPlmvyjxQaTQEKH5zuU008UQhVhep",
                    paymentIntent.id
                )
                    .enqueue(object : Callback<CommonResponseClass> {
                        override fun onResponse(
                            call: Call<CommonResponseClass>,
                            response: Response<CommonResponseClass>
                        ) {
                            Log.d("TAG", "onResponse: called " + response.isSuccessful)
                            runOnUiThread {
                                progress_text.visibility = View.GONE
                                progressBar.visibility = View.GONE
                                Toast.makeText(
                                    this@PaymentActivity,
                                    "Payment Captured Successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<CommonResponseClass>, t: Throwable) {
                            Log.d("TAG", "onFailure: called")
                            runOnUiThread {
                                tv_error.text = "Capture Payment Failure"
                                progress_text.visibility = View.GONE
                                progressBar.visibility = View.GONE
                            }
                        }
                    })
            }
        })
    }

    override fun onRequestReaderInput(options: ReaderInputOptions) {
        super.onRequestReaderInput(options)
        // Placeholder for updating your app's checkout UI
        Toast.makeText(this, options.toString(), Toast.LENGTH_SHORT).show()
    }

    override fun onRequestReaderDisplayMessage(message: ReaderDisplayMessage) {
        super.onRequestReaderDisplayMessage(message)
        Toast.makeText(this, message.toString(), Toast.LENGTH_SHORT).show()
    }

}