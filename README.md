# 📝 retrosheet
Turn Google Spreadsheet to JSON endpoint.

![https://github.com/theapache64/notes](demo.png)

## 🤝 Benefits

- 🖱️ asy  to use and real time interface (GoogleSheet)
- 🔥 Free analytics via Google forms
- 🔄 Migrate to your REST API with minimal code changes.
- 📊 Manage data directly through the Google Spreadsheet app.
- 🏃‍♂️ Speed up development of your POC or MVP with this library.


## 🚀 Platform Supported

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white) ![iOS](https://img.shields.io/badge/iOS-000000?style=for-the-badge&logo=ios&logoColor=white) ![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white) ![JavaScript](https://img.shields.io/badge/javascript-%23323330.svg?style=for-the-badge&logo=javascript&logoColor=%23F7DF1E)


## 🤝 Install

![latestVersion](https://img.shields.io/github/v/release/theapache64/retrosheet)

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.theapache64:retrosheet:<latest.version>")
}
```

## ⌘️ Usage

### ✍️ Writing Data

#### 📝 Step 1: Create a Google Form
Create a form with required fields.  
![Google Form](https://i.imgur.com/9PeK2EQ.png)

#### 🎯 Step 2: Set Response Destination
Choose a Google Sheet to save responses.  
![Response Destination](https://i.imgur.com/fIzWiN5.png)  
![Sheet Selection](https://i.imgur.com/7ASAB55.png)

#### 📊 Step 3: Customize Sheet
Rename sheet and columns (optional).  
![Before](https://i.imgur.com/keT8P1o.png)  
![After](https://i.imgur.com/N6xfuZK.png)

#### 🔗 Step 4: Get Form Link
Press `Send` and copy the link.  
![Form Link](https://i.imgur.com/veATAn5.png)

#### 🔧 Step 5: Create `RetrosheetConfig` and attach it to the client
```kotlin
val config = RetrosheetConfig.Builder()
    .setLogging(true)
    // For reading from sheet
    .addSheet(
        "notes", // sheet name
        "created_at", "title", "description" // columns in same order
    )
    // For writing to sheet
    .addForm(
        "add_note",
        "https://docs.google.com/forms/d/e/1FAIpQLSdmavg6P4eZTmIu-0M7xF_z-qDCHdpGebX8MGL43HSGAXcd3w/viewform?usp=sf_link" // form link
    )
    .build()

val ktorClient = HttpClient {
    install(createRetrosheetPlugin(config)) {}
    ...
}
```

#### 🌐 Step 6: Create API Interface
```kotlin
interface NotesApi {
    @Read("SELECT *")
    @GET("notes")
    suspend fun getNotes(): List<Note>

    @Write
    @POST("add_note")
    suspend fun addNote(@Body note: Note): Note
}
```

> **@Write** is used for writing data and **@Read** for reading data.

[Query Language Guide](https://developers.google.com/chart/interactive/docs/querylanguage)

### 📚 Reading Data

#### 🔄 Step 7: Share Sheet
Open a sheet and copy its shareable link.  
![Copy Link](https://i.imgur.com/MNYD7mg.png)

#### ✂️ Step 8: Edit Link
Trim the link after the last '/'.

`https://docs.google.com/spreadsheets/d/1IcZTH6-g7cZeht_xr82SHJOuJXD_p55QueMrZcnsAvQ`~~/edit?usp=sharing~~

#### 🔗 Step 9: Set Base URL
Use the trimmed link as `baseUrl` in `Ktorfit`.

```kotlin
val retrofit = Ktorfit.Builder()
    // Like this 👇🏼
    .baseUrl("https://docs.google.com/spreadsheets/d/1YTWKe7_mzuwl7AO1Es1aCtj5S9buh3vKauKCMjx1j_M/")
    .httpClient(ktorClient)
    .converterFactories(RetrosheetConverter(config))
    .build()
```

**Done 👍**

## 🌠 Full Example

**build.gradle.kts**
```kotlin
plugins {
    kotlin("jvm") version "2.1.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
    id("com.google.devtools.ksp") version "2.1.10-1.0.31"
    id("de.jensklingenberg.ktorfit") version "2.5.1"
}
...
dependencies {
    implementation("io.ktor:ktor-client-content-negotiation:3.1.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.3")
    implementation("de.jensklingenberg.ktorfit:ktorfit-lib:2.5.1")
    implementation("io.github.theapache64:retrosheet:3.0.0-alpha02")
    testImplementation(kotlin("test"))
}
...
```

**NotesApi.kt**

```kotlin
interface NotesApi {
    @Read("SELECT *")
    @GET("notes")
    suspend fun getNotes(): List<Note>

    @Write
    @POST("add_note")
    suspend fun addNote(@Body note: Note): Note
}
```

**Main.kt**
```kotlin
@Serializable
data class Note(
    @SerialName("Title")
    val title: String,
    @SerialName("Description")
    val description: String?,
    @SerialName("Timestamp")
    val createdAt: String? = null,
)


suspend fun main() {
    val notesApi = createMyNotesApi()
    println(notesApi.getNotes())

    // Adding sample order
    val newNote = notesApi.addNote(
        Note(
            createdAt = null,
            title = "Dynamic com.sample.Note 1",
            description = "Dynámic Desc 1: ${Date()}"
        )
    )

    println(newNote)
}


fun createMyNotesApi(
    configBuilder: RetrosheetConfig.Builder.() -> Unit = {}
): NotesApi {
    val config = RetrosheetConfig.Builder()
        .apply { this.configBuilder() }
        .setLogging(true)
        // To Read
        .addSheet(
            "notes", // sheet name
            "created_at", "title", "description" // columns in same order
        )
        // To write
        .addForm(
            "add_note",
            // Google form name
            "https://docs.google.com/forms/d/e/1FAIpQLSdmavg6P4eZTmIu-0M7xF_z-qDCHdpGebX8MGL43HSGAXcd3w/viewform?usp=sf_link"
        )
        .build()

    val ktorClient = HttpClient {
        install(createRetrosheetPlugin(config)) {}
        install(ContentNegotiation) {
            json()
        }
    }

    val ktorfit = Ktorfit.Builder()
        // GoogleSheet Public URL
        .baseUrl("https://docs.google.com/spreadsheets/d/1YTWKe7_mzuwl7AO1Es1aCtj5S9buh3vKauKCMjx1j_M/")
        .httpClient(ktorClient)
        .converterFactories(RetrosheetConverter(config))
        .build()

    return ktorfit.createNotesApi()
}
```
- Source: https://github.com/theapache64/retrosheet-jvm-sample. Check `sample` directory for more samples

## 🔄 Migration
- Want to migrate from v1 or v2?Here's the [guide](https://github.com/theapache64/retrosheet/blob/master/MIGRATION.md)

## 🤝 Contributing
This project applies [`ktlint`](https://ktlint.github.io/) (without import ordering since it's conflicted with IDE's format). Before creating a PR, please make sure your code is aligned with `ktlint` (`./gradlew ktlint`).

We can run auto-format with:
```shell
./gradlew ktlintFormat
```

## Must Read ✋🏼
Retrosheet is great for prototyping and shouldn’t be used in production for a real app. That said, I do use it in production for a few of my [side projects](https://github.com/theapache64/stackzy) for more than 5 years now. This library makes direct calls to Google APIs—so if they go down, we all go down. (So I'll be right there, drowning in tears with you.)

## ✍️ Author
- theapache64  

