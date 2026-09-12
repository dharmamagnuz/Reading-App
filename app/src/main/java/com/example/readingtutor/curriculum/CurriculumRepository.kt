package com.example.readingtutor.curriculum

import android.content.Context
import com.example.readingtutor.models.ActivityItem
import com.example.readingtutor.models.CourseInfo
import com.example.readingtutor.models.DayPlan
import com.example.readingtutor.models.DaySummary
import com.example.readingtutor.models.ExampleItem
import com.example.readingtutor.models.PictureOption
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

object CurriculumRepository {

    private fun readAssetString(context: Context, path: String): String {
        val inputStream = context.assets.open(path)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val sb = StringBuilder()
        var line: String? = reader.readLine()
        while (line != null) {
            sb.append(line).append("\n")
            line = reader.readLine()
        }
        reader.close()
        return sb.toString()
    }

    fun loadCourseInfo(context: Context): CourseInfo {
        val jsonStr = readAssetString(context, "content/course.json")
        val json = JSONObject(jsonStr)
        val daysArray = json.optJSONArray("days") ?: JSONArray()
        val daysList = mutableListOf<DaySummary>()

        for (i in 0 until daysArray.length()) {
            val d = daysArray.getJSONObject(i)
            daysList.add(
                DaySummary(
                    dayNumber = d.getInt("dayNumber"),
                    title = d.getString("title"),
                    summary = d.getString("summary"),
                    icon = d.optString("icon", "⭐")
                )
            )
        }

        return CourseInfo(
            id = json.getString("id"),
            title = json.getString("title"),
            subtitle = json.getString("subtitle"),
            description = json.getString("description"),
            totalDays = json.getInt("totalDays"),
            days = daysList
        )
    }

    fun loadDayPlan(context: Context, dayNumber: Int): DayPlan {
        val formattedDay = String.format("%02d", dayNumber)
        val jsonStr = readAssetString(context, "content/day_$formattedDay.json")
        val json = JSONObject(jsonStr)

        val targetSoundsArray = json.optJSONArray("targetSounds") ?: JSONArray()
        val targetSounds = mutableListOf<String>()
        for (i in 0 until targetSoundsArray.length()) {
            targetSounds.add(targetSoundsArray.getString(i))
        }

        val activitiesArray = json.optJSONArray("activities") ?: JSONArray()
        val activities = mutableListOf<ActivityItem>()

        for (i in 0 until activitiesArray.length()) {
            val act = activitiesArray.getJSONObject(i)
            val id = act.getString("id")
            val type = act.getString("type")
            val instruction = act.optString("instruction", "")
            val letter = act.optString("letter", "")
            val sound = act.optString("sound", "")
            val targetSound = act.optString("targetSound", "")
            val targetWord = act.optString("targetWord", "")
            val emoji = act.optString("emoji", "")
            val prefix = act.optString("prefix", "")
            val suffix = act.optString("suffix", "")
            val correctAnswer = act.optString("correctAnswer", "")
            val blendedWord = act.optString("blendedWord", "")
            val meaning = act.optString("meaning", "")
            val sentence = act.optString("sentence", "")
            val category = act.optString("category", "")
            val skillName = act.optString("skillName", "")

            // Lists
            val lettersList = parseStringList(act.optJSONArray("letters"))
            val soundsList = parseStringList(act.optJSONArray("sounds"))
            val optionsList = parseStringList(act.optJSONArray("options"))
            val wordsList = parseStringList(act.optJSONArray("words"))
            val helperWordsList = parseStringList(act.optJSONArray("helperWords"))
            val decodableWordsList = parseStringList(act.optJSONArray("decodableWords"))

            // Examples
            val examples = mutableListOf<ExampleItem>()
            val examplesArray = act.optJSONArray("examples")
            if (examplesArray != null) {
                for (j in 0 until examplesArray.length()) {
                    val ex = examplesArray.getJSONObject(j)
                    examples.add(
                        ExampleItem(
                            word = ex.getString("word"),
                            emoji = ex.optString("emoji", "⭐"),
                            sound = ex.optString("sound", "")
                        )
                    )
                }
            }

            // Picture options
            val picOptions = mutableListOf<PictureOption>()
            val picArray = act.optJSONArray("options")
            if (picArray != null && picArray.length() > 0 && picArray.optJSONObject(0) != null) {
                for (j in 0 until picArray.length()) {
                    val po = picArray.getJSONObject(j)
                    picOptions.add(
                        PictureOption(
                            word = po.getString("word"),
                            emoji = po.optString("emoji", "🖼️")
                        )
                    )
                }
            }

            activities.add(
                ActivityItem(
                    id = id,
                    type = type,
                    instruction = instruction,
                    letter = letter,
                    sound = sound,
                    targetSound = targetSound,
                    targetWord = targetWord,
                    emoji = emoji,
                    prefix = prefix,
                    suffix = suffix,
                    correctAnswer = correctAnswer,
                    blendedWord = blendedWord,
                    meaning = meaning,
                    sentence = sentence,
                    category = category,
                    skillName = skillName,
                    letters = lettersList,
                    sounds = soundsList,
                    options = optionsList,
                    words = wordsList,
                    helperWords = helperWordsList,
                    decodableWords = decodableWordsList,
                    examples = examples,
                    pictureOptions = picOptions
                )
            )
        }

        return DayPlan(
            dayNumber = json.getInt("dayNumber"),
            title = json.getString("title"),
            subtitle = json.getString("subtitle"),
            targetSounds = targetSounds,
            activities = activities
        )
    }

    private fun parseStringList(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until array.length()) {
            val opt = array.opt(i)
            if (opt is String) {
                list.add(opt)
            }
        }
        return list
    }
}
