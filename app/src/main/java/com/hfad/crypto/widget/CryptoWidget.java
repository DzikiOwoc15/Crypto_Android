package com.hfad.crypto.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.hfad.crypto.R;

public class CryptoWidget extends AppWidgetProvider {
    RemoteViews views;
    private static final String CLICK_TAG = "CLICK";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (CLICK_TAG.equals(intent.getAction())){
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, CryptoWidget.class));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.appwidget_list_view);
            Toast.makeText(context, context.getResources().getText(R.string.data_reloaded) , Toast.LENGTH_LONG).show();
        }
    }

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                         int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.app_name);
        // Construct the RemoteViews object
        views = new RemoteViews(context.getPackageName(), R.layout.crypto_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.crypto_widget);
            SharedPreferences currencyPreferences = context.getApplicationContext().getSharedPreferences(String.valueOf(R.string.setting_currency), Context.MODE_PRIVATE);
            String currency = currencyPreferences.getString(String.valueOf(R.string.setting_currency), "USD");
            if(currency.equals("7 dni")){
                currency = "USD";
            }
            remoteViews.setTextViewText(R.id.appwidget_currency, currency);
            Intent clickIntent = new Intent(context, getClass());
            clickIntent.setAction(CLICK_TAG);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.appwidget_reload_data, pendingIntent);
            Intent intent = new Intent(context, WidgetService.class);
            remoteViews.setRemoteAdapter(R.id.appwidget_list_view, intent);
            updateAppWidget(context, appWidgetManager, appWidgetId);
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

}

