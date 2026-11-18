# Laboratoriya 8 — Room (NOTES.md)

Loyiha: RoomNotes. Eslatmalar ilovasi, Room bilan. 29-savol uchun 20-leksiyadagi Retrofit ham ulangan.

Qanday tekshirdim:
- `gradlew test` — 11 ta JVM test, hammasi o'tdi, emulator kerak emas.
- `gradlew connectedAndroidTest` — 26 ta test, emulator (lab8, API 35), hammasi o'tdi.
- Ilovani emulatorga o'rnatib, qo'lda ham ishlatib ko'rdim (skrinshot: `docs/ilova.png`).

Baza versiyasi 4 ta bo'ldi: 1 → 2 (priority qo'shildi), 2 → 3 (body nomi content ga o'zgardi), 3 → 4 (is_archived olib tashlandi). Schema fayllari `app/schemas/` ichida.

---

## 1-bo'lim. Entity, DAO va Database

### 1-savol (nazariy)

Room ning uchta qismi bor:

`@Entity` — bitta klass bitta jadval bo'ladi. Maydonlar ustunga aylanadi.
`@Dao` — bazaga tegadigan funksiyalar shu yerda. SQL ham shu yerda yoziladi.
`@Database` — entity va DAO larni bir joyga yig'adi, versiyani ushlab turadi.

SQLite da hammasi ishlayotgan vaqtda ma'lum bo'ladi, Room esa build paytida tekshiradi:
1. `@Query` ichidagi SQL to'g'rimi, ustun nomi bormi;
2. funksiya qaytaradigan tur so'rov natijasiga mos keladimi;
3. entity o'zgargan bo'lsa versiya va migratsiya bormi.

Ustiga Room kursorni o'qiydigan kodni ham o'zi yozadi, qo'lda `cursor.getString(0)` yozish kerak emas.

"Compile time da tekshiriladi" degani: `@Query("SELECT * FROM note WHERE titlee = :q")` deb xato yozsam, ilova telefonda yiqilmaydi, build umuman o'tmaydi. Xatoni o'sha zahoti ko'raman.

### 2-savol (Android Studio)

`gradle/libs.versions.toml` ga room 2.6.1 qo'shildi, `app/build.gradle.kts` da:

```kotlin
implementation(libs.room.runtime)
implementation(libs.room.ktx)
ksp(libs.room.compiler)
```

Build qilgandan keyin generatsiya qilingan kod shu yerda turibdi:
`app/build/generated/ksp/debug/java/uz/student/roomnotes/data/NoteDao_Impl.java`

O'sha fayldan `insert` ni ko'chirdim:

```java
public Object insert(final Note note, final Continuation<? super Long> $completion) {
  return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
    @Override
    public Long call() throws Exception {
      __db.beginTransaction();
      try {
        final Long _result = __insertionAdapterOfNote.insertAndReturnId(note);
        __db.setTransactionSuccessful();
        return _result;
      } finally {
        __db.endTransaction();
      }
    }
  }, $completion);
}
```

O'sha faylda SQL ham ko'rinib turibdi:

```
INSERT OR ABORT INTO `note` (`id`,`title`,`content`,`created_at`,`pinned`,`priority`,`notebookId`,`tags`)
VALUES (nullif(?, 0),?,?,?,?,?,?,?)
```

KSP ni nega tanladim: KSP Kotlin kodini to'g'ridan-to'g'ri o'qiydi, KAPT esa avval Java stub yasaydi, keyin shuni o'qiydi. Shuning uchun KSP tezroq ishlaydi va Kotlin ning null bo'lishi mumkin/mumkin emasini to'g'ri tushunadi.

### 3-savol (Android Studio)

`Note` entity: `data/Note.kt`.

```kotlin
@PrimaryKey(autoGenerate = true) val id: Long = 0
@ColumnInfo(name = "created_at") val createdAt: Instant
@Ignore var isSelected: Boolean = false
indices = [Index("created_at"), Index("notebookId")]
```

Build natijasida chiqqan haqiqiy SQL (`app/schemas/.../4.json` dan):

```
CREATE TABLE IF NOT EXISTS `note` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
`title` TEXT NOT NULL, `content` TEXT NOT NULL, `created_at` INTEGER NOT NULL,
`pinned` INTEGER NOT NULL, `priority` TEXT NOT NULL, `notebookId` INTEGER,
`tags` TEXT NOT NULL, FOREIGN KEY(`notebookId`) REFERENCES `notebook`(`id`) ...)
CREATE INDEX `index_note_created_at` ON `note` (`created_at`)
```

Ko'rinib turibdi: ustun nomi `createdAt` emas, `created_at`. `isSelected` esa umuman yo'q, chunki `@Ignore` qo'yilgan.

Emulatorda ilovani ochib eslatma qo'shdim, ishladi: `docs/ilova.png`.

Database Inspector ning skrinshotini qo'ya olmadim, uni Android Studio ning o'zidan ochish kerak. Qilish tartibi: ilovani emulatorda ishga tushirib, View → Tool Windows → App Inspection → Database Inspection → notes.db → note jadvali.

### 4-savol (Android Studio)

DAO: `data/NoteDao.kt`. Qaytadigan qiymatlar:

| funksiya | qaytaradi |
|---|---|
| `insert(note): Long` | yangi qatorning id si |
| `insertAll(list): List<Long>` | har biriga id |
| `update(note): Int` | o'zgargan qatorlar soni |
| `delete(note): Int` | o'chgan qatorlar soni |
| `deleteById(id): Int` | o'chgan qatorlar soni |

Test (`NoteDaoTest.yoqQatorniYangilaganda`): bo'lmagan qatorni yangilasam `0` qaytdi, o'chirsam ham `0`. Ya'ni xato chiqmaydi, shunchaki hech narsa bo'lmaydi. Buni unutib qo'ysam, "saqlandi" deb yozib yuraveraman.

Bir xil primary key bilan `@Insert` qilsam, standart strategiya `ABORT` bo'lgani uchun `SQLiteConstraintException` chiqadi (UNIQUE constraint failed). Generatsiya qilingan SQL da ham `INSERT OR ABORT` deb turibdi.

### 5-savol (Android Studio)

Uchta xatoni ataylab qildim va build xatosini ko'chirib oldim.

1. Bo'lmagan ustun: `@Query("SELECT * FROM note WHERE titlee = :q")`

```
e: [ksp] .../NoteDao.kt:60: There is a problem with the query: [SQLITE_ERROR] SQL error or missing database (no such column: titlee)
e: [ksp] .../NoteDao.kt:60: Not sure how to convert a Cursor to this method's return type (java.util.List<Note>).
```

Keraklisi: `no such column: titlee`. Qaysi ustun yo'qligini to'g'ridan-to'g'ri aytyapti.

2. Noto'g'ri qaytish turi: `@Query("SELECT * FROM note") suspend fun xato2(): Int`

```
e: [ksp] .../NoteDao.kt:60: Not sure how to convert a Cursor to this method's return type (java.lang.Integer).
```

Keraklisi: `return type (java.lang.Integer)`. Men qator so'rayapman, Int emas.

3. Room bilmaydigan tur. `java.util.UUID` ni sinab ko'rdim, u ishlab ketdi (Room 2.4 dan beri UUID ni o'zi biladi). Shuning uchun `java.time.Duration` ni qo'ydim:

```
e: [ksp] .../Note.kt:34: Cannot figure out how to save this field into database. You can consider adding a type converter for it.
e: [ksp] .../Note.kt:34: Cannot figure out how to read this field from a cursor.
```

Keraklisi: `consider adding a type converter`. Yechim ham shu yerda yozib qo'yilgan.

---

## 2-bo'lim. So'rovlar, oqimlar va konverterlar

### 6-savol (Android Studio)

Qo'shilgan so'rovlar (`NoteDao.kt`): `observeAll()` (created_at DESC), `getById(id)`, `search(q)` LIKE bilan, `count()`, `createdAfter(time)`.

`salom` deb qidirsam `Salom dunyo` topildi, demak lotin harflarida LIKE katta-kichikka qaramaydi. SQLite da LIKE faqat ASCII uchun registrni e'tiborsiz qoldiradi. Kirillda shu narsa ishlamaydi: `привет` deb qidirsam `Привет` topilmaydi.

Nima qilish mumkin: sarlavhaning kichik harfli nusxasini alohida ustunga yozib, qidiruvni o'sha ustundan qilish. Oddiyroq yo'li, agar ma'lumot ko'p bo'lmasa, Kotlin tomonda `lowercase()` bilan solishtirish. `COLLATE NOCASE` yordam bermaydi, u ham ASCII uchun.

### 7-savol (nazariy)

| funksiya | qaysi oqimda | main thread dan chaqirsa bo'ladimi |
|---|---|---|
| `suspend fun getAll()` | Room ning o'z fonga oladigan executor ida | ha, chunki suspend, u o'zi fonga o'tkazadi |
| `fun observeAll(): Flow` | yig'ib olayotgan dispatcher da, fonda | ha, collect qilish bloklamaydi |
| `fun getAllBlocking()` | chaqirgan oqimning o'zida | yo'q, IllegalStateException |
| `fun count(): LiveData` | fonda, Room o'zi hisoblaydi | ha |

`Flow` va `LiveData` ga `suspend` kerak emas, chunki ular qiymatni darhol qaytarmaydi. Ular faqat "men shu jadvalni kuzataman" deb obyekt qaytaradi, haqiqiy so'rov keyinroq, fonda bajariladi.

Room har bir yozuvdan keyin xabar bera oladi, chunki ichida InvalidationTracker bor. U jadvalga trigger qo'yadi. Men ilovani ishlatganimda logda o'sha triggerni ko'rdim:

```
CREATE TEMP TRIGGER IF NOT EXISTS `room_table_modification_trigger_note_INSERT` AFTER INSERT ON `note` ...
```

Jadval o'zgarsa, Room so'rovni qayta bajarib, yangi ro'yxatni Flow ga yuboradi.

### 8-savol (Android Studio)

Ilovaga "Blocking test" tugmasi qo'yildi, u `getAllBlocking()` ni to'g'ridan-to'g'ri chaqiradi. Shuni testda ham tekshirdim (`MainThreadTest.kt`), chunki test oqimi main thread emas, shuning uchun `runOnMainSync` ishlatdim. Haqiqiy natija:

```
main thread: java.lang.IllegalStateException: Cannot access database on the main thread since it may potentially lock the UI for a long period of time.
```

`allowMainThreadQueries()` bilan yasalgan bazada o'sha chaqiruv muammosiz o'tdi:

```
allowMainThreadQueries: ishladi, 0 ta
```

Bu sozlama nega bor: testda yoki juda kichik tekshiruvda qulay, hamma narsani coroutine ichiga o'rab o'tirmaysan. Nega ilovaga chiqarib bo'lmaydi: baza so'rovi sekin bo'lsa, ekran qotib qoladi va ANR chiqadi. Foydalanuvchining telefonida ma'lumot ko'p bo'lishi mumkin, men esa buni sezmayman.

`suspend` versiyasi `viewModelScope.launch { }` ichida chaqirilganda main dispatcher da ham normal ishlaydi, ViewModel dagi `add()` shunday qilingan.

### 9-savol (Android Studio)

`data/Converters.kt`: `Instant` ↔ `Long`, `List<String>` ↔ JSON (kotlinx.serialization), `Priority` ↔ `String`.

Test (`NoteDaoTest.teglarVaPriorityRoundTrip`) ichida qiyin teglarni sinadim: bo'sh ro'yxat, ichida vergul bor teg (`"uy, ish"`), qo'shtirnoqli teg, bo'sh satr. Hammasi o'z holicha qaytdi. Agar teglarni vergul bilan qo'shib yozganimda, `uy, ish` degan bitta teg ikkitaga bo'linib ketardi. JSON buni o'zi hal qiladi.

`null` bo'lgan `Instant` uchun konverter `null` qaytaradi, shuning uchun nullable maydonga ham yozsa bo'ladi.

Enum ni nomi bilan saqlash xavfsizroq, chunki `ordinal` tartib raqami. Keyin enum ichiga yangi qiymatni o'rtaga qo'shib qo'ysam, eski qatorlardagi raqamlar boshqa qiymatni ko'rsatib qoladi. Nom esa o'zgarmaydi.

### 10-savol (Android Studio)

`data/Contact.kt` da `Address(street, city)` ikki marta ishlatilgan. Prefix siz build xatosi:

```
e: [ksp] .../Contact.kt:14: Multiple fields have the same columnName: street. Field names: homeAddress > street, workAddress > street.
e: [ksp] .../Contact.kt:8: Field has non-unique column name.
e: [ksp] .../Contact.kt:14: Multiple fields have the same columnName: city. Field names: homeAddress > city, workAddress > city.
```

Tuzatish: `@Embedded(prefix = "work_")`. Shundan keyin ustunlar `street`, `city`, `work_street`, `work_city` bo'ldi (4.json da ko'rinadi).

Qoida: `@Embedded` ni obyekt doim bitta bo'lsa va alohida yashashi kerak bo'lmasa ishlataman (manzil, koordinata). Agar obyekt ko'p bo'lsa, alohida izlanadi yoki o'chirilsa, alohida jadval qilaman.

---

## 3-bo'lim. ViewModel, Flow va Compose

### 11-savol (Android Studio)

`NoteViewModel` konstruktorda DAO oladi, shuning uchun oddiy factory ishlamaydi. `NoteViewModel.Factory` yozdim:

```kotlin
val Factory = viewModelFactory {
    initializer {
        val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
        NoteViewModel(AppDatabase.get(app).noteDao())
    }
}
```

Emulatorda ekranni aylantirib logni oldim:

```
10:20:16 D VMtest: viewModel hashCode = 50431690
10:20:36 D VMtest: viewModel hashCode = 50431690   (aylantirgandan keyin)
```

hashCode o'zgarmadi, demak ViewModel saqlanib qoldi. Agar composable ichida `NoteViewModel(dao)` deb qo'lda yasasam, har aylantirganda yangi obyekt bo'lardi va hashCode boshqacha chiqardi, ro'yxat ham noldan yuklanardi.

### 12-savol (Android Studio)

Ekran: `ui/NoteScreen.kt`. Sarlavha va matn maydoni, "Qo'shish" tugmasi, ro'yxat, har bir eslatmada "O'chirish".

Tekshirish ViewModel da:
- bo'sh sarlavha qabul qilinmaydi;
- sarlavha `trim()` qilinadi;
- 100 belgidan uzun bo'lsa qabul qilinmaydi.

Testlar `app/src/test/.../NoteViewModelTest.kt` da, yasama DAO bilan (`FakeNoteDao`, ichida `MutableStateFlow`). 6 ta test, emulator kerak emas, `gradlew test` bilan o'tadi. 100 belgili sarlavha o'tadi, 101 belgili o'tmaydi, chegarani ham tekshirdim.

### 13-savol (nazariy)

`stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())`:

1-argument, scope: oqim qaysi scope da yashaydi. ViewModel o'lsa, oqim ham to'xtaydi.
2-argument, started: qachon boshlanadi va qachon to'xtaydi.
3-argument, initialValue: hech kim hali hech narsa yubormaganda turadigan qiymat. Ekran birinchi marta chizilganda bo'sh ro'yxat ko'rsatiladi.

5000 ms nega: ekranni aylantirganda ekran bir zumga yo'qoladi va qaytadan yaratiladi. 5 soniya kutib tursa, o'sha paytda bazaga qayta ulanish shart emas, eski natija turaveradi. Agar foydalanuvchi ilovadan chiqib ketsa va 5 soniyadan ko'p qaytmasa, oqim to'xtaydi va bazani bekorga kuzatib turmaydi.

`Eagerly` qo'ysam, oqim ViewModel yaratilishi bilan boshlanadi va ViewModel o'lguncha to'xtamaydi. Ekran ko'rinmay turganda ham so'rovlar ketaveradi, bu batareyani bekorga yeydi.

`collectAsState()` ekran ko'rinmay qolsa ham yig'ishda davom etadi. `collectAsStateWithLifecycle()` esa lifecycle STARTED dan pastga tushsa to'xtaydi. Shuning uchun ikkinchisi to'g'riroq.

### 14-savol (Android Studio)

ViewModel da: `query` → `debounce(300)` → `flatMapLatest { dao.search(it) }` → `stateIn`.

Ikki joyda tekshirdim.

1. JVM test (`DebounceTest.kt`), yasama DAO qidiruv necha marta chaqirilganini sanaydi:

```
tez yozganda qidiruv chaqirildi: 1 marta     (6 ta harf, orasi 50 ms)
sekin yozganda qidiruv chaqirildi: 3 marta   (3 ta harf, orasi 400 ms)
```

2. Emulatorda. Bazaga `setQueryCallback` qo'ydim (`AppDatabase.kt`), u har bir SQL ni logga yozadi. Qidiruv maydoniga `salom` deb tez yozdim. Logda faqat bitta qidiruv so'rovi chiqdi:

```
D RoomSQL: SELECT * FROM note WHERE title LIKE '%' || ? || '%' ORDER BY created_at DESC  args=[salom]
```

Ya'ni 5 ta harfga 5 ta so'rov emas, 1 ta so'rov. `debounce` bo'lmasa har bir harfga bittadan, 5 ta so'rov ketardi.

### 15-savol (Android Studio)

`SortOrder` sealed interface: `ByDate`, `ByTitle`, `PinnedFirst`. ViewModel da `combine(found, sortOrder)` bilan birlashtirildi. Ekran `Note` entity ni emas, `NoteUi(id, title, preview, dateText)` ni ko'radi.

Testi: `SortOrderTest.kt`, 5 ta eslatma, ikkitasining nomi `Alma`. 3 ta test ham o'tdi.

---

## 4-bo'lim. Aloqalar va cheklovlar

### 16-savol (Android Studio)

`Notebook` entity qo'shildi, `Note` ga `notebookId` va `ForeignKey(onDelete = CASCADE)`, ustiga `Index("notebookId")`.

```kotlin
@Transaction
@Query("SELECT * FROM notebook")
fun observeWithNotes(): Flow<List<NotebookWithNotes>>
```

`@Transaction` nega kerak: Room bitta so'rov bilan daftarlarni, keyin ikkinchi so'rov bilan ularning eslatmalarini oladi. Ikkala so'rov orasida kimdir eslatma qo'shsa yoki o'chirsa, natija chalkash bo'ladi. `@Transaction` ikkalasini bitta tranzaksiyaga o'rab qo'yadi.

Test: `RelationTest.daftarVaEslatmalariBirgaKeladi`, o'tdi.

### 17-savol (nazariy)

Ota qator o'chirilganda:
- `CASCADE` — bolalari ham o'chadi;
- `RESTRICT` — bolasi bor bo'lsa o'chirishga umuman ruxsat bermaydi, xato chiqadi;
- `SET_NULL` — bolalarning o'sha ustuni null bo'ladi, o'zi qoladi;
- `NO_ACTION` — hech narsa qilmaydi, tekshiruv tranzaksiya oxiriga qoldiriladi.

SQLite da foreign key tekshiruvi standart holatda o'chirilgan, uni `PRAGMA foreign_keys = ON` bilan yoqish kerak. Room buni o'zi yoqib qo'yadi. Shuning uchun mening testimda haqiqiy xato chiqdi:

```
xato: FOREIGN KEY constraint failed (code 787 SQLITE_CONSTRAINT_FOREIGNKEY)
```

Bola ustunida indeks bo'lmasa, Room build paytida ogohlantiradi. Sababi: otani o'chirganda yoki yangilaganda SQLite bola jadvalni boshdan oxir skanerlab chiqadi. Jadval katta bo'lsa, bitta o'chirish sekin ishlaydi.

### 18-savol (Android Studio)

`RelationTest.kt`, xotiradagi baza bilan:
- `daftarOchirilsaEslatmalarHamOchadi` — daftar o'chdi, 2 ta eslatma ham yo'qoldi (cascade ishlayapti);
- `yoqDaftargaEslatmaQoshilmaydi` — `assertFailsWith<SQLiteConstraintException>`, yuqoridagi 787 xatosi chiqdi.

`RESTRICT` uchun alohida test yozmadim. Uni sinash uchun `Note` dagi `onDelete` ni `ForeignKey.RESTRICT` ga o'zgartirib, bolasi bor daftarni o'chirish kerak. O'shanda ham `SQLiteConstraintException` chiqadi, faqat matni boshqacha: `FOREIGN KEY constraint failed` o'rniga RESTRICT haqida yozadi.

### 19-savol (Android Studio)

Ikkita test yozdim, natija aniq:

`replaceMalumotniYoqotadi`: daftar va 3 ta eslatma bor. O'sha daftarni `OnConflictStrategy.REPLACE` bilan qayta `insert` qildim. Keyin eslatmalar soni `0` bo'ldi.

`upsertMalumotniSaqlaydi`: xuddi shu holat, lekin `@Upsert` bilan. Eslatmalar soni `3` bo'lib qoldi.

Sababi: REPLACE aslida `DELETE` qilib, keyin `INSERT` qiladi. Daftar o'chganda cascade uning eslatmalarini ham olib ketadi. `@Upsert` esa qator bor bo'lsa `UPDATE` qiladi, hech narsa o'chmaydi, shuning uchun bolalar joyida qoladi.

### 20-savol (Android Studio)

`Tag` va `NoteTagCrossRef(noteId, tagId)` qo'shildi, primary key ikkala ustundan iborat, ikkalasiga indeks qo'yilgan. `NoteWithTags` va `TagWithNotes` da `Junction(NoteTagCrossRef::class)` ishlatilgan.

Test (`teglarIkkiTomondanOqiladi`): 2 ta eslatma, 2 ta teg, 3 ta bog'lanish. Birinchi eslatmaning 2 ta tegi chiqdi, `uy` tegining 2 ta eslatmasi chiqdi. Ikkala tomon ham ishlayapti.

---

## 5-bo'lim. Indekslar va migratsiyalar

### 21-savol (Android Studio)

`IndexTest.kt`: 50 000 ta eslatma yozib, `EXPLAIN QUERY PLAN` ni indeks bor va indeks yo'q holatda bajardim. Haqiqiy natija:

```
jami: 50000
indeks bor -> SEARCH note USING INDEX index_note_created_at (created_at>?)
topildi 1000 ta, vaqt 8 ms
indeks yo'q -> SCAN note
topildi 1000 ta, vaqt 5 ms
```

Reja o'zgarishi ko'rinib turibdi: `SEARCH ... USING INDEX` edi, indeksni `DROP INDEX` qilgandan keyin `SCAN note` bo'ldi, ya'ni jadvalni boshdan oxir o'qiydi.

Lekin vaqt kutganimdek chiqmadi, indekssiz hatto 3 ms tezroq bo'ldi. Buning sababi testda baza xotirada (`inMemoryDatabaseBuilder`), diskdan o'qish yo'q, 50 000 qator esa juda katta emas. Haqiqiy telefonda, diskdagi bazada va ko'proq ma'lumotda farq sezilarli bo'ladi. Bu menga shuni ko'rsatdi: reja o'zgarsa ham, tezlik avtomatik oshib ketmaydi, o'lchab ko'rish kerak.

### 22-savol (nazariy)

Room schema ning "identity hash" ini hisoblaydi va uni bazaning ichidagi `room_master_table` ga yozib qo'yadi. Ilova ochilganda telefondagi hash bilan koddagi schema hashini solishtiradi. Mos kelmasa, versiya raqamiga qarab migratsiya qidiradi. Topolmasa `IllegalStateException` bilan yiqiladi.

Ustun nomini o'zgartirib, versiyani oshirmasa yoki migratsiya yozmasa, foydalanuvchining telefonida xuddi shunday bo'ladi.

`exportSchema = true` har bir versiya uchun JSON fayl chiqaradi. Mening loyihamda: `app/schemas/uz.student.roomnotes.data.AppDatabase/1.json ... 4.json`. Shular bo'lmasa migratsiyani testda tekshirib bo'lmaydi, chunki `MigrationTestHelper` aynan shu fayllarga qarab solishtiradi.

Schema o'zgarganda uchta yo'l bor:
1. qo'lda `Migration` yozish, ya'ni SQL ni o'zing yozasan;
2. `@AutoMigration`, Room o'zi yozadi, lekin oddiy holatlar uchun;
3. `fallbackToDestructiveMigration()`, bazani o'chirib tashlab, noldan yasaydi.

Uchinchisi faqat debug build da maqul, chunki u foydalanuvchining hamma ma'lumotini o'chirib yuboradi. O'zim ishlab turganimda qulay, lekin ilovaga chiqsa odamlarning eslatmalari yo'qoladi.

### 23-savol (Android Studio)

`app/build.gradle.kts`:

```kotlin
ksp { arg("room.schemaLocation", "$projectDir/schemas") }
```

Versiyani bosqichma-bosqich oshirib build qildim, natijada 4 ta fayl chiqdi: `1.json`, `2.json`, `3.json`, `4.json`.

`1.json` va `2.json` farqi (`diff` natijasi):

```
< "version": 1,
< "identityHash": "4ef1f247649dfc42561e13136ba945a3",
> "version": 2,
> "identityHash": "8dba75c52fe8c952c07c5c40a98e7e76",

createSql da: ... `pinned` INTEGER NOT NULL, `is_archived` INTEGER NOT NULL,
                 + `priority` TEXT NOT NULL, `notebookId` INTEGER ...

fields ro'yxatiga qo'shildi:
> "fieldPath": "priority", "columnName": "priority", "affinity": "TEXT", "notNull": true

> "INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '8dba75c5...')"
```

Ya'ni o'zgargan narsalar: versiya raqami, identity hash (ikki joyda) va `note` jadvaliga bitta ustun.

### 24-savol (Android Studio)

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE note ADD COLUMN priority TEXT NOT NULL DEFAULT 'NORMAL'")
    }
}
```

Test `MigrationTest.migratsiya1dan2ga`: 1-versiyada 2 ta qator yozdim, migratsiyani ishga tushirdim, 2.json ga solishtirdim. Ikkala qator ham joyida qoldi va `priority` ustuni `NORMAL` bo'lib to'ldi.

Keyin SQL ni ataylab buzdim: `priority` o'rniga `prioriti` deb yozdim. Test yiqildi:

```
MigrationTest > migratsiya1dan2ga FAILED
java.lang.IllegalStateException: Migration didn't properly handle: note
Expected: TableInfo{name='note', columns={... priority=Column{name='priority', type='TEXT', notNull=true ...} ...}}
```

Xabar aniq: Room schema da `priority` ustunini kutgan, bazada esa topmagan. Keyin SQL ni tuzatib qayta o'tkazdim, test yana o'tdi.

### 25-savol (Android Studio)

3-versiya, `body` → `content`, avtomatik:

```kotlin
@RenameColumn(tableName = "note", fromColumnName = "body", toColumnName = "content")
class RenameBodySpec : AutoMigrationSpec
```

va `@Database(autoMigrations = [AutoMigration(from = 2, to = 3, spec = RenameBodySpec::class)])`.

4-versiya, `is_archived` ni olib tashlash, qo'lda. SQLite da ustunni o'chirish yo'q (eski versiyalarda), shuning uchun: yangi jadval yasadim, ma'lumotni ko'chirdim, eskisini `DROP` qildim, yangisini `RENAME` qildim va indekslarni qayta yaratdim. Kodi `Migrations.kt` da, `MIGRATION_3_4`.

Ikkalasi ham `MigrationTestHelper` bilan tekshirildi (`migratsiya2dan3gaAvtomatik`, `migratsiya3dan4ga`), ikkalasi ham o'tdi. Avtomatik migratsiyani tekshirish uchun helper ga spec ni berish kerak bo'ldi:

```kotlin
MigrationTestHelper(instrumentation, AppDatabase::class.java, listOf(RenameBodySpec()))
```

Telefonda 2-versiya ma'lumoti bor bo'lsa va migratsiyasiz yangi versiya chiqarilsa, ilova ochilishida yiqiladi (`IllegalStateException: A migration from 2 to 4 was required but not found`). `fallbackToDestructiveMigration()` qo'yilsa yiqilmaydi, lekin baza tozalanadi va eslatmalar yo'qoladi. Shuning uchun uni faqat debug da ishlatish kerak.

---

## 6-bo'lim. Testlar, offline-first va yakuniy ish

### 26-savol (Android Studio)

`NoteDaoTest.kt`, har bir test uchun yangi xotiradagi baza:

```kotlin
Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
    .allowMainThreadQueries()
    .build()
```

`@Before` da ochiladi, `@After` da yopiladi. 8 ta test: qo'shib o'qish, yangilash, o'chirish, bo'lmagan qator, tartib, qidiruv, `@Upsert`, konverterlar. Hammasi o'tdi.

### 27-savol (Android Studio)

`NoteFlowTest.kt`, Turbine bilan 3 ta test:
1. avval bo'sh ro'yxat keldi, eslatma qo'shilgach 1 ta elementli ro'yxat keldi;
2. eslatmani yangilaganda yangi ro'yxat keldi;
3. `notebook` jadvaliga yozganda `note` oqimi jim turdi (`expectNoEvents()`).

Uchinchisi nega shunday: Room ning InvalidationTracker so'rovda qatnashgan jadvallarnigina kuzatadi. `notebook` ga yozilgani `note` ga tegmaydi. Agar oqimni o'zim yasab, har qanday yozuvdan keyin "yangilandi" deb xabar bersam, bu test yiqilardi, chunki butunlay boshqa jadval o'zgarganda ham ekran qayta chizilaverardi.

### 28-savol (Android Studio)

`NoteRepository` qo'shildi (`data/NoteRepository.kt`), DAO ustida yupqa qatlam. ViewModel testlari yasama DAO bilan ishlaydi, `Dispatchers.setMain(StandardTestDispatcher())` orqali.

`gradlew test` natijasi: 11 ta test (6 ta ViewModel, 3 ta tartib, 2 ta debounce), hammasi o'tdi, emulator ulanmagan.

### 29-savol (Android Studio)

`network/` papkasi: `GitHubService` (Retrofit, 20-leksiyadagidek `@GET("/users")`), `UserEntity` va `UserDao` (kesh), `UserRepository`.

Repository ekranga faqat bazadan beradi:

```kotlin
fun users(): Flow<List<User>> = userDao.observeAll().map { ... }

suspend fun refresh() {
    val fromNetwork = service.users()
    userDao.replaceAll(fromNetwork.map { UserEntity(it.id, it.login, it.avatarUrl) })
}
```

`replaceAll` `@Transaction` bilan belgilangan, ya'ni eski ma'lumot o'chib yangisi yozilishi bitta amal.

`UserCacheTest.kt`, MockWebServer bilan 3 ta test, hammasi o'tdi:
1. birinchi marta serverdan oldi va bazaga yozdi (2 ta user);
2. serverni o'chirib qo'ydim, `refresh()` xato berdi (`ConnectException`), lekin kesh joyida qoldi va ekranga 2 ta user chiqaverdi;
3. server `404` qaytardi (`HttpException`), kesh yana o'zgarmadi.

Ikkinchi va uchinchi holatda xatoni `try/catch` bilan ushladim, baza esa tegilmagani uchun ilova baribir ma'lumot ko'rsatadi.

### 30-savol (Android Studio)

Yakuniy holat:
- daftarlar va eslatmalar, cascade bilan;
- qidiruv, `debounce(300)` bilan;
- tartiblash (sana, nom, pinned);
- teglar, ko'pga-ko'p aloqa orqali;
- 4 ta schema fayli eksport qilingan;
- 3 ta migratsiya (1→2 qo'lda, 2→3 avtomatik, 3→4 qo'lda), har biriga test;
- xotiradagi baza bilan DAO testlari, Turbine testlari, JVM ViewModel testlari;
- `README.md` da ER diagramma bor.

Jami: 11 ta JVM test, 26 ta instrumented test.

---

## Qilinmagan yoki qisman qilingan ishlar

Halol bo'lsin deb yozib qo'yaman:

1. Database Inspector skrinshotlari yo'q (3 va 21-savol). Buning o'rniga schema JSON va `EXPLAIN QUERY PLAN` natijasini qo'ydim. Inspector ni Android Studio dan ochish kerak, u buyruq satridan ishlamaydi.
2. 11-savolda faqat factory variantini qurilmada tekshirdim. Qo'lda yasalgan ViewModel ni sinab ko'rmadim, hashCode o'zgarishini faqat tushuntirdim.
3. 18-savolda RESTRICT uchun alohida test yo'q, chunki loyihada foreign key CASCADE bilan ishlaydi.
4. 21-savolda vaqt o'lchovi xotiradagi bazada qilingan, shuning uchun indeksning foydasi vaqt bo'yicha ko'rinmadi.
5. Commit tarixi yo'q, loyiha git ga qo'yilmagan. Har savolga alohida commit qilish kerak edi (`q07: return types`).
