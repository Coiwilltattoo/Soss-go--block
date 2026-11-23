package com.meuapp.sossegoblock

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService

class BloqueadorService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val prefs = getSharedPreferences("SossegoPrefs", Context.MODE_PRIVATE)
        val bloqueioAtivo = prefs.getBoolean("bloqueio_ativo", true)

        if (!bloqueioAtivo) {
            respondToCall(callDetails, CallResponse.Builder().build())
            return
        }

        if (callDetails.callDirection == Call.Details.DIRECTION_INCOMING) {
            val numero = callDetails.handle?.schemeSpecificPart

            if (numero.isNullOrEmpty() || !numeroEstaNaAgenda(numero)) {
                val resposta = CallResponse.Builder()
                    .setDisallowCall(true)
                    .setRejectCall(true)
                    .setSkipNotification(true)
                    .setSkipCallLog(false)
                    .build()

                respondToCall(callDetails, resposta)
                return
            }
        }

        respondToCall(callDetails, CallResponse.Builder().build())
    }

    private fun numeroEstaNaAgenda(numero: String): Boolean {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(numero)
        )
        val projection = arrayOf(ContactsContract.PhoneLookup._ID)

        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return true
            }
        }
        return false
    }
}
