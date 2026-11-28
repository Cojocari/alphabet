package com.cojocari.alphabet.story

import com.cojocari.alphabet.data.Progress
import com.cojocari.alphabet.i18n.Language
import com.cojocari.alphabet.platform.KeyValueStore
import org.junit.Assert.assertEquals
import org.junit.Test

/** The store-screenshot progress (iOS launch argument `-demo YES`) must not crash: it once asked shows for lessons. */
class DemoProgressTest {
    private class MemoryStore : KeyValueStore {
        private val m = HashMap<String, Any>()
        override fun getInt(key: String, default: Int) = (m[key] as? Int) ?: default
        override fun getLong(key: String, default: Long) = (m[key] as? Long) ?: default
        override fun getBoolean(key: String, default: Boolean) = (m[key] as? Boolean) ?: default
        override fun getString(key: String) = m[key] as? String
        override fun putInt(key: String, value: Int) { m[key] = value }
        override fun putLong(key: String, value: Long) { m[key] = value }
        override fun putBoolean(key: String, value: Boolean) { m[key] = value }
        override fun putString(key: String, value: String) { m[key] = value }
        override fun remove(key: String) { m.remove(key) }
        override fun clear() = m.clear()
    }

    @Test fun demoProgressFillsEveryLanguage() {
        for (language in Language.entries) {
            val story = StoryProgress(MemoryStore())
            fillDemoProgress(Progress(MemoryStore()), story, language)
            assertEquals(30, story.done)
        }
    }
}
