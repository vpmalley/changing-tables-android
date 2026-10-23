package fr.vpm.changingtables

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import fr.vpm.changingtables.database.AppDatabase
import fr.vpm.changingtables.databinding.ActivityMainBinding
import fr.vpm.changingtables.repositories.BusinessRepository
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: BusinessRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        setSupportActionBar(binding.toolbar)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_map, R.id.navigation_saved, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.navigation_map) {
                binding.appBar.visibility = android.view.View.GONE
            } else {
                binding.appBar.visibility = android.view.View.VISIBLE
            }
        }

        val dao = AppDatabase.getDatabase(this).businessDao()
        repository = BusinessRepository(dao)
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            repository.importFromJson(applicationContext)
        }
    }

    override fun onStop() {
        super.onStop()
        lifecycleScope.launch {
            repository.exportToJson(applicationContext)
        }
    }
}