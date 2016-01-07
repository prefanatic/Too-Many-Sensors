package io.github.prefanatic.toomanysensors

import com.google.android.gms.wearable.Wearable
import edu.uri.egr.hermes.Hermes
import io.realm.Realm
import io.realm.RealmConfiguration
import timber.log.Timber

public class Application : android.app.Application() {

    override fun onCreate() {
        super.onCreate()

        val realmConfig = RealmConfiguration.Builder(this)
                .deleteRealmIfMigrationNeeded() // TODO: REMOVE THIS WHEN IN PRODUCTION!!!!!
                .build()

        Realm.getInstance(realmConfig).close()

        val config = Hermes.Config()
                .addApi(Wearable.API)
                .enableDebug(BuildConfig.DEBUG)

        Hermes.init(this, config)

        (0..5).forEach {
            Timber.e("PLEASE DONT FORGET ABOUT REALM MIGRATIONS!")
        }

    }
}
