package com.example.mqttsample

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class MainActivity : AppCompatActivity() {

    lateinit var mqttClient:MqttAndroidClient
    val topic="foo/bar"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<Button>(R.id.send_button).setOnClickListener(View.OnClickListener {
            var text=findViewById<EditText>(R.id.send_msg).text.toString()
            if(!TextUtils.isEmpty(text)){
                publish(text)
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()
        connectMqtt()
    }

    override fun onStop() {
        super.onStop()
        disconnectMqtt()
    }

    fun connectMqtt() {
        var clientId = MqttClient.generateClientId()
        mqttClient = MqttAndroidClient(applicationContext, "tcp://broker.hivemq.com:1883", clientId)
        try {
            var token: IMqttToken = mqttClient.connect()
            token.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MQTT", "connection success")
                    subscribe()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d("MQTT", "connection failure")
                }


            }

            mqttClient.setCallback( object:MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                   Log.d("MQTT","connection lost")
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    var receivedMsg=message?.toString()
                    Log.d("MQTT", "message arrived on topic "+topic+ "message:"+receivedMsg)
                    findViewById<TextView>(R.id.received_msg).setText(receivedMsg)



                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    Log.d("MQTT", "deliveryComplete")
                }

            })


        } catch (e: MqttException) {


        }
    }

    fun publish(payload:String){

        //var payload:String="hello from app"

        val message = MqttMessage()
        message.payload = payload.toByteArray()
        message.qos = 1
        message.isRetained = false
        mqttClient.publish(topic, message, null, object:IMqttActionListener{
            override fun onSuccess(asyncActionToken: IMqttToken?) {
               Log.d("MQTT", "publish success")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.d("MQTT", "publish failed")
            }

        })
    }

    fun subscribe(){
        var token=mqttClient.subscribe(topic,1)
        token.actionCallback=object:IMqttActionListener{
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.d("MQTT", "subscribe success")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.d("MQTT", "subscribe failed")
            }

        }
    }

    fun unsubscribe(){

        mqttClient.unsubscribe(topic)
    }

    fun disconnectMqtt(){
        try{
           var token= mqttClient.disconnect()
            token?.actionCallback=object:IMqttActionListener{
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MQTT","disconnect success");
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                   Log.d("MQTT", "disconnect failed");
                }

            }

        }catch (e:MqttException){

        }
    }


}