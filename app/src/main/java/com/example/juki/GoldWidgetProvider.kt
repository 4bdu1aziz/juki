package com.example.juki

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.juki.data.remote.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GoldWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val today = sdf.format(Date())
                val weekAgo = sdf.format(Date(System.currentTimeMillis() - 7 * 86400000L))

                val response = RetrofitClient.api.getGoldRate(weekAgo, today)

                val goldRecords = response.body()?.records?.filter { it.code == "1" }
                val goldPrice = goldRecords?.lastOrNull()?.buy ?: "—"

                val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
                prefs.edit().putFloat("gold_rate", goldPrice.replace(",", ".").toFloatOrNull() ?: 0f).apply()

                for (id in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_gold_ball)
                    views.setTextViewText(R.id.tv_gold_price, "$goldPrice ₽")
                    appWidgetManager.updateAppWidget(id, views)
                }
            } catch (e: Exception) {
            e.printStackTrace()
            val errorMsg = "${e.javaClass.simpleName}: ${e.localizedMessage?.take(20)}"
            for (id in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_gold_ball)
                views.setTextViewText(R.id.tv_gold_price, errorMsg)
                appWidgetManager.updateAppWidget(id, views)
            }

            } finally {
                pendingResult.finish()
            }
        }
    }
}