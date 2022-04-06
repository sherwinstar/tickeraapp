package com.tickera.tickeraapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tickera.tickeraapp.stripe_terminal.StripeTerminalIntegrationActivity
import com.tickera.tickeraapp.wedgescan.WedgeContinuousScanActivity
import com.tickera.tickeraapp.wedgescan.WedgeOneScanActivity
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        button.setOnClickListener{
            startActivity(Intent(this,MainActivity2Activity::class.java))
        }

        button2.setOnClickListener{
            startActivity(Intent(this, StripeTerminalIntegrationActivity::class.java))
        }

        button3.setOnClickListener{
            startActivity(Intent(this,WedgeOneScanActivity::class.java))
        }

        button4.setOnClickListener{
            startActivity(Intent(this, WedgeContinuousScanActivity::class.java))
        }
    }
}