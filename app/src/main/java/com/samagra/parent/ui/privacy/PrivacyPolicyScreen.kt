package com.samagra.parent.ui.privacy

import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.preference.PreferenceManager
import com.example.assets.uielements.CustomMessageDialog
import com.samagra.parent.authentication.AuthenticationActivity
import com.samagra.grove.logging.Grove
import com.samagra.parent.R
import com.samagra.parent.UtilityFunctions
import com.samagra.parent.databinding.PrivacyPolicyViewBinding
import java.io.InputStream


@Suppress("DEPRECATION")
class PrivacyPolicyScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: PrivacyPolicyViewBinding = DataBindingUtil.setContentView(this, R.layout.privacy_policy_view)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val res: Resources = resources
                val in_s: InputStream = res.openRawResource(R.raw.privay_policy_pl_app)
                val b = ByteArray(in_s.available())
                in_s.read(b)
                val de = String(b)
                binding.privacyPolicyView.text = Html.fromHtml(de, Html.FROM_HTML_MODE_LEGACY)
            } catch (e: Exception) {
                // e.printStackTrace();
            }

        } else {
            try {
                val res: Resources = resources
                val in_s: InputStream = res.openRawResource(R.raw.privay_policy_pl_app)
                val b = ByteArray(in_s.available())
                in_s.read(b)
                val de = String(b)
                binding.privacyPolicyView.text = Html.fromHtml(de);
            } catch (e: Exception) {
                // e.printStackTrace();
            }

        }
        binding.privacyScroller.setOnClickListener {
            binding.privacySv.fullScroll(View.FOCUS_DOWN)
        }
        binding.iAcceptText.setOnClickListener {
            binding.privacyCheck.isChecked = !binding.privacyCheck.isChecked
        }
        binding.acceptPolicy.setOnClickListener {
            if (binding.privacyCheck.isChecked) {
                PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("isPrivacyPolicyRead", true).apply()
                redirectToAuthFlow()
            } else {
                val customDialog = CustomMessageDialog(
                    this,null,
                    getString(R.string.privacy_instructions),
                    null
                )
                customDialog.setOnFinishListener {
                    //handle finish
                }
                customDialog.show()
            }
        }
        val tvVersion: TextView = findViewById(R.id.tv_version)
        tvVersion.text = UtilityFunctions.getVersionName(this)
    }

    private fun redirectToAuthFlow() {
        val intent = Intent(this, AuthenticationActivity::class.java)
        startActivity(intent)
        Grove.d("Closing screen! redirecting to auth flow.")
        finish()
    }
}