package com.example.kotlintut.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.kotlintut.data.model.*
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "RistoranteTotem_Compose.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_USERS = "utenti"
        const val COLUMN_USER_ID = "id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_NOME = "nome"
        const val COLUMN_CARD_NUMBER = "card_number"
        const val COLUMN_CARD_EXPIRY = "card_expiry"
        const val COLUMN_CARD_CVV = "card_cvv"

        const val TABLE_PRODUCTS = "prodotti"
        const val COLUMN_PROD_ID = "id"
        const val COLUMN_PROD_NAME = "nome_key"
        const val COLUMN_PROD_PRICE = "prezzo"
        const val COLUMN_PROD_DESC = "desc_key"
        const val COLUMN_PROD_CAT = "categoria"

        const val TABLE_CART = "carrello_salvato"
        const val COLUMN_CART_ID = "id"
        const val COLUMN_CART_USER = "username"
        const val COLUMN_CART_NOME = "prodotto_nome"
        const val COLUMN_CART_PREZZO = "prodotto_prezzo"
        const val COLUMN_CART_DESC = "prodotto_desc"
        const val COLUMN_CART_QTY = "quantita"
        const val COLUMN_CART_IMG = "prodotto_img"

        const val TABLE_ORDERS = "ordini"
        const val COLUMN_ORDER_ID = "id"
        const val COLUMN_ORDER_USER = "username"
        const val COLUMN_ORDER_DATE = "data"
        const val COLUMN_ORDER_TOTAL = "totale"

        const val TABLE_ORDER_ITEMS = "ordini_dettagli"
        const val COLUMN_ITEM_ID = "id"
        const val COLUMN_ITEM_ORDER_ID = "ordine_id"
        const val COLUMN_ITEM_NAME = "prodotto_nome"
        const val COLUMN_ITEM_PRICE = "prodotto_prezzo"
        const val COLUMN_ITEM_QTY = "quantita"

        const val TABLE_ATTRIBUTES = "attributi"
        const val COLUMN_ATTR_ID = "id"
        const val COLUMN_ATTR_PROD_ID = "prodotto_id"
        const val COLUMN_ATTR_NAME = "nome"
        const val COLUMN_ATTR_PRICE = "prezzo_extra"

        const val TABLE_FAVORITES = "preferiti"
        const val COLUMN_FAV_ID = "id"
        const val COLUMN_FAV_USER = "username"
        const val COLUMN_FAV_PROD_NAME = "prodotto_nome"
        const val COLUMN_FAV_PROD_PRICE = "prodotto_prezzo"
        const val COLUMN_FAV_PROD_DESC = "prodotto_desc"
        const val COLUMN_FAV_PROD_IMG = "prodotto_img"
    }

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
            CREATE TABLE $TABLE_PRODUCTS (
                $COLUMN_PROD_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_PROD_NAME TEXT,
                $COLUMN_PROD_PRICE REAL,
                $COLUMN_PROD_DESC TEXT,
                $COLUMN_PROD_CAT TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_CART (
                $COLUMN_CART_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CART_USER TEXT,
                $COLUMN_CART_NOME TEXT,
                $COLUMN_CART_PREZZO REAL,
                $COLUMN_CART_DESC TEXT,
                $COLUMN_CART_QTY INTEGER,
                $COLUMN_CART_IMG TEXT
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
                $COLUMN_ITEM_NAME TEXT,
                $COLUMN_ITEM_PRICE REAL,
                $COLUMN_ITEM_QTY INTEGER,
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
            CREATE TABLE $TABLE_ATTRIBUTES (
                $COLUMN_ATTR_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_ATTR_PROD_ID INTEGER,
                $COLUMN_ATTR_NAME TEXT,
                $COLUMN_ATTR_PRICE REAL,
                FOREIGN KEY($COLUMN_ATTR_PROD_ID) REFERENCES $TABLE_PRODUCTS($COLUMN_PROD_ID)
            )
        """.trimIndent())

        insertInitialProducts(db)
    }

    private fun insertInitialProducts(db: SQLiteDatabase) {
        val products = listOf(
            Triple("prod_hamburger", 7.50, "desc_hamburger" to "Panini"),
            Triple("prod_cheeseburger", 8.00, "desc_cheeseburger" to "Panini"),
            Triple("prod_pizza", 12.00, "desc_pizza" to "Primi"),
            Triple("prod_pasta_al_pesto", 8.50, "desc_pasta_al_pesto" to "Primi"),
            Triple("prod_pasta", 6.00, "desc_pasta" to "Primi"),
            Triple("prod_lasagna", 9.00, "desc_lasagna" to "Primi"),
            Triple("prod_cotoletta", 10.00, "desc_cotoletta" to "Secondi"),
            Triple("prod_grigliata", 15.00, "desc_grigliata" to "Secondi"),
            Triple("prod_acqua", 1.50, "desc_acqua" to "Bevande"),
            Triple("prod_coca", 2.50, "desc_coca" to "Bevande"),
            Triple("prod_fanta", 2.50, "desc_fanta" to "Bevande"),
            Triple("prod_sprite", 2.50, "desc_sprite" to "Bevande"),
            Triple("prod_fanta_zero", 2.50, "desc_fanta_zero" to "Bevande"),
            Triple("prod_birra", 3.00, "desc_birra" to "Bevande"),
            Triple("prod_chinotto", 3.00, "desc_chinotto" to "Bevande"),
            Triple("prod_lemon_soda", 3.00, "desc_lemon_soda" to "Bevande"),
            Triple("prod_oran_soda", 3.00, "desc_oran_soda" to "Bevande"),
            Triple("prod_water", 2.00, "desc_water" to "Bevande"),
            Triple("prod_the_pesca", 3.00, "desc_the_pesca" to "Bevande"),
            Triple("prod_the_limone", 3.00, "desc_the_limone" to "Bevande")
        )

        products.forEach { (name, price, descCat) ->
            val (desc, cat) = descCat
            db.execSQL("INSERT INTO $TABLE_PRODUCTS ($COLUMN_PROD_NAME, $COLUMN_PROD_PRICE, $COLUMN_PROD_DESC, $COLUMN_PROD_CAT) VALUES ('$name', $price, '$desc', '$cat')")
        }

        db.execSQL("INSERT INTO $TABLE_ATTRIBUTES ($COLUMN_ATTR_PROD_ID, $COLUMN_ATTR_NAME, $COLUMN_ATTR_PRICE) VALUES (1, 'Cipolla extra', 0.50)")
        db.execSQL("INSERT INTO $TABLE_ATTRIBUTES ($COLUMN_ATTR_PROD_ID, $COLUMN_ATTR_NAME, $COLUMN_ATTR_PRICE) VALUES (1, 'Bacon', 1.00)")
        db.execSQL("INSERT INTO $TABLE_ATTRIBUTES ($COLUMN_ATTR_PROD_ID, $COLUMN_ATTR_NAME, $COLUMN_ATTR_PRICE) VALUES (1, 'Senza cetrioli', 0.00)")
        db.execSQL("INSERT INTO $TABLE_ATTRIBUTES ($COLUMN_ATTR_PROD_ID, $COLUMN_ATTR_NAME, $COLUMN_ATTR_PRICE) VALUES (4, 'Parmigiano extra', 0.80)")
        db.execSQL("INSERT INTO $TABLE_ATTRIBUTES ($COLUMN_ATTR_PROD_ID, $COLUMN_ATTR_NAME, $COLUMN_ATTR_PRICE) VALUES (4, 'Pesto extra', 1.20)")
        db.execSQL("INSERT INTO $TABLE_ATTRIBUTES ($COLUMN_ATTR_PROD_ID, $COLUMN_ATTR_NAME, $COLUMN_ATTR_PRICE) VALUES (9, 'Senza ghiaccio', 0.00)")
        db.execSQL("INSERT INTO $TABLE_ATTRIBUTES ($COLUMN_ATTR_PROD_ID, $COLUMN_ATTR_NAME, $COLUMN_ATTR_PRICE) VALUES (9, 'Fetta di limone', 0.20)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CART")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDER_ITEMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ATTRIBUTES")
        onCreate(db)
    }

    fun userExists(username: String): Boolean {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_USERS WHERE $COLUMN_USERNAME=?", arrayOf(username)).use { it.count > 0 }
    }

    fun registerUser(user: String, email: String, pass: String, nome: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, user)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD, pass)
            put(COLUMN_NOME, nome)
        }
        return db.insert(TABLE_USERS, null, values)
    }

    fun loginAndGetUsername(identifier: String, pass: String): String? {
        val db = readableDatabase
        return db.rawQuery("SELECT $COLUMN_USERNAME FROM $TABLE_USERS WHERE ($COLUMN_USERNAME=? OR $COLUMN_EMAIL=?) AND $COLUMN_PASSWORD=?", arrayOf(identifier, identifier, pass)).use {
            if (it.moveToFirst()) it.getString(0) else null
        }
    }

    fun getProductsByCategory(category: String): List<Product> {
        val list = mutableListOf<Product>()
        val db = readableDatabase
        db.query(TABLE_PRODUCTS, null, "$COLUMN_PROD_CAT=?", arrayOf(category), null, null, null).use { cursor ->
            if (cursor.moveToFirst()) {
                val res = context.resources
                val pkg = context.packageName
                do {
                    val nomeKey = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROD_NAME))
                    val prezzo = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PROD_PRICE))
                    val descKey = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROD_DESC))

                    val resNomeId = res.getIdentifier(nomeKey, "string", pkg)
                    val resDescId = res.getIdentifier(descKey, "string", pkg)

                    val nomeTradotto = if (resNomeId != 0) context.getString(resNomeId) else nomeKey
                    val descTradotta = if (resDescId != 0) context.getString(resDescId) else descKey

                    var imgKey = nomeKey.replace("prod_", "")
                    imgKey = when (imgKey) {
                        "hamburger" -> "burger"
                        "pasta_al_pesto" -> "pesto"
                        "pasta" -> "sugo"
                        "fanta_zero" -> "fantazero"
                        "lemon_soda" -> "lemon"
                        "oran_soda" -> "oran"
                        "the_pesca" -> "pesca"
                        "the_limone" -> "lemonthe"
                        "acqua" -> "water"
                        "coca" -> "coca"
                        "birra" -> "birra"
                        else -> imgKey
                    }

                    list.add(Product(nomeTradotto, prezzo, descTradotta, imgKey, category))
                } while (cursor.moveToNext())
            }
        }
        return list
    }

    fun getAttributesByProduct(productName: String): List<Attribute> {
        val list = mutableListOf<Attribute>()
        val db = readableDatabase
        val query = """
            SELECT a.$COLUMN_ATTR_NAME, a.$COLUMN_ATTR_PRICE
            FROM $TABLE_ATTRIBUTES a
            JOIN $TABLE_PRODUCTS p ON a.$COLUMN_ATTR_PROD_ID = p.$COLUMN_PROD_ID
            WHERE ? LIKE '%' || REPLACE(p.$COLUMN_PROD_NAME, 'prod_', '') || '%'
            OR REPLACE(p.$COLUMN_PROD_NAME, 'prod_', '') LIKE '%' || ? || '%'
        """.trimIndent()

        db.rawQuery(query, arrayOf(productName, productName)).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    list.add(Attribute(cursor.getString(0), cursor.getDouble(1)))
                } while (cursor.moveToNext())
            }
        }
        return list
    }

    fun getFavorites(username: String): List<Product> {
        val list = mutableListOf<Product>()
        val db = readableDatabase
        db.query(TABLE_FAVORITES, null, "$COLUMN_FAV_USER=?", arrayOf(username), null, null, null).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    list.add(Product(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FAV_PROD_NAME)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_FAV_PROD_PRICE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FAV_PROD_DESC)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FAV_PROD_IMG))
                    ))
                } while (cursor.moveToNext())
            }
        }
        return list
    }

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

    fun removeFavorite(username: String, productName: String) {
        val db = writableDatabase
        db.delete(TABLE_FAVORITES, "$COLUMN_FAV_USER=? AND $COLUMN_FAV_PROD_NAME=?", arrayOf(username, productName))
    }

    fun isFavorite(username: String, productName: String): Boolean {
        val db = readableDatabase
        return db.query(TABLE_FAVORITES, null, "$COLUMN_FAV_USER=? AND $COLUMN_FAV_PROD_NAME=?", arrayOf(username, productName), null, null, null).use { it.count > 0 }
    }

    fun saveCart(username: String, items: List<CartItem>) {
        val db = writableDatabase
        db.delete(TABLE_CART, "$COLUMN_CART_USER=?", arrayOf(username))
        items.forEach { item ->
            val values = ContentValues().apply {
                put(COLUMN_CART_USER, username)
                put(COLUMN_CART_NOME, item.product.name)
                put(COLUMN_CART_PREZZO, item.product.price)
                put(COLUMN_CART_DESC, item.product.description)
                put(COLUMN_CART_QTY, item.quantity)
                put(COLUMN_CART_IMG, item.product.imageKey)
            }
            db.insert(TABLE_CART, null, values)
        }
    }

    fun loadCart(username: String): List<CartItem> {
        val list = mutableListOf<CartItem>()
        val db = readableDatabase
        db.query(TABLE_CART, null, "$COLUMN_CART_USER=?", arrayOf(username), null, null, null).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val product = Product(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CART_NOME)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CART_PREZZO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CART_DESC)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CART_IMG))
                    )
                    list.add(CartItem(product, cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CART_QTY))))
                } while (cursor.moveToNext())
            }
        }
        return list
    }

    fun saveOrder(username: String, total: Double, items: List<CartItem>) {
        val db = writableDatabase
        val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
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
                    put(COLUMN_ITEM_NAME, item.product.name)
                    put(COLUMN_ITEM_PRICE, item.product.price)
                    put(COLUMN_ITEM_QTY, item.quantity)
                }
                db.insert(TABLE_ORDER_ITEMS, null, itemValues)
            }
        }
    }

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
                    orders.add(Order(id, date, total, items))
                } while (cursor.moveToNext())
            }
        }
        return orders
    }

    private fun getOrderItems(orderId: Int): List<CartItem> {
        val items = mutableListOf<CartItem>()
        val db = readableDatabase
        db.query(TABLE_ORDER_ITEMS, null, "$COLUMN_ITEM_ORDER_ID=?", arrayOf(orderId.toString()), null, null, null).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val product = Product(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_NAME)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_ITEM_PRICE)),
                        "", ""
                    )
                    items.add(CartItem(product, cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_QTY))))
                } while (cursor.moveToNext())
            }
        }
        return items
    }
}
