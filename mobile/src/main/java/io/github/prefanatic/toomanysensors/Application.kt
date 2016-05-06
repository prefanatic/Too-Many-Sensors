/*
 * Copyright 2015-2016 Cody Goldberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
                .deleteRealmIfMigrationNeeded()
                .build()

        Realm.setDefaultConfiguration(realmConfig);
        Realm.getInstance(realmConfig).close()

        val config = Hermes.Config()
                .addApi(Wearable.API)
                .enableDebug(BuildConfig.DEBUG)

        Hermes.init(this, config)
    }
}
