package project.alkautsar.simulasikredit

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import project.alkautsar.simulasikredit.databinding.ActivityMainBinding
import project.alkautsar.simulasikredit.viewmodel.SplashViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val splashViewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition { splashViewModel.isLoading.value }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cvAnuitas.setOnClickListener {
            val intent = Intent(this, BungaAnuitasActivity::class.java)
            startActivity(intent)
        }

        // Click listener untuk CardView Bunga Tetap
        binding.cvTetap.setOnClickListener {
            val intent = Intent(this, BungaTetapActivity::class.java)
            startActivity(intent)
        }

        // Click listener untuk CardView Bunga Efektif
        binding.cvEfektif.setOnClickListener {
            val intent = Intent(this, BungaEfektifActivity::class.java)
            startActivity(intent)
        }
    }
}