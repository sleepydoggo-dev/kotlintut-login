package com.example.kotlintut.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.kotlintut.data.model.*
import com.example.kotlintut.data.network.NetworkExtra
import com.example.kotlintut.data.network.NetworkIngredient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "RistoranteTotem_Compose.db"
        private const val DATABASE_VERSION = 12

        const val TABLE_USERS = "utenti"
        const val COLUMN_USER_ID = "id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_NOME = "nome"
        const val COLUMN_CARD_NUMBER = "card_number"
        const val COLUMN_CARD_EXPIRY = "card_expiry"
        const val COLUMN_CARD_CVV = "card_cvv"

        const val TABLE_CATEGORIES = "categorie"
        const val COLUMN_CAT_ID = "id"
        const val COLUMN_CAT_REMOTE_ID = "remote_id"
        const val COLUMN_CAT_NAME = "nome"

        const val TABLE_PRODUCTS = "prodotti"
        const val COLUMN_PROD_ID = "id"
        const val COLUMN_PROD_REMOTE_ID = "remote_id"
        const val COLUMN_PROD_NAME = "nome_key"
        const val COLUMN_PROD_PRICE = "prezzo"
        const val COLUMN_PROD_DESC = "desc_key"
        const val COLUMN_PROD_CAT = "categoria"
        const val COLUMN_PROD_IMG_URL = "immagine_url"
        const val COLUMN_PROD_AVAILABLE = "disponibile"
        const val COLUMN_PROD_ATTRIBUTES = "attributi"
        const val COLUMN_PROD_IVA = "iva"

        const val TABLE_CART = "carrello_salvato"
        const val COLUMN_CART_ID = "id"
        const val COLUMN_CART_USER = "username"
        const val COLUMN_CART_PROD_ID = "prodotto_id"
        const val COLUMN_CART_NOME = "prodotto_nome"
        const val COLUMN_CART_PREZZO = "prodotto_prezzo"
        const val COLUMN_CART_DESC = "prodotto_desc"
        const val COLUMN_CART_QTY = "quantita"
        const val COLUMN_CART_IMG = "prodotto_img"
        const val COLUMN_CART_REMOVED_ING = "removed_ingredients"
        const val COLUMN_CART_ADDED_EXT = "added_extras"
        const val COLUMN_CART_ATTRIBUTES = "selected_attributes"
        const val COLUMN_CART_FULL_ATTRIBUTES = "full_attributes"
        const val COLUMN_CART_CAT = "prodotto_categoria"

        const val TABLE_ORDERS = "ordini"
        const val COLUMN_ORDER_ID = "id"
        const val COLUMN_ORDER_USER = "username"
        const val COLUMN_ORDER_DATE = "data"
        const val COLUMN_ORDER_TOTAL = "totale"

        const val TABLE_ORDER_ITEMS = "ordini_dettagli"
        const val COLUMN_ITEM_ID = "id"
        const val COLUMN_ITEM_ORDER_ID = "ordine_id"
        const val COLUMN_ITEM_PROD_ID = "prodotto_id"
        const val COLUMN_ITEM_NAME = "prodotto_nome"
        const val COLUMN_ITEM_PRICE = "prodotto_prezzo"
        const val COLUMN_ITEM_QTY = "quantita"
        const val COLUMN_ITEM_REMOVED_ING = "removed_ingredients"
        const val COLUMN_ITEM_ADDED_EXT = "added_extras"
        const val COLUMN_ITEM_ATTRIBUTES = "selected_attributes"
        const val COLUMN_ITEM_FULL_ATTRIBUTES = "full_attributes"
        const val COLUMN_ITEM_CAT = "prodotto_categoria"

        const val TABLE_INGREDIENTS = "ingredienti"
        const val COLUMN_ING_ID = "id_ingrediente"
        const val COLUMN_ING_PROD_ID = "product_id"
        const val COLUMN_ING_NAME = "nome"
        const val COLUMN_ING_REMOVABLE = "eliminabile"

        const val TABLE_EXTRAS = "aggiunte"
        const val COLUMN_EXT_ID = "id_aggiunta"
        const val COLUMN_EXT_PROD_ID = "product_id"
        const val COLUMN_EXT_NAME = "nome"
        const val COLUMN_EXT_PRICE = "prezzo"

        const val TABLE_FAVORITES = "preferiti"
        const val COLUMN_FAV_ID = "id"
        const val COLUMN_FAV_USER = "username"
        const val COLUMN_FAV_PROD_NAME = "prodotto_nome"
        const val COLUMN_FAV_PROD_PRICE = "prodotto_prezzo"
        const val COLUMN_FAV_PROD_DESC = "prodotto_desc"
        const val COLUMN_FAV_PROD_IMG = "prodotto_img"
    }

    /** Crea le tabelle del database al primo avvio dell'applicazione. */
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT,
                $COLUMN_EMAIL TEXT,
                $COLUMN_PASSWORD TEXT,
                $COLUMN_NOME TEXT,
                $COLUMN_CARD_NUMBER TEXT,
                $COLUMN_CARD_EXPIRY TEXT,
                $COLUMN_CARD_CVV TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_CATEGORIES (
                $COLUMN_CAT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CAT_REMOTE_ID TEXT UNIQUE,
                $COLUMN_CAT_NAME TEXT,
                categoria_padre TEXT,
                posizionamento INTEGER,
                visibile INTEGER DEFAULT 1
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_PRODUCTS (
                $COLUMN_PROD_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_PROD_REMOTE_ID TEXT UNIQUE,
                $COLUMN_PROD_NAME TEXT,
                $COLUMN_PROD_PRICE REAL,
                $COLUMN_PROD_DESC TEXT,
                $COLUMN_PROD_CAT TEXT,
                $COLUMN_PROD_IMG_URL TEXT,
                $COLUMN_PROD_AVAILABLE INTEGER,
                $COLUMN_PROD_ATTRIBUTES TEXT,
                $COLUMN_PROD_IVA TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_CART (
                $COLUMN_CART_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CART_USER TEXT,
                $COLUMN_CART_PROD_ID TEXT,
                $COLUMN_CART_NOME TEXT,
                $COLUMN_CART_PREZZO REAL,
                $COLUMN_CART_DESC TEXT,
                $COLUMN_CART_QTY INTEGER,
                $COLUMN_CART_IMG TEXT,
                $COLUMN_CART_REMOVED_ING TEXT,
                $COLUMN_CART_ADDED_EXT TEXT,
                $COLUMN_CART_ATTRIBUTES TEXT,
                $COLUMN_CART_FULL_ATTRIBUTES TEXT,
                $COLUMN_CART_CAT TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_ORDERS (
                $COLUMN_ORDER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_ORDER_USER TEXT,
                $COLUMN_ORDER_DATE TEXT,
                $COLUMN_ORDER_TOTAL REAL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_ORDER_ITEMS (
                $COLUMN_ITEM_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_ITEM_ORDER_ID INTEGER,
                $COLUMN_ITEM_PROD_ID TEXT,
                $COLUMN_ITEM_NAME TEXT,
                $COLUMN_ITEM_PRICE REAL,
                $COLUMN_ITEM_QTY INTEGER,
                $COLUMN_ITEM_REMOVED_ING TEXT,
                $COLUMN_ITEM_ADDED_EXT TEXT,
                $COLUMN_ITEM_ATTRIBUTES TEXT,
                $COLUMN_ITEM_FULL_ATTRIBUTES TEXT,
                $COLUMN_ITEM_CAT TEXT,
                FOREIGN KEY($COLUMN_ITEM_ORDER_ID) REFERENCES $TABLE_ORDERS($COLUMN_ORDER_ID)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_FAVORITES (
                $COLUMN_FAV_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_FAV_USER TEXT,
                $COLUMN_FAV_PROD_NAME TEXT,
                $COLUMN_FAV_PROD_PRICE REAL,
                $COLUMN_FAV_PROD_DESC TEXT,
                $COLUMN_FAV_PROD_IMG TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_INGREDIENTS (
                $COLUMN_ING_ID TEXT PRIMARY KEY,
                $COLUMN_ING_PROD_ID TEXT,
                $COLUMN_ING_NAME TEXT,
                $COLUMN_ING_REMOVABLE TEXT,
                FOREIGN KEY($COLUMN_ING_PROD_ID) REFERENCES $TABLE_PRODUCTS($COLUMN_PROD_REMOTE_ID)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_EXTRAS (
                $COLUMN_EXT_ID TEXT PRIMARY KEY,
                $COLUMN_EXT_PROD_ID TEXT,
                $COLUMN_EXT_NAME TEXT,
                $COLUMN_EXT_PRICE REAL,
                FOREIGN KEY($COLUMN_EXT_PROD_ID) REFERENCES $TABLE_PRODUCTS($COLUMN_PROD_REMOTE_ID)
            )
        """.trimIndent())

        // Rimosso insertInitialProducts(db) per pulizia dati mock
    }

    /**
     * Gestisce l'aggiornamento del database.
     *
     * Le tabelle "categorie", "prodotti", "ingredienti" e "aggiunte" sono una semplice
     * cache offline-first: vengono SEMPRE ripopolate dal server tramite upsertCategories()/
     * upsertProducts() alla prima chiamata di rete andata a buon fine. Per questo, ad ogni
     * cambio di schema, invece di scrivere una ALTER TABLE diversa per ogni colonna aggiunta
     * (facile da dimenticare, come è successo con "attributi"), le ricreiamo da zero: non si
     * perdono dati reali e siamo certi che lo schema sia sempre allineato al codice.
     *
     * Utenti, carrello, ordini e preferiti invece sono dati reali dell'utente e non vengono
     * mai eliminati in una migrazione.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_INGREDIENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EXTRAS")

        db.execSQL("""
            CREATE TABLE $TABLE_CATEGORIES (
                $COLUMN_CAT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CAT_REMOTE_ID TEXT UNIQUE,
                $COLUMN_CAT_NAME TEXT,
                categoria_padre TEXT,
                posizionamento INTEGER,
                visibile INTEGER DEFAULT 1
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_PRODUCTS (
                $COLUMN_PROD_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_PROD_REMOTE_ID TEXT UNIQUE,
                $COLUMN_PROD_NAME TEXT,
                $COLUMN_PROD_PRICE REAL,
                $COLUMN_PROD_DESC TEXT,
                $COLUMN_PROD_CAT TEXT,
                $COLUMN_PROD_IMG_URL TEXT,
                $COLUMN_PROD_AVAILABLE INTEGER,
                $COLUMN_PROD_ATTRIBUTES TEXT,
                $COLUMN_PROD_IVA TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_INGREDIENTS (
                $COLUMN_ING_ID TEXT PRIMARY KEY,
                $COLUMN_ING_PROD_ID TEXT,
                $COLUMN_ING_NAME TEXT,
                $COLUMN_ING_REMOVABLE TEXT,
                FOREIGN KEY($COLUMN_ING_PROD_ID) REFERENCES $TABLE_PRODUCTS($COLUMN_PROD_REMOTE_ID)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_EXTRAS (
                $COLUMN_EXT_ID TEXT PRIMARY KEY,
                $COLUMN_EXT_PROD_ID TEXT,
                $COLUMN_EXT_NAME TEXT,
                $COLUMN_EXT_PRICE REAL,
                FOREIGN KEY($COLUMN_EXT_PROD_ID) REFERENCES $TABLE_PRODUCTS($COLUMN_PROD_REMOTE_ID)
            )
        """.trimIndent())
    }

    // --- METODI PER UPSERT (OFFLINE-FIRST) ---

    /** Sincronizza le categorie scaricate dall'API salvandole o aggiornandole nel database locale. */
    fun upsertCategories(networkList: List<com.example.kotlintut.data.network.NetworkCategory>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            networkList.forEach { cat ->
                val parentId = when (val pc = cat.parentCategory) {
                    is com.google.gson.JsonPrimitive -> {
                        if (pc.isString) pc.asString else pc.toString()
                    }
                    null -> null
                    else -> pc.toString()
                }

                val values = ContentValues().apply {
                    put(COLUMN_CAT_REMOTE_ID, cat.id ?: "")
                    put(COLUMN_CAT_NAME, cat.name ?: "")
                    put("categoria_padre", if (parentId == "0" || parentId == "") null else parentId)
                    put("posizionamento", cat.position ?: 0)
                    put("visibile", if (cat.isVisible == true) 1 else 0)
                }
                db.insertWithOnConflict(TABLE_CATEGORIES, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    /** Sincronizza i prodotti e i relativi ingredienti/aggiunte salvandoli nel database locale. */
    fun upsertProducts(networkList: List<com.example.kotlintut.data.network.NetworkProduct>, requestedCategoryId: String) {
        val db = writableDatabase
        db.beginTransaction()
        val gson = Gson()
        try {
            networkList.forEach { prod ->
                val values = ContentValues().apply {
                    put(COLUMN_PROD_REMOTE_ID, prod.id ?: "")
                    put(COLUMN_PROD_NAME, prod.name ?: "")
                    put(COLUMN_PROD_PRICE, prod.price ?: 0.0)
                    put(COLUMN_PROD_DESC, "")
                    put(COLUMN_PROD_CAT, requestedCategoryId)
                    put(COLUMN_PROD_IMG_URL, prod.imageUrl ?: "")
                    put(COLUMN_PROD_AVAILABLE, if (prod.isAvailable == true) 1 else 0)
                    put(COLUMN_PROD_ATTRIBUTES, gson.toJson(prod.attributes ?: emptyList<com.example.kotlintut.data.network.NetworkAttribute>()))
                    put(COLUMN_PROD_IVA, gson.toJson(prod.iva))
                }
                db.insertWithOnConflict(TABLE_PRODUCTS, null, values, SQLiteDatabase.CONFLICT_REPLACE)

                // Upsert Ingredienti
                prod.ingredients?.forEach { ing ->
                    val ingValues = ContentValues().apply {
                        put(COLUMN_ING_ID, "${prod.id ?: ""}_${ing.id ?: ""}") // Chiave composta per sicurezza
                        put(COLUMN_ING_PROD_ID, prod.id ?: "")
                        put(COLUMN_ING_NAME, ing.name ?: "")
                        put(COLUMN_ING_REMOVABLE, ing.isRemovable ?: "no")
                    }
                    db.insertWithOnConflict(TABLE_INGREDIENTS, null, ingValues, SQLiteDatabase.CONFLICT_REPLACE)
                }

                // Upsert Aggiunte
                prod.extras?.forEach { ext ->
                    val extValues = ContentValues().apply {
                        put(COLUMN_EXT_ID, "${prod.id ?: ""}_${ext.id ?: ""}")
                        put(COLUMN_EXT_PROD_ID, prod.id ?: "")
                        put(COLUMN_EXT_NAME, ext.name ?: "")
                        put(COLUMN_EXT_PRICE, ext.price ?: 0.0)
                    }
                    db.insertWithOnConflict(TABLE_EXTRAS, null, extValues, SQLiteDatabase.CONFLICT_REPLACE)
                }
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            android.util.Log.e("DatabaseHelper", "Error during upsertProducts", e)
        } finally {
            db.endTransaction()
        }
    }

    /** Recupera tutte le categorie principali (root) dal database locale. */
    fun getAllCategoriesLocal(): List<com.example.kotlintut.data.network.NetworkCategory> {
        val list = mutableListOf<com.example.kotlintut.data.network.NetworkCategory>()
        val db = readableDatabase
        // Query che restituisce solo le categorie principali (categoria_padre IS NULL o '0')
        val selection = "visibile=1 AND (categoria_padre IS NULL OR categoria_padre = '0' OR categoria_padre = '')"
        db.query(TABLE_CATEGORIES, null, selection, null, null, null, "posizionamento ASC").use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    list.add(com.example.kotlintut.data.network.NetworkCategory(
                        id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAT_REMOTE_ID)) ?: "",
                        name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAT_NAME)) ?: "",
                        parentCategory = null,
                        position = cursor.getInt(cursor.getColumnIndexOrThrow("posizionamento")),
                        isVisible = cursor.getInt(cursor.getColumnIndexOrThrow("visibile")) == 1
                    ))
                } while (cursor.moveToNext())
            }
        }
        return list
    }

    /** Recupera le sottocategorie appartenenti a una categoria padre specifica. */
    fun getSubCategoriesLocal(parentId: String): List<com.example.kotlintut.data.network.NetworkCategory> {
        val list = mutableListOf<com.example.kotlintut.data.network.NetworkCategory>()
        val db = readableDatabase
        val selection = "visibile=1 AND categoria_padre = ?"
        db.query(TABLE_CATEGORIES, null, selection, arrayOf(parentId), null, null, "posizionamento ASC").use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    list.add(com.example.kotlintut.data.network.NetworkCategory(
                        id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAT_REMOTE_ID)) ?: "",
                        name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAT_NAME)) ?: "",
                        parentCategory = null,
                        position = cursor.getInt(cursor.getColumnIndexOrThrow("posizionamento")),
                        isVisible = cursor.getInt(cursor.getColumnIndexOrThrow("visibile")) == 1
                    ))
                } while (cursor.moveToNext())
            }
        }
        return list
    }

    /** Ottiene la lista dei prodotti associati a una determinata categoria dal database locale. */
    fun getProductsByCategory(category: String): List<Product> {
        val list = mutableListOf<Product>()
        val db = readableDatabase
        val gson = Gson()
        val attributeType = object : TypeToken<List<com.example.kotlintut.data.network.NetworkAttribute>>() {}.type
        val ivaType = object : TypeToken<com.example.kotlintut.data.network.NetworkIva>() {}.type

        db.query(TABLE_PRODUCTS, null, "$COLUMN_PROD_CAT=?", arrayOf(category), null, null, null).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val remoteId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROD_REMOTE_ID)) ?: ""
                    val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROD_NAME)) ?: ""
                    val price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PROD_PRICE))
                    val desc = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROD_DESC)) ?: ""
                    val imgUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROD_IMG_URL)) ?: ""

                    val attrJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROD_ATTRIBUTES))
                    val attributes: List<com.example.kotlintut.data.network.NetworkAttribute> = try {
                        if (!attrJson.isNullOrBlank()) {
                            gson.fromJson(attrJson, attributeType) ?: emptyList()
                        } else emptyList()
                    } catch (e: Exception) {
                        emptyList()
                    }

                    val ivaJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROD_IVA))
                    val iva: com.example.kotlintut.data.network.NetworkIva? = try {
                        if (!ivaJson.isNullOrBlank()) {
                            gson.fromJson(ivaJson, ivaType)
                        } else null
                    } catch (e: Exception) {
                        null
                    }

                    list.add(Product(remoteId, name, price, desc, imgUrl, category, attributes, iva))
                } while (cursor.moveToNext())
            }
        }
        return list
    }

    /** Recupera gli ingredienti base associati a un prodotto specifico dal database locale. */
    fun getIngredientsByProduct(productId: String): List<com.example.kotlintut.data.network.NetworkIngredient> {
        val list = mutableListOf<com.example.kotlintut.data.network.NetworkIngredient>()
        val db = readableDatabase
        db.query(TABLE_INGREDIENTS, null, "$COLUMN_ING_PROD_ID=?", arrayOf(productId), null, null, null).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    list.add(com.example.kotlintut.data.network.NetworkIngredient(
                        id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ING_ID)) ?: "",
                        name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ING_NAME)) ?: "",
                        isRemovable = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ING_REMOVABLE)) ?: "no",
                        qta = 1,
                        price = 0.0
                    ))
                } while (cursor.moveToNext())
            }
        }
        return list
    }

    /** Recupera le aggiunte extra disponibili per un prodotto specifico dal database locale. */
    fun getExtrasByProduct(productId: String): List<com.example.kotlintut.data.network.NetworkExtra> {
        val list = mutableListOf<com.example.kotlintut.data.network.NetworkExtra>()
        val db = readableDatabase
        db.query(TABLE_EXTRAS, null, "$COLUMN_EXT_PROD_ID=?", arrayOf(productId), null, null, null).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    list.add(com.example.kotlintut.data.network.NetworkExtra(
                        id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXT_ID)) ?: "",
                        name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXT_NAME)) ?: "",
                        price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_EXT_PRICE)),
                        qta = 1
                    ))
                } while (cursor.moveToNext())
            }
        }
        return list
    }

    /** Ottiene la lista dei prodotti preferiti di un utente dal database locale. */
    fun getFavorites(username: String): List<Product> {
        val list = mutableListOf<Product>()
        val db = readableDatabase

        db.query(TABLE_FAVORITES, null, "$COLUMN_FAV_USER=?", arrayOf(username), null, null, null).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    // Nota: TABLE_FAVORITES al momento non ha remote_id, formati o dimensioni salvati singolarmente.
                    // Per ora ritorniamo i dati base, ma in una versione futura TABLE_FAVORITES potrebbe essere aggiornata.
                    list.add(Product(
                        id = "",
                        name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FAV_PROD_NAME)),
                        price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_FAV_PROD_PRICE)),
                        description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FAV_PROD_DESC)),
                        imageKey = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FAV_PROD_IMG))
                    ))
                } while (cursor.moveToNext())
            }
        }
        return list
    }

    /** Aggiunge un prodotto alla lista dei preferiti di un utente nel database locale. */
    fun addFavorite(username: String, product: Product) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FAV_USER, username)
            put(COLUMN_FAV_PROD_NAME, product.name)
            put(COLUMN_FAV_PROD_PRICE, product.price)
            put(COLUMN_FAV_PROD_DESC, product.description)
            put(COLUMN_FAV_PROD_IMG, product.imageKey)
        }
        db.insert(TABLE_FAVORITES, null, values)
    }

    /** Rimuove un prodotto dalla lista dei preferiti di un utente nel database locale. */
    fun removeFavorite(username: String, productName: String) {
        val db = writableDatabase
        db.delete(TABLE_FAVORITES, "$COLUMN_FAV_USER=? AND $COLUMN_FAV_PROD_NAME=?", arrayOf(username, productName))
    }

    /** Verifica se un determinato prodotto è presente nei preferiti dell'utente nel database locale. */
    fun isFavorite(username: String, productName: String): Boolean {
        val db = readableDatabase
        return db.query(TABLE_FAVORITES, null, "$COLUMN_FAV_USER=? AND $COLUMN_FAV_PROD_NAME=?", arrayOf(username, productName), null, null, null).use { it.count > 0 }
    }

    /** Salva il contenuto attuale del carrello nel database locale serializzando le personalizzazioni in JSON. */
    fun saveCart(username: String, items: List<CartItem>) {
        val db = writableDatabase
        db.delete(TABLE_CART, "$COLUMN_CART_USER=?", arrayOf(username))
        val gson = Gson()
        items.forEach { item ->
            val values = ContentValues().apply {
                put(COLUMN_CART_USER, username)
                put(COLUMN_CART_PROD_ID, item.id)
                put(COLUMN_CART_NOME, item.name)
                put(COLUMN_CART_PREZZO, item.price)
                put(COLUMN_CART_DESC, item.description)
                put(COLUMN_CART_QTY, item.quantity)
                put(COLUMN_CART_IMG, item.imageKey)
                put(COLUMN_CART_REMOVED_ING, gson.toJson(item.removedIngredients))
                put(COLUMN_CART_ADDED_EXT, gson.toJson(item.addedExtras))
                put(COLUMN_CART_ATTRIBUTES, gson.toJson(item.orderAttributes))
                put(COLUMN_CART_FULL_ATTRIBUTES, gson.toJson(item.fullAttributesList))
                put(COLUMN_CART_CAT, item.category)
            }
            db.insert(TABLE_CART, null, values)
        }
    }

    /** Carica il carrello salvato per un utente, deserializzando gli ingredienti e le aggiunte dal formato JSON. */
    fun loadCart(username: String): List<CartItem> {
        val list = mutableListOf<CartItem>()
        val db = readableDatabase
        val gson = Gson()
        val typeIng = object : TypeToken<List<com.example.kotlintut.data.network.NetworkIngredient>>() {}.type
        val typeExt = object : TypeToken<List<com.example.kotlintut.data.network.NetworkExtra>>() {}.type
        val typeOrderAttr = object : TypeToken<List<com.example.kotlintut.data.network.OrderSelectedAttribute>>() {}.type
        val typeFullAttr = object : TypeToken<List<com.example.kotlintut.data.network.NetworkAttribute>>() {}.type

        db.query(TABLE_CART, null, "$COLUMN_CART_USER=?", arrayOf(username), null, null, null).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CART_PROD_ID)) ?: ""
                    val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CART_NOME))
                    val price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CART_PREZZO))
                    val desc = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CART_DESC))
                    val img = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CART_IMG))
                    val qty = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CART_QTY))

                    val removedIngJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CART_REMOVED_ING))
                    val addedExtJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CART_ADDED_EXT))
                    val attrJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CART_ATTRIBUTES))
                    val fullAttrJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CART_FULL_ATTRIBUTES))
                    val category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CART_CAT)) ?: ""

                    val removedIng: List<com.example.kotlintut.data.network.NetworkIngredient> = gson.fromJson(removedIngJson, typeIng) ?: emptyList()
                    val addedExt: List<com.example.kotlintut.data.network.NetworkExtra> = gson.fromJson(addedExtJson, typeExt) ?: emptyList()
                    val orderAttributes: List<com.example.kotlintut.data.network.OrderSelectedAttribute> = gson.fromJson(attrJson, typeOrderAttr) ?: emptyList()
                    val fullAttributes: List<com.example.kotlintut.data.network.NetworkAttribute> = gson.fromJson(fullAttrJson, typeFullAttr) ?: emptyList()

                    list.add(CartItem(
                        id = id,
                        name = name,
                        price = price,
                        quantity = qty,
                        orderAttributes = orderAttributes,
                        addedExtras = addedExt,
                        removedIngredients = removedIng,
                        description = desc,
                        imageKey = img,
                        fullAttributesList = fullAttributes,
                        category = category,
                        categorie = listOf(category),
                        categoriaOrigine = category
                    ))
                } while (cursor.moveToNext())
            }
        }
        return list
    }

    /** Registra un nuovo ordine nel database locale, salvando i dettagli dei prodotti e le loro personalizzazioni. */
    fun saveOrder(username: String, total: Double, items: List<CartItem>) {
        val db = writableDatabase
        val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val gson = Gson()
        val orderValues = ContentValues().apply {
            put(COLUMN_ORDER_USER, username)
            put(COLUMN_ORDER_TOTAL, total)
            put(COLUMN_ORDER_DATE, date)
        }
        val orderId = db.insert(TABLE_ORDERS, null, orderValues)
        if (orderId != -1L) {
            items.forEach { item ->
                val itemValues = ContentValues().apply {
                    put(COLUMN_ITEM_ORDER_ID, orderId)
                    put(COLUMN_ITEM_PROD_ID, item.id)
                    put(COLUMN_ITEM_NAME, item.name)
                    put(COLUMN_ITEM_PRICE, item.price)
                    put(COLUMN_ITEM_QTY, item.quantity)
                    put(COLUMN_ITEM_REMOVED_ING, gson.toJson(item.removedIngredients))
                    put(COLUMN_ITEM_ADDED_EXT, gson.toJson(item.addedExtras))
                    put(COLUMN_ITEM_ATTRIBUTES, gson.toJson(item.orderAttributes))
                    put(COLUMN_ITEM_FULL_ATTRIBUTES, gson.toJson(item.fullAttributesList))
                    put(COLUMN_ITEM_CAT, item.category)
                }
                db.insert(TABLE_ORDER_ITEMS, null, itemValues)
            }
        }
    }

    /** Recupera la cronologia degli ordini effettuati da un utente dal database locale. */
    fun getOrdersByUser(username: String): List<Order> {
        val orders = mutableListOf<Order>()
        val db = readableDatabase
        db.query(TABLE_ORDERS, null, "$COLUMN_ORDER_USER=?", arrayOf(username), null, null, "$COLUMN_ORDER_ID DESC").use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORDER_ID))
                    val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_DATE))
                    val total = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_ORDER_TOTAL))
                    val items = getOrderItems(id)
                    orders.add(Order(id, date, total, "In elaborazione", items))
                } while (cursor.moveToNext())
            }
        }
        return orders
    }

    /** Ottiene i dettagli dei prodotti inclusi in un ordine specifico dal database locale. */
    fun getOrderItems(orderId: Int): List<CartItem> {
        val items = mutableListOf<CartItem>()
        val db = readableDatabase
        val gson = Gson()
        val typeIng = object : TypeToken<List<com.example.kotlintut.data.network.NetworkIngredient>>() {}.type
        val typeExt = object : TypeToken<List<com.example.kotlintut.data.network.NetworkExtra>>() {}.type
        val typeOrderAttr = object : TypeToken<List<com.example.kotlintut.data.network.OrderSelectedAttribute>>() {}.type
        val typeFullAttr = object : TypeToken<List<com.example.kotlintut.data.network.NetworkAttribute>>() {}.type

        db.query(TABLE_ORDER_ITEMS, null, "$COLUMN_ITEM_ORDER_ID=?", arrayOf(orderId.toString()), null, null, null).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_PROD_ID)) ?: ""
                    val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_NAME))
                    val price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_ITEM_PRICE))
                    val qty = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_QTY))

                    val removedIngJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_REMOVED_ING))
                    val addedExtJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_ADDED_EXT))
                    val attrJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_ATTRIBUTES))
                    val fullAttrJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_FULL_ATTRIBUTES))
                    val category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_CAT)) ?: ""

                    val removedIng: List<com.example.kotlintut.data.network.NetworkIngredient> = gson.fromJson(removedIngJson, typeIng) ?: emptyList()
                    val addedExt: List<com.example.kotlintut.data.network.NetworkExtra> = gson.fromJson(addedExtJson, typeExt) ?: emptyList()
                    val orderAttributes: List<com.example.kotlintut.data.network.OrderSelectedAttribute> = gson.fromJson(attrJson, typeOrderAttr) ?: emptyList()
                    val fullAttributes: List<com.example.kotlintut.data.network.NetworkAttribute> = gson.fromJson(fullAttrJson, typeFullAttr) ?: emptyList()

                    items.add(CartItem(
                        id = id,
                        name = name,
                        price = price,
                        quantity = qty,
                        orderAttributes = orderAttributes,
                        addedExtras = addedExt,
                        removedIngredients = removedIng,
                        fullAttributesList = fullAttributes,
                        category = category,
                        categorie = listOf(category),
                        categoriaOrigine = category
                    ))
                } while (cursor.moveToNext())
            }
        }
        return items
    }

    /** Verifica le credenziali di login nel database locale e restituisce l'username se corretti. */
    fun loginAndGetUsername(identifier: String, pass: String): String? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USERNAME),
            "($COLUMN_USERNAME=? OR $COLUMN_EMAIL=?) AND $COLUMN_PASSWORD=?",
            arrayOf(identifier, identifier, pass),
            null, null, null
        )
        return cursor.use {
            if (it.moveToFirst()) it.getString(0) else null
        }
    }

    /** Controlla se un utente esiste già nel database locale tramite il suo username. */
    fun userExists(username: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            "$COLUMN_USERNAME=?",
            arrayOf(username),
            null, null, null
        )
        return cursor.use { it.count > 0 }
    }

    /** Registra un nuovo utente nel database locale salvando i suoi dati personali. */
    fun registerUser(username: String, email: String, pass: String, nome: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD, pass)
            put(COLUMN_NOME, nome)
        }
        return db.insert(TABLE_USERS, null, values)
    }
}