package com.ninhhk.grpcexamples

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ninhhk.grpcexamples.databinding.ActivityMainBinding
import io.grpc.examples.helloworld.GreeterGrpcKt
import io.grpc.examples.helloworld.HelloRequest
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.launch
import java.net.URL
import java.util.logging.Logger

const val SERVER_URL = "http://localhost:8080"

class MainActivity : AppCompatActivity() {

    private val logger = Logger.getLogger(this.javaClass.name)

    private val channel: ManagedChannel
        get() {
            val url = URL(SERVER_URL)
            val port = if (url.port == -1) url.defaultPort else url.port

            logger.info("Connecting to ${url.host}:$port")

            val builder = ManagedChannelBuilder.forAddress(url.host, port)
            if (url.protocol == "https") {
                builder.useTransportSecurity()
            } else {
                builder.usePlaintext()
            }

            return builder.executor(Dispatchers.Default.asExecutor())
                .build()
        }

    private val greeter by lazy { GreeterGrpcKt.GreeterCoroutineStub(channel) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fun sendReq() = with(binding) {
            lifecycleScope.launch {
                try {
                    val request =
                        HelloRequest.newBuilder().setName(editTextName.text.toString()).build()
                    val response = greeter.sayHello(request)
                    val message = "${response.message}\n"
                    textViewResponse.text = message
                } catch (e: Exception) {
                    textViewResponse.text = e.message
                    e.printStackTrace()
                }
            }
        }

        with(binding) {
            editTextName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    buttonSend.isEnabled = s.isNotEmpty()
                }

                override fun afterTextChanged(s: Editable?) {}

            })

            buttonSend.setOnClickListener { sendReq() }

            editTextName.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    sendReq()
                    true
                } else {
                    false
                }
            }
        }
    }
}