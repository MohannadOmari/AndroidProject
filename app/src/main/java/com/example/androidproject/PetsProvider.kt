package com.example.androidproject

import android.content.ContentProvider
import android.content.*
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.text.TextUtils
import java.lang.IllegalArgumentException
import java.net.URL
import java.util.HashMap



class PetsProvider() : ContentProvider() {
    companion object {
        val PROVIDER_NAME = "com.example.androidproject.PetsProvider"
        val URL = "content://" + PROVIDER_NAME + "/pets"
        val CONTENT_URI = Uri.parse(URL)
        val _ID = "_id"
        val NAME = "name"
        val TYPE = "type"
        val AGE = "age"
        private val PETS_PROJECTION_MAP: HashMap<String, String>? = null
        val PETS = 1
        val PET_ID = 2
        val uriMatcher: UriMatcher? = null
        val DATABASE_NAME = "Pet_Shop"
        val PETS_TABLE_NAME = "Pets"
        val DATABASE_VERSION = 1
        val CREATE_DB_TABLE = "CREATE TABLE " + PETS_TABLE_NAME +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT," + "name TEXT NOT NULL," +
                "type TEXT NOT NULL," + "age INTEGER NOT NULL);"
        private var sUriMatcher = UriMatcher(UriMatcher.NO_MATCH);

        init {
            sUriMatcher.addURI(PROVIDER_NAME, "Pets", PETS)
            sUriMatcher.addURI(PROVIDER_NAME, "Pets/#", PET_ID)
        }
    }

    private var db: SQLiteDatabase? = null

    private class DatabaseHelper internal constructor(context: Context?) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(CREATE_DB_TABLE)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS" + PETS_TABLE_NAME)
            onCreate(db)
        }
    }

    override fun onCreate(): Boolean {
        val context = context
        val dbHelper = DatabaseHelper(context)
        db = dbHelper.writableDatabase
        return if (db == null) false else true
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val rowID = db!!.insert(PETS_TABLE_NAME, "", values)

        if (rowID > 0) {
            val _uri = ContentUris.withAppendedId(CONTENT_URI, rowID)
            context!!.contentResolver.notifyChange(_uri, null)
            return _uri
        }
        throw SQLException("Failed to add a record into $uri")
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        var sortOrder = sortOrder
        val qb = SQLiteQueryBuilder()
        qb.tables = PETS_TABLE_NAME
        when (uriMatcher!!.match(uri)) {
            PET_ID -> {
                qb.appendWhere(_ID + "=" + uri.pathSegments[1])
            }
            else -> {
                null
            }
        }
        if (sortOrder == null || sortOrder === "") {
            sortOrder = NAME
        }
        val c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder)
        c.setNotificationUri(context!!.contentResolver, uri)
        return c
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        var count = 0
        when (uriMatcher!!.match(uri)) {
            PETS -> count = db!!.delete(
                PETS_TABLE_NAME, selection,
                selectionArgs
            )
            PET_ID -> {
                val id = uri.pathSegments[1]
                count = db!!.delete(
                    PETS_TABLE_NAME,
                    _ID + "=" + id +
                            if (!TextUtils.isEmpty(selection)) "AND ($selection)" else "",
                    selectionArgs
                )
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return count
    }

    override fun update(uri: Uri, values: ContentValues?,
                        selection: String?, selectionArgs: Array<String>?): Int {
        var count = 0
        when (uriMatcher!!.match(uri)) {
            PETS -> count = db!!.update(
                PETS_TABLE_NAME, values, selection,
                selectionArgs
            )
            PET_ID -> count = db!!.update(
                PETS_TABLE_NAME,
                values,
                _ID + "=" + uri.pathSegments[1] + (if (!TextUtils.isEmpty(selection)) " AND ($selection)" else ""),
                selectionArgs
            )
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return count
    }

    override fun getType(uri: Uri): String? {
        when (uriMatcher!!.match(uri)) {
            PETS -> return "vnd.android.cursor.dir/vnd.example.pets"
            PET_ID -> return "vnd.android.cursor.item/vnd.example.pets"
            else -> throw IllegalArgumentException("Unsupported URI: $uri")
        }
    }
}