package by.ssrlab.birdvoice.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.room.Room
import by.ssrlab.birdvoice.R
import by.ssrlab.birdvoice.app.MainApp
import by.ssrlab.birdvoice.databinding.ActivityMainBinding
import by.ssrlab.birdvoice.db.CollectionDao
import by.ssrlab.birdvoice.db.CollectionDatabase
import by.ssrlab.birdvoice.helpers.utils.LoginManager
import by.ssrlab.birdvoice.main.vm.MainVM
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawer: DrawerLayout
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var navController: NavController

    private lateinit var collectionDao: CollectionDao

    private var regValue = 0
    private var recognitionToken = ""
    private var refreshToken = ""
    private var username = ""
    private var accountId = 0

    private val mainApp = MainApp()
    private val mainVM: MainVM by viewModels()
    private lateinit var loginManager: LoginManager

    private var homeCallback = {}
    private lateinit var launcher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        mainApp.setContext(this@MainActivity)
        loadPreferences()
        initDb()

        binding.apply {
            drawer = mainDrawerLayout
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

            initNavController()
            initBottomNav()

            mainVM.activityBinding = this

            setContentView(root)
            setSupportActionBar(mainToolbar)
            mainVM.setToolbarTitleObserver(mainToolbar, this@MainActivity)
        }

        loginManager = LoginManager(mainApp.getContext())

        regValue = intent.getIntExtra("userRegisterToken", 1)
        recognitionToken = intent.getStringExtra("access").toString()
        refreshToken = intent.getStringExtra("refresh").toString()
        username = intent.getStringExtra("username").toString()
        accountId = intent.getIntExtra("accountId", 0)

        mainVM.setTokens(recognitionToken, refreshToken, accountId)

        registerActivityResultAction()
    }

    override fun onResume() {
        super.onResume()

        mainVM.setupDrawer(binding, this@MainActivity)
    }

    @Suppress("DEPRECATION")
    private fun loadPreferences() {
        val sharedPreferences = getSharedPreferences(mainApp.constPreferences, MODE_PRIVATE)
        val locale = sharedPreferences.getString(mainApp.constLocale, "en")
        locale?.let { Locale(it) }?.let { mainApp.setLocale(it) }

        val count = sharedPreferences.getInt(mainApp.constLaunches, 0)
        if (count > 3) {
            getLocationPermission()
            with (sharedPreferences.edit()) {
                putInt(mainApp.constLaunches, 0)
                apply()
            }
        }

        val config = mainApp.getContext().resources.configuration
        config.setLocale(Locale(locale!!))
        Locale.setDefault(Locale(locale))

        mainApp.getContext().resources.updateConfiguration(config, resources.displayMetrics)
        mainApp.setLocaleInt(locale)

        binding.apply {
            drawerSettingsLabel.setText(R.string.settings)
            drawerButtonLanguage.setText(R.string.language)
            drawerButtonFeedback.setText(R.string.feedback)
            drawerButtonInstruction.setText(R.string.instruction)
            drawerButtonLogOut.setText(R.string.sign_out)
            drawerButtonDelete.setText(R.string.delete)
        }
    }

    private fun initDb() {
        val db = Room.databaseBuilder(mainApp.getContext(), CollectionDatabase::class.java, "birds_collection")
            .fallbackToDestructiveMigration()
            .build()
        collectionDao = db.collectionDao()
    }

    fun setToolbarAction(icon: Int, action: () -> Unit){
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(icon)
        }
        homeCallback = action
    }

    private fun setToolbarUploadAction() {
        binding.mainToolbarUploadIc.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "audio/*"
            launcher.launch(intent)
        }
    }

    private fun registerActivityResultAction() {
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    val uri = result.data?.data

                    mainVM.setUri(uri)
                    mainVM.observableFileToken.value = true
                }
            }
        }
    }

    fun switchToolbarUploadVisibility(hide: Boolean = true) {
        val alphaOutAnim = AnimationUtils.loadAnimation(this@MainActivity, R.anim.common_alpha_out)
        val alphaEnterAnim = AnimationUtils.loadAnimation(this@MainActivity, R.anim.common_alpha_enter)
        binding.mainToolbarUploadIc.apply {
            visibility = if (hide) {
                startAnimation(alphaOutAnim)
                View.GONE
            } else {
                startAnimation(alphaEnterAnim)
                setToolbarUploadAction()
                View.VISIBLE
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                homeCallback()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun setPopBackCallback(func: () -> Unit){
        mainVM.setNavUpLambda(func)
        onBackPressedDispatcher.addCallback(this, mainVM.onMapBackPressedCallback)
    }

    fun deletePopBackCallback() { if (mainVM.onMapBackPressedCallback.isEnabled) mainVM.onMapBackPressedCallback.remove() }

    private fun initNavController() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_container) as NavHostFragment
        navController = navHostFragment.navController
        mainVM.setNavController(navController)
    }

    private fun initBottomNav() {
        binding.apply {
            bottomNav = mainBottomNavigationView
            bottomNav.inflateMenu(R.menu.bottom_menu)
            bottomNav.setupWithNavController(navController)
            showBottomNav()
        }
    }

    fun hideBottomNav() {
        if (bottomNav.visibility == View.VISIBLE){
            bottomNav.startAnimation(AnimationUtils.loadAnimation(mainApp.getContext(), R.anim.nav_bottom_view_out))
            bottomNav.visibility = View.GONE
        }
    }

    fun showBottomNav() {
        if (bottomNav.visibility == View.GONE){
            bottomNav.startAnimation(AnimationUtils.loadAnimation(mainApp.getContext(), R.anim.nav_bottom_view_enter))
            bottomNav.visibility = View.VISIBLE
        }
    }

    fun openDrawer() { drawer.openDrawer(GravityCompat.START) }
    fun closeDrawer() { drawer.closeDrawer(GravityCompat.START) }

    fun getRegValue() = regValue

    fun setRegValue(value: Int){
        regValue = value
    }

    /**
     * Add related text
     */
    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                1)
    }

    fun getApp() = mainApp
    fun getCollectionDao() = collectionDao
    fun getLoginManager() = loginManager
    fun getUsername() = username
}