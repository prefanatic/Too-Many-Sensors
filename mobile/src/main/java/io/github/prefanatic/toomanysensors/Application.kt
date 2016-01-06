package io.github.prefanatic.toomanysensors

import com.google.android.gms.wearable.Wearable
import edu.uri.egr.hermes.Hermes
import io.realm.Realm
import io.realm.RealmConfiguration

public class Application : android.app.Application() {

    override fun onCreate() {
        super.onCreate()

        val realmConfig = RealmConfiguration.Builder(this)
                .build()

        val config = Hermes.Config()
                .addApi(Wearable.API)
                .enableDebug(BuildConfig.DEBUG)

        Hermes.init(this, config)
    }
}
