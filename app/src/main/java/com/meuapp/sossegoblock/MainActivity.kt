package com.meuapp.sossegoblock

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var txtStatus: TextView
    private lateinit var switchBloqueio: SwitchCompat

    private val pedirPermissaoContatos = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedida ->
        if (concedida) {
            pedirPapelBloqueador()
        } else {
            Toast.makeText(this, "Permissão de contatos necessária!", Toast.LENGTH_LONG).show()
        }
        atualizarTela()
    }

    private val pedirPapelLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        atualizarTela()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtStatus = findViewById(R.id.txtStatus)
        switchBloqueio = findViewById(R.id.switchBloqueio)
        val btnPermissoes = findViewById<Button>(R.id.btnPermissoes)

        val prefs = getSharedPreferences("SossegoPrefs", Context.MODE_PRIVATE)
        switchBloqueio.isChecked = prefs.getBoolean("bloqueio_ativo", true)

        switchBloqueio.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("bloqueio_ativo", isChecked).apply()
            atualizarTela()
        }

        btnPermissoes.setOnClickListener {
            verificarPermissoes()
        }

        atualizarTela()
    }

    private fun verificarPermissoes() {
        val temContatos = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (!temContatos) {
            pedirPermissaoContatos.launch(Manifest.permission.READ_CONTACTS)
        } else {
            pedirPapelBloqueador()
        }
    }

    private fun pedirPapelBloqueador() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
            if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                pedirPapelLauncher.launch(intent)
            }
        }
    }

    private fun atualizarTela() {
        val temContatos = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        var ehBloqueadorPadrao = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
            ehBloqueadorPadrao = roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
        }

        val prefs = getSharedPreferences("SossegoPrefs", Context.MODE_PRIVATE)
        val ligado = prefs.getBoolean("bloqueio_ativo", true)

        if (!temContatos || !ehBloqueadorPadrao) {
            txtStatus.text = "⚠️ FALTAM PERMISSÕES\nToque no botão abaixo para configurar"
        } else if (ligado) {
            txtStatus.text = "🛡️ SOSSÊGO ATIVO\nRejeitando 100% fora da agenda"
        } else {
            txtStatus.text = "⏸️ BLOQUEIO PAUSADO\nRecebendo todas as chamadas"
        }
    }
}
