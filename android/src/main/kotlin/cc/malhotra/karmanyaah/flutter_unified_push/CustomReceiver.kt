package cc.malhotra.karmanyaah.flutter_unified_push
//
import android.content.Context
import android.content.Intent
import org.unifiedpush.android.connector.MessagingReceiver
import org.unifiedpush.android.connector.MessagingReceiverHandler
import io.flutter.view.FlutterMain
import android.util.Log
import java.util.HashMap
import android.net.Uri
//


val handler = object : MessagingReceiverHandler {

    override fun onMessage(context: Context?, message: String) {
//        event = "c"
//         val dict = URLDecoder.decode(message,"UTF-8").split("&")
//         val params= dict.associate { try{it.split("=")[0] to it.split("=")[1]}catch (e: Exception){"" to ""} }
//         val text = params["message"]?: "New notification"
//         val priority = params["priority"]?.toInt()?: 8
//         val title = params["title"]?: "UP - Example"
        // Notifier(context!!).sendNotification(title,text,priority)
FlutterMain.startInitialization(context!!)
        FlutterMain.ensureInitializationComplete(context!!, null)
        val intent = Intent(context!!,CustomReceiver::class.java )

        intent.putExtra("message", message)
Log.d("CustomReceiver", "onMessage")
        FlutterUnifiedPushService.enqueueWork(context!!, intent)
    }

    override fun onNewEndpoint(context: Context?, endpoint: String) {
//        event = "c"
        // print(endpoint)
       Log.d("TAGGG", endpoint)
        if (FlutterUnifiedPushPlugin.channel != null)
        FlutterUnifiedPushPlugin.channel!!.invokeMethod("onNewEndpoint", endpoint)
        else Log.e("TAGGG", "channel is null")

    }

    override fun onUnregistered(context: Context?) {
//        event = "c"
    FlutterUnifiedPushPlugin.channel?.invokeMethod("onUnregister", null)
    }
}
class CustomReceiver: MessagingReceiver(handler)
