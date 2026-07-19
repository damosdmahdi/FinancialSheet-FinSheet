package com.mobileprogramming.finsheet.data.remote

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class GoogleSheetsRepository(private val context: Context) {
    private val client = OkHttpClient()
    
    private val prefs = context.getSharedPreferences("finsheet_prefs", Context.MODE_PRIVATE)
    
    fun getSpreadsheetId(): String? {
        return prefs.getString("spreadsheet_id", null)
    }
    
    fun saveSpreadsheetId(id: String) {
        prefs.edit().putString("spreadsheet_id", id).apply()
    }

    fun clearSpreadsheetId() {
        prefs.edit().remove("spreadsheet_id").apply()
    }

    fun getManualAccessToken(): String? {
        return prefs.getString("manual_access_token", null)
    }

    fun saveManualAccessToken(token: String) {
        prefs.edit().putString("manual_access_token", token).apply()
    }

    fun clearManualAccessToken() {
        prefs.edit().remove("manual_access_token").apply()
    }

    fun getAppsScriptUrl(): String? {
        return prefs.getString("apps_script_url", null)
    }

    fun saveAppsScriptUrl(url: String) {
        prefs.edit().putString("apps_script_url", url).apply()
    }

    fun clearAppsScriptUrl() {
        prefs.edit().remove("apps_script_url").apply()
    }

    suspend fun appendTransactionsViaAppsScript(
        url: String,
        values: List<List<Any>>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                val arrayOuter = org.json.JSONArray()
                values.forEach { row ->
                    val arrayInner = org.json.JSONArray()
                    row.forEach { cell ->
                        arrayInner.put(cell)
                    }
                    arrayOuter.put(arrayInner)
                }
                put("values", arrayOuter)
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val isSuccessful = response.isSuccessful
            val bodyString = response.body?.string()
            response.close()
            
            if (isSuccessful && bodyString != null) {
                val json = JSONObject(bodyString)
                return@withContext json.optString("status") == "success"
            }
            return@withContext isSuccessful
        } catch (e: Exception) {
            Log.e("GoogleSheetsRepo", "Error appending via Apps Script", e)
            return@withContext false
        }
    }

    suspend fun findSpreadsheetByName(accessToken: String): String? = withContext(Dispatchers.IO) {
        try {
            val query = "name='FinSheet Transactions Backup' and mimeType='application/vnd.google-apps.spreadsheet' and trashed=false"
            val url = okhttp3.HttpUrl.Builder()
                .scheme("https")
                .host("www.googleapis.com")
                .addPathSegment("drive")
                .addPathSegment("v3")
                .addPathSegment("files")
                .addQueryParameter("q", query)
                .build()

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $accessToken")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            response.close()

            if (response.isSuccessful && responseBody != null) {
                val jsonObject = JSONObject(responseBody)
                val filesArray = jsonObject.optJSONArray("files")
                if (filesArray != null && filesArray.length() > 0) {
                    val firstFile = filesArray.getJSONObject(0)
                    return@withContext firstFile.getString("id")
                }
            } else {
                Log.e("GoogleSheetsRepo", "Failed to find spreadsheet: $responseBody")
                if (response.code == 403) {
                    throw Exception("DRIVE_API_DISABLED")
                }
            }
            return@withContext null
        } catch (e: Exception) {
            Log.e("GoogleSheetsRepo", "Error finding spreadsheet", e)
            if (e.message == "DRIVE_API_DISABLED") throw e
            return@withContext null
        }
    }

    // Returns Pair(spreadsheetId, isNew)
    suspend fun ensureSpreadsheetExists(accessToken: String): Pair<String?, Boolean> = withContext(Dispatchers.IO) {
        var id = getSpreadsheetId()
        var isNew = false
        
        // 1. Verify if local ID still exists on Google's servers
        if (id != null) {
            try {
                val request = Request.Builder()
                    .url("https://sheets.googleapis.com/v4/spreadsheets/$id?fields=spreadsheetId")
                    .addHeader("Authorization", "Bearer $accessToken")
                    .get()
                    .build()
                val response = client.newCall(request).execute()
                val isSuccessful = response.isSuccessful
                response.close()
                
                if (!isSuccessful) {
                    clearSpreadsheetId()
                    id = null
                }
            } catch (e: Exception) {
                clearSpreadsheetId()
                id = null
            }
        }
        
        // 2. If no local ID, try to find it in Google Drive (in case user reinstalled / logged out)
        if (id == null) {
            id = findSpreadsheetByName(accessToken)
            if (id != null) {
                saveSpreadsheetId(id)
            }
        }
        
        // 3. If still not found, create a brand new one
        if (id == null) {
            id = createSpreadsheet(accessToken)
            if (id != null) {
                isNew = true
            }
        }
        return@withContext Pair(id, isNew)
    }

    suspend fun createSpreadsheet(accessToken: String): String? = withContext(Dispatchers.IO) {
        try {
            val jsonBody = """
                {
                  "properties": {
                    "title": "FinSheet Transactions Backup"
                  },
                  "sheets": [
                    {
                      "properties": {
                        "title": "Transactions"
                      },
                      "data": [
                        {
                          "startRow": 0,
                          "startColumn": 0,
                          "rowData": [
                            {
                              "values": [
                                { "userEnteredValue": { "stringValue": "Transaction ID" } },
                                { "userEnteredValue": { "stringValue": "Date" } },
                                { "userEnteredValue": { "stringValue": "Type" } },
                                { "userEnteredValue": { "stringValue": "Category" } },
                                { "userEnteredValue": { "stringValue": "Amount" } },
                                { "userEnteredValue": { "stringValue": "Notes" } },
                                { "userEnteredValue": { "stringValue": "Created At" } }
                              ]
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
            """.trimIndent()

            val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
            
            val request = Request.Builder()
                .url("https://sheets.googleapis.com/v4/spreadsheets")
                .addHeader("Authorization", "Bearer $accessToken")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val jsonObject = JSONObject(responseBody)
                val spreadsheetId = jsonObject.getString("spreadsheetId")
                saveSpreadsheetId(spreadsheetId)
                return@withContext spreadsheetId
            } else {
                Log.e("GoogleSheetsRepo", "Failed to create spreadsheet: $responseBody")
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e("GoogleSheetsRepo", "Error creating spreadsheet", e)
            return@withContext null
        }
    }

    suspend fun appendTransactions(
        accessToken: String,
        spreadsheetId: String,
        values: List<List<Any>>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("range", "Transactions!A:G")
                put("majorDimension", "ROWS")
                
                val valuesArray = org.json.JSONArray()
                for (row in values) {
                    val rowArray = org.json.JSONArray()
                    for (cell in row) {
                        rowArray.put(cell)
                    }
                    valuesArray.put(rowArray)
                }
                put("values", valuesArray)
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            
            val request = Request.Builder()
                .url("https://sheets.googleapis.com/v4/spreadsheets/$spreadsheetId/values/Transactions%21A%3AG/append?valueInputOption=USER_ENTERED&insertDataOption=INSERT_ROWS")
                .addHeader("Authorization", "Bearer $accessToken")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful) {
                return@withContext true
            } else {
                Log.e("GoogleSheetsRepo", "Failed to append: $responseBody")
                if (response.code == 404) {
                    clearSpreadsheetId()
                }
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e("GoogleSheetsRepo", "Error appending to spreadsheet", e)
            return@withContext false
        }
    }
}
